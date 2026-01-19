package com.github.frosxt.bucketguard.api.spec.builder;

import com.github.frosxt.bucketguard.api.spec.ContentionStrategy;
import com.github.frosxt.bucketguard.api.spec.TokenBucketSpec;
import com.github.frosxt.bucketguard.api.time.TimeSource;

import java.time.Duration;

/**
 * Builder for {@link TokenBucketSpec}.
 */
public final class TokenBucketSpecBuilder {
    private long capacity = 100;
    private long refillTokens = 10;
    private Duration refillPeriod = Duration.ofSeconds(1);
    private ContentionStrategy contentionStrategy = ContentionStrategy.AUTO;
    private TimeSource timeSource = TimeSource.system();
    private boolean strictMath = true;
    private boolean allowBurst = true;

    public long getCapacity() {
        return capacity;
    }

    public long getRefillTokens() {
        return refillTokens;
    }

    public Duration getRefillPeriod() {
        return refillPeriod;
    }

    public ContentionStrategy getContentionStrategy() {
        return contentionStrategy;
    }

    public TimeSource getTimeSource() {
        return timeSource;
    }

    public boolean isStrictMath() {
        return strictMath;
    }

    public boolean isAllowBurst() {
        return allowBurst;
    }

    /**
     * Sets the maximum capacity of tokens.
     * 
     * @param capacity must be >= 1.
     * @return this builder.
     */
    public TokenBucketSpecBuilder capacity(final long capacity) {
        this.capacity = capacity;
        return this;
    }

    /**
     * Sets the number of tokens to refill per period.
     * 
     * @param refillTokens must be >= 1.
     * @return this builder.
     */
    public TokenBucketSpecBuilder refillTokens(final long refillTokens) {
        this.refillTokens = refillTokens;
        return this;
    }

    /**
     * Sets the refill period.
     * 
     * @param refillPeriod must be > 0.
     * @return this builder.
     */
    public TokenBucketSpecBuilder refillPeriod(final Duration refillPeriod) {
        this.refillPeriod = refillPeriod;
        return this;
    }

    /**
     * Sets the contention strategy.
     * 
     * @param contentionStrategy must not be null.
     * @return this builder.
     */
    public TokenBucketSpecBuilder contentionStrategy(final ContentionStrategy contentionStrategy) {
        this.contentionStrategy = contentionStrategy;
        return this;
    }

    /**
     * Sets the time source.
     * 
     * @param timeSource must not be null.
     * @return this builder.
     */
    public TokenBucketSpecBuilder timeSource(final TimeSource timeSource) {
        this.timeSource = timeSource;
        return this;
    }

    /**
     * Sets whether to use strict math (overflow safe).
     * 
     * @param strictMath true to enable.
     * @return this builder.
     */
    public TokenBucketSpecBuilder strictMath(final boolean strictMath) {
        this.strictMath = strictMath;
        return this;
    }

    /**
     * Sets whether to allow bursts.
     * 
     * @param allowBurst true to allow.
     * @return this builder.
     */
    public TokenBucketSpecBuilder allowBurst(final boolean allowBurst) {
        this.allowBurst = allowBurst;
        return this;
    }

    /**
     * Builds the spec.
     * 
     * @return a new {@link TokenBucketSpec}.
     */
    public TokenBucketSpec build() {
        return new TokenBucketSpec(this);
    }
}
