package com.github.frosxt.bucketguard.runtime.bucket;

import com.github.frosxt.bucketguard.api.LimiterStats;
import com.github.frosxt.bucketguard.api.Permit;

/**
 * Internal abstraction for a token bucket.
 */
public interface Bucket {

    /**
     * Attempts to acquire the given number of tokens.
     *
     * @param tokens   tokens to acquire
     * @param nowNanos current time in nanoseconds
     * @return a Permit result
     */
    Permit tryAcquire(long tokens, long nowNanos);

    /**
     * @return current stats snapshot
     */
    LimiterStats snapshot();
}
