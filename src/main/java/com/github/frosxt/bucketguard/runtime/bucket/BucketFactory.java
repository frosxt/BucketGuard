package com.github.frosxt.bucketguard.runtime.bucket;

import com.github.frosxt.bucketguard.api.spec.ContentionStrategy;
import com.github.frosxt.bucketguard.api.spec.TokenBucketSpec;
import com.github.frosxt.bucketguard.runtime.bucket.atomic.AtomicBucket;
import com.github.frosxt.bucketguard.runtime.bucket.striped.StripedBucket;

/**
 * Factory for creating {@link Bucket} instances based on configuration and
 * heuristics.
 */
public final class BucketFactory {

    private BucketFactory() {
        throw new UnsupportedOperationException("This class cannot be instantiated!");
    }

    /**
     * Creates a bucket based on the provided spec.
     * 
     * @param spec config spec, not null
     * @return a new Bucket instance
     */
    public static Bucket create(final TokenBucketSpec spec) {
        ContentionStrategy strategy = spec.contentionStrategy();

        if (strategy == ContentionStrategy.AUTO) {
            strategy = ContentionStrategy.ATOMIC;
        }

        if (strategy == ContentionStrategy.STRIPED) {
            int stripes = Runtime.getRuntime().availableProcessors() * 4;
            if (stripes > 64) {
                stripes = 64;
            }
            if (stripes < 2) {
                stripes = 2;
            }

            final long refill = spec.refillTokens();
            final long capacity = spec.capacity();

            final long limit = Math.min(refill, capacity);

            if (limit < stripes) {
                final long maxStripes = Long.highestOneBit(limit);
                if (maxStripes < 2) {
                    return new AtomicBucket(spec);
                }
                if (stripes > maxStripes) {
                    stripes = (int) maxStripes;
                }
            }

            if (Integer.bitCount(stripes) != 1) {
                stripes = Integer.highestOneBit(stripes);
            }

            return new StripedBucket(spec, stripes);
        }

        return new AtomicBucket(spec);
    }
}
