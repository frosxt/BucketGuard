package com.github.frosxt.bucketguard.runtime.bucket.atomic;

import com.github.frosxt.bucketguard.api.LimiterStats;
import com.github.frosxt.bucketguard.api.Permit;
import com.github.frosxt.bucketguard.api.spec.TokenBucketSpec;
import com.github.frosxt.bucketguard.api.time.TimeSource;
import com.github.frosxt.bucketguard.runtime.bucket.AbstractBucket;
import com.github.frosxt.bucketguard.runtime.bucket.SimplePermit;

import java.time.Duration;

/**
 * A token bucket implementation using atomic operations (CAS).
 * <p>
 * This implementation is lock-free and suitable for low-to-moderate contention.
 * It uses the Generic Cell Rate Algorithm (GCRA) for efficiency and
 * zero-allocation state management.
 */
public class AtomicBucket extends AbstractBucket {

    private final AtomicStateCodec state;
    private final long emissionIntervalNanos;
    private final long burstOffsetNanos;
    private final boolean strictMath;
    private final TimeSource timeSource;

    /**
     * Creates a new AtomicBucket with the given spec.
     * 
     * @param spec configuration spec, not null
     */
    public AtomicBucket(final TokenBucketSpec spec) {
        super(spec);

        this.state = new AtomicStateCodec();
        this.emissionIntervalNanos = RefillMath.calculateEmissionIntervalNanos(spec.refillTokens(), spec.refillPeriod());
        this.burstOffsetNanos = RefillMath.calculateBurstOffsetNanos(spec.allowBurst(), spec.capacity(), emissionIntervalNanos, spec.strictMath());
        this.strictMath = spec.strictMath();
        this.timeSource = spec.timeSource();
    }

    @Override
    public Permit tryAcquire(final long tokens, final long nowNanos) {
        if (emissionIntervalNanos == 0) {
            return new SimplePermit(true, tokens, Long.MAX_VALUE, 0);
        }

        final long costNanos = GcraMath.calculateCostNanos(tokens, emissionIntervalNanos, strictMath);

        while (true) {
            final long currentTat = state.getTat();
            final long baseTime = Math.max(currentTat, nowNanos);
            final long potentialTat = GcraMath.calculatePotentialTat(baseTime, costNanos, strictMath);
            final long allowedLimit = nowNanos + burstOffsetNanos;

            if (potentialTat <= allowedLimit) {
                if (state.compareAndSetTat(currentTat, potentialTat)) {
                    final long remainingTime = allowedLimit - potentialTat;
                    final long remainingTokens = remainingTime / emissionIntervalNanos;
                    return new SimplePermit(true, tokens, remainingTokens, 0);
                }
            } else {
                final long retryAfter = potentialTat - burstOffsetNanos - nowNanos;
                return new SimplePermit(false, tokens, 0, retryAfter);
            }
        }
    }

    @Override
    public LimiterStats snapshot() {
        final long now = timeSource.nanoTime();
        final long cTat = state.getTat();
        final long base = Math.max(cTat, now);

        final long availTime = (now + burstOffsetNanos) - base;
        final long avail = (emissionIntervalNanos == 0) ? Long.MAX_VALUE : Math.max(0, availTime / emissionIntervalNanos);

        return new LimiterStats(capacity, avail, refillTokens, Duration.ofNanos(refillPeriodNanos));
    }
}
