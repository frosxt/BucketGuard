package com.github.frosxt.bucketguard.api;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;

/**
 * A token bucket rate limiter.
 * <p>
 * Implementations must be thread-safe.
 */
public interface RateLimiter {

    /**
     * Attempts to acquire 1 token immediately.
     *
     * @return a Permit indicating success or failure/retry-after.
     */
    Permit tryAcquire();

    /**
     * Attempts to acquire {@code tokens} immediately.
     *
     * @param tokens number of tokens to acquire, must be >= 1.
     * @return a Permit indicating success or failure/retry-after.
     * @throws IllegalArgumentException if tokens &lt; 1.
     */
    Permit tryAcquire(long tokens);

    /**
     * Acquires 1 token, blocking until available.
     *
     * @return a granted Permit.
     * @throws InterruptedException if interrupted while waiting.
     */
    Permit acquire() throws InterruptedException;

    /**
     * Acquires {@code tokens}, blocking until available.
     *
     * @param tokens number of tokens to acquire, must be >= 1.
     * @return a granted Permit.
     * @throws IllegalArgumentException if tokens &lt; 1.
     * @throws InterruptedException     if interrupted while waiting.
     */
    Permit acquire(long tokens) throws InterruptedException;

    /**
     * Asynchronously acquires 1 token.
     * <p>
     * If delayed retries are required, the provided executor <b>MUST</b> be an
     * instance of {@link java.util.concurrent.ScheduledExecutorService}.
     *
     * @param executor the executor to use for scheduling the completion.
     * @return a CompletionStage that completes with a granted Permit.
     */
    CompletionStage<Permit> acquireAsync(Executor executor);

    /**
     * Asynchronously acquires {@code tokens}.
     * <p>
     * If delayed retries are required, the provided executor <b>MUST</b> be an
     * instance of
     * {@link java.util.concurrent.ScheduledExecutorService}.
     *
     * @param tokens   number of tokens
     * @param executor executor for async completion
     * @return completion stage
     * @throws IllegalArgumentException if tokens &lt; 1 or executor is not valid
     *                                  for
     *                                  the operation
     */
    CompletionStage<Permit> acquireAsync(long tokens, Executor executor);

    /**
     * @return a snapshot of the limiter's current statistics.
     */
    LimiterStats snapshot();
}
