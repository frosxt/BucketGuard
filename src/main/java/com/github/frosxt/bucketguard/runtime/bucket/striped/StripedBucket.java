package com.github.frosxt.bucketguard.runtime.bucket.striped;

import com.github.frosxt.bucketguard.api.LimiterStats;
import com.github.frosxt.bucketguard.api.Permit;
import com.github.frosxt.bucketguard.api.spec.TokenBucketSpec;
import com.github.frosxt.bucketguard.runtime.bucket.Bucket;
import com.github.frosxt.bucketguard.runtime.bucket.atomic.AtomicBucket;

import java.time.Duration;

/**
 * A token bucket implementation that stripes requests across multiple inner
 * buckets to reduce contention.
 * <p>
 * This implementation is suitable for high-concurrency scenarios where a single
 * atomic bucket becomes a bottleneck due to CAS failures.
 */
public class StripedBucket implements Bucket {
    private final Bucket[] stripes;
    private final int mask;
    private final long totalCapacity;
    private final long totalRefillTokens;
    private final Duration refillPeriod;

    /**
     * Creates a new StripedBucket.
     * 
     * @param spec        bucket configuration, not null
     * @param stripeCount number of stripes, must be a power of two
     */
    public StripedBucket(final TokenBucketSpec spec, final int stripeCount) {
        if (Integer.bitCount(stripeCount) != 1) {
            throw new IllegalArgumentException("stripeCount must be a power of two");
        }

        this.mask = stripeCount - 1;
        this.stripes = new Bucket[stripeCount];

        this.totalCapacity = spec.capacity();
        this.totalRefillTokens = spec.refillTokens();
        this.refillPeriod = spec.refillPeriod();

        final long capacityPerStripe = totalCapacity / stripeCount;
        final long capacityRemainder = totalCapacity % stripeCount;

        final long refillPerStripe = totalRefillTokens / stripeCount;
        final long refillRemainder = totalRefillTokens % stripeCount;

        for (int i = 0; i < stripeCount; i++) {
            final long cap = capacityPerStripe + (i < capacityRemainder ? 1 : 0);
            final long ref = refillPerStripe + (i < refillRemainder ? 1 : 0);

            if (cap < 1 || ref < 1) {
                throw new IllegalStateException(
                        "Stripe capacity or refill became 0. Striping count too high for spec.");
            }

            final TokenBucketSpec stripeSpec = TokenBucketSpec.builder()
                    .capacity(cap)
                    .refillTokens(ref)
                    .refillPeriod(spec.refillPeriod())
                    .timeSource(spec.timeSource())
                    .strictMath(spec.strictMath())
                    .allowBurst(spec.allowBurst())
                    .build();

            stripes[i] = new AtomicBucket(stripeSpec);
        }
    }

    @Override
    public Permit tryAcquire(final long tokens, final long nowNanos) {
        int h = (int) Thread.currentThread().threadId();
        h ^= (h >>> 16);
        h *= 0x85ebca6b;
        h ^= (h >>> 13);
        h *= 0xc2b2ae35;
        h ^= (h >>> 16);

        final int index = h & mask;
        return stripes[index].tryAcquire(tokens, nowNanos);
    }

    @Override
    public LimiterStats snapshot() {
        long avail = 0;
        for (final Bucket b : stripes) {
            avail += b.snapshot().availableTokens();
        }

        return new LimiterStats(totalCapacity, avail, totalRefillTokens, refillPeriod);
    }
}
