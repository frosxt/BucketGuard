package com.github.frosxt.bucketguard.runtime.bucket;

import com.github.frosxt.bucketguard.api.spec.TokenBucketSpec;

public abstract class AbstractBucket implements Bucket {
    protected final long capacity;
    protected final long refillTokens;
    protected final long refillPeriodNanos;

    protected AbstractBucket(final TokenBucketSpec spec) {
        this.capacity = spec.capacity();
        this.refillTokens = spec.refillTokens();
        this.refillPeriodNanos = spec.refillPeriod().toNanos();
    }

    protected long calculateRefill(final long elapsedNanos) {
        if (elapsedNanos <= 0) {
            return 0;
        }
        return (elapsedNanos * refillTokens) / refillPeriodNanos;
    }

    protected long calculateConsumedNanos(final long addedTokens) {
        return (addedTokens * refillPeriodNanos) / refillTokens;
    }

    protected long calculateRetryAfter(final long deficit) {
        final long numerator = (deficit * refillPeriodNanos);
        return (numerator + refillTokens - 1) / refillTokens;
    }
}
