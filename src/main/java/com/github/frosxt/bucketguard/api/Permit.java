package com.github.frosxt.bucketguard.api;

import java.time.Duration;

/**
 * Represents the result of a token acquisition attempt.
 * <p>
 * This class is designed to be low-allocation on the hot path.
 */
public interface Permit {

    /**
     * @return true if the permit was acquired, false otherwise.
     */
    boolean granted();

    /**
     * @return the number of tokens requested in this attempt.
     */
    long tokensRequested();

    /**
     * @return the approximate number of tokens remaining in the bucket after this attempt.
     */
    long remainingTokens();

    /**
     * @return the number of nanoseconds to wait before a retry might succeed.
     *         Returns 0 if granted.
     */
    long retryAfterNanos();

    /**
     * Convenience method to get retry-after as a Duration.
     * <p>
     * Note: This method may allocate. Prefer {@link #retryAfterNanos()} in hot
     * paths.
     *
     * @return the duration to wait before retrying.
     */
    default Duration retryAfter() {
        return Duration.ofNanos(retryAfterNanos());
    }
}
