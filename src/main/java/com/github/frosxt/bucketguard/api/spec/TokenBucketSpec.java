package com.github.frosxt.bucketguard.api.spec;

import com.github.frosxt.bucketguard.api.spec.builder.TokenBucketSpecBuilder;
import com.github.frosxt.bucketguard.api.time.TimeSource;

import java.time.Duration;
import java.util.Objects;

/**
 * Immutable configuration for a token bucket.
 */
public final class TokenBucketSpec {
    private final long capacity;
    private final long refillTokens;
    private final Duration refillPeriod;
    private final ContentionStrategy contentionStrategy;
    private final TimeSource timeSource;
    private final boolean strictMath;
    private final boolean allowBurst;

    public TokenBucketSpec(final TokenBucketSpecBuilder builder) {
        this.capacity = builder.getCapacity();
        this.refillTokens = builder.getRefillTokens();
        this.refillPeriod = builder.getRefillPeriod();
        this.contentionStrategy = builder.getContentionStrategy();
        this.timeSource = builder.getTimeSource();
        this.strictMath = builder.isStrictMath();
        this.allowBurst = builder.isAllowBurst();
        validate();
    }

    private void validate() {
        if (capacity < 1) {
            throw new IllegalArgumentException("capacity must be >= 1");
        }
        if (refillTokens < 1) {
            throw new IllegalArgumentException("refillTokens must be >= 1");
        }
        if (refillPeriod == null || refillPeriod.isZero() || refillPeriod.isNegative()) {
            throw new IllegalArgumentException("refillPeriod must be > 0");
        }
        try {
            refillPeriod.toNanos();
        } catch (final ArithmeticException e) {
            throw new IllegalArgumentException("refillPeriod overflow", e);
        }

        Objects.requireNonNull(contentionStrategy, "contentionStrategy");
        Objects.requireNonNull(timeSource, "timeSource");
    }

    public static TokenBucketSpecBuilder builder() {
        return new TokenBucketSpecBuilder();
    }

    /**
     * @return the maximum capacity of tokens in the bucket.
     */
    public long capacity() {
        return capacity;
    }

    /**
     * @return the number of tokens to refill per period.
     */
    public long refillTokens() {
        return refillTokens;
    }

    /**
     * @return the period over which tokens are refilled.
     */
    public Duration refillPeriod() {
        return refillPeriod;
    }

    /**
     * @return the strategy for handling thread contention.
     */
    public ContentionStrategy contentionStrategy() {
        return contentionStrategy;
    }

    /**
     * @return the time source used for time calculations.
     */
    public TimeSource timeSource() {
        return timeSource;
    }

    /**
     * @return true if strict overflow checks are enabled for token math.
     */
    public boolean strictMath() {
        return strictMath;
    }

    /**
     * @return true if bursts up to capacity are allowed (Standard Token Bucket behavior).
     *         If false, behavior may be smoothed/throttled.
     */
    public boolean allowBurst() {
        return allowBurst;
    }
}
