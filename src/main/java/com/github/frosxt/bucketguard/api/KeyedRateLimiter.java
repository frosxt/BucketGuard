package com.github.frosxt.bucketguard.api;

import java.time.Duration;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;

/**
 * A rate limiter that manages separate buckets per key.
 * <p>
 * Implementations must be thread-safe.
 * <p>
 * This interface extends {@link AutoCloseable} to support lifecycle management
 * for scheduled maintenance tasks.
 *
 * @param <K> the type of key
 */
public interface KeyedRateLimiter<K> extends AutoCloseable {

    /**
     * Attempts to acquire 1 token for the given key immediately.
     *
     * @param key the key, must not be null.
     * @return a Permit indicating success or failure/retry-after.
     * @throws IllegalArgumentException if key is null.
     */
    Permit tryAcquire(K key);

    /**
     * Attempts to acquire {@code tokens} for the given key immediately.
     *
     * @param key    the key, must not be null.
     * @param tokens number of tokens to acquire, must be >= 1.
     * @return a Permit indicating success or failure/retry-after.
     * @throws IllegalArgumentException if key is null or tokens &lt; 1.
     */
    Permit tryAcquire(K key, long tokens);

    /**
     * Acquires 1 token for the given key, blocking until available.
     *
     * @param key the key, must not be null.
     * @return a granted Permit.
     * @throws IllegalArgumentException if key is null.
     * @throws InterruptedException     if interrupted while waiting.
     */
    Permit acquire(K key) throws InterruptedException;

    /**
     * Acquires {@code tokens} for the given key, blocking until available.
     *
     * @param key    the key, must not be null.
     * @param tokens number of tokens to acquire, must be >= 1.
     * @return a granted Permit.
     * @throws IllegalArgumentException if key is null or tokens &lt; 1.
     * @throws InterruptedException     if interrupted while waiting.
     */
    Permit acquire(K key, long tokens) throws InterruptedException;

    /**
     * Asynchronously acquires 1 token for the given key.
     * <p>
     * If delayed retries are required, the provided executor <b>MUST</b> be an
     * instance of {@link ScheduledExecutorService}.
     *
     * @param key      the key, must not be null.
     * @param executor the executor to use for scheduling the completion.
     * @return a CompletionStage that completes with a granted Permit.
     * @throws IllegalArgumentException if key is null or executor is invalid.
     */
    CompletionStage<Permit> acquireAsync(K key, Executor executor);

    /**
     * Asynchronously acquires {@code tokens} for the given key.
     * <p>
     * If delayed retries are required, the provided executor <b>MUST</b> be an
     * instance of {@link ScheduledExecutorService}.
     *
     * @param key      the key, must not be null.
     * @param tokens   number of tokens to acquire, must be >= 1.
     * @param executor the executor to use for scheduling the completion.
     * @return a CompletionStage that completes with a granted Permit.
     * @throws IllegalArgumentException if key is null, tokens &lt; 1, or executor
     *                                  is
     *                                  invalid.
     */
    CompletionStage<Permit> acquireAsync(K key, long tokens, Executor executor);

    /**
     * @param key the key to get stats for.
     * @return a snapshot of the limiter's stats for the specific key, or empty
     *         stats if unknown.
     */
    LimiterStats snapshot(K key);

    /**
     * @return a snapshot of the keyed limiter's overall state.
     *         Note: This may be expensive to compute.
     */
    KeyedLimiterStats snapshotAll();

    /**
     * Returns a snapshot of stats for up to {@code limit} keys.
     * 
     * @param limit maximum number of keys to include in the snapshot
     * @return a map of stats per key
     */
    KeyedLimiterStats snapshotSample(int limit);

    /**
     * Prunes expired keys from the storage.
     * <p>
     * This method is useful when using {@code EXPIRE_AFTER_ACCESS} policy,
     * as eviction is otherwise passive (occurring only on map access).
     * users can schedule this method to run periodically to ensure
     * strictly expired keys are removed even if the map is idle.
     * <p>
     * Default implementation is a no-op; implementations may override.
     */
    default void prune() {
    }

    /**
     * Starts scheduled maintenance using the provided scheduler.
     * <p>
     * This schedules {@link #prune()} to run at a fixed delay.
     * <p>
     * Default implementation is a no-op; implementations may override.
     * <p>
     * This method is idempotent; calling it multiple times has no additional
     * effect.
     *
     * @param scheduler the scheduler to use for maintenance tasks, not null.
     * @throws NullPointerException if scheduler is null.
     */
    default void startMaintenance(final ScheduledExecutorService scheduler) {
        startMaintenance(scheduler, Duration.ofSeconds(5));
    }

    /**
     * Starts scheduled maintenance using the provided scheduler and period.
     * <p>
     * This schedules {@link #prune()} to run at a fixed delay.
     * <p>
     * Default implementation is a no-op; implementations may override.
     * <p>
     * This method is idempotent; calling it multiple times has no additional
     * effect.
     *
     * @param scheduler the scheduler to use for maintenance tasks, not null.
     * @param period    the delay between prune executions, not null.
     * @throws NullPointerException if scheduler or period is null.
     */
    default void startMaintenance(final ScheduledExecutorService scheduler, final Duration period) {
    }

    /**
     * @return {@code true} if scheduled maintenance is currently running.
     */
    default boolean isMaintenanceRunning() {
        return false;
    }

    /**
     * Closes this rate limiter and cancels any scheduled maintenance tasks.
     * <p>
     * After closing, the limiter may still be used for rate limiting, but
     * maintenance will no longer run. This does not shut down the scheduler.
     * <p>
     * Default implementation is a no-op; implementations may override.
     */
    @Override
    default void close() {
    }
}
