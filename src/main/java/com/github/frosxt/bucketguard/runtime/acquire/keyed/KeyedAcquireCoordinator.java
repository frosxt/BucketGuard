package com.github.frosxt.bucketguard.runtime.acquire.keyed;

import com.github.frosxt.bucketguard.api.Permit;
import com.github.frosxt.bucketguard.api.spec.TokenBucketSpec;
import com.github.frosxt.bucketguard.runtime.bucket.Bucket;
import com.github.frosxt.bucketguard.runtime.bucket.BucketFactory;
import com.github.frosxt.bucketguard.runtime.bucket.SimplePermit;
import com.github.frosxt.bucketguard.runtime.key.store.KeyedStore;

import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.locks.LockSupport;

public final class KeyedAcquireCoordinator<K> {
    private final KeyedStore<K> store;
    private final TokenBucketSpec bucketSpec;

    public KeyedAcquireCoordinator(final KeyedStore<K> store, final TokenBucketSpec bucketSpec) {
        this.store = store;
        this.bucketSpec = bucketSpec;
    }

    /**
     * Attempts to acquire tokens for a key immediately.
     *
     * @param key    the key
     * @param tokens number of tokens
     * @return result permit
     */
    public Permit tryAcquire(final K key, final long tokens) {
        Objects.requireNonNull(key, "key");
        if (tokens < 1) {
            throw new IllegalArgumentException("tokens must be >= 1");
        }

        if (!bucketSpec.allowBurst() && tokens > 1) {
            return new SimplePermit(false, tokens, 0, bucketSpec.refillPeriod().toNanos());
        }

        final Bucket bucket = store.getOrCreate(key, () -> BucketFactory.create(bucketSpec));
        return bucket.tryAcquire(tokens, bucketSpec.timeSource().nanoTime());
    }

    /**
     * Acquires tokens for a key, blocking if necessary.
     *
     * @param key    the key
     * @param tokens number of tokens
     * @return granted permit
     * @throws InterruptedException if interrupted
     */
    public Permit acquire(final K key, final long tokens) throws InterruptedException {
        Objects.requireNonNull(key, "key");
        if (tokens < 1) {
            throw new IllegalArgumentException("tokens must be >= 1");
        }

        if (!bucketSpec.allowBurst() && tokens > 1) {
            long remaining = 0;
            for (long i = 0; i < tokens; i++) {
                final Permit p = acquireSingle(key);
                remaining = p.remainingTokens();
            }
            return new SimplePermit(true, tokens, remaining, 0);
        }

        return acquireSingle(key, tokens);
    }

    private Permit acquireSingle(final K key) throws InterruptedException {
        return acquireSingle(key, 1);
    }

    private Permit acquireSingle(final K key, final long tokens) throws InterruptedException {
        final Bucket bucket = store.getOrCreate(key, () -> BucketFactory.create(bucketSpec));

        while (true) {
            final long now = bucketSpec.timeSource().nanoTime();
            final Permit permit = bucket.tryAcquire(tokens, now);
            if (permit.granted()) {
                return permit;
            }

            if (Thread.interrupted()) {
                throw new InterruptedException();
            }

            final long waitNanos = permit.retryAfterNanos();
            if (waitNanos > 0) {
                if (waitNanos > 1000) {
                    LockSupport.parkNanos(waitNanos);
                } else {
                    Thread.onSpinWait();
                }
            }
        }
    }

    /**
     * Acquires tokens for a key asynchronously.
     *
     * @param key      the key
     * @param tokens   number of tokens
     * @param executor executor for scheduling
     * @return completion stage
     */
    public CompletionStage<Permit> acquireAsync(final K key, final long tokens, final Executor executor) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(executor, "executor");
        if (tokens < 1) {
            throw new IllegalArgumentException("tokens must be >= 1");
        }

        if (!bucketSpec.allowBurst() && tokens > 1) {
            return chainAsyncAcquisitions(key, tokens, executor);
        }

        final CompletableFuture<Permit> future = new CompletableFuture<>();
        scheduleAsync(key, tokens, executor, future);
        return future;
    }

    private CompletionStage<Permit> chainAsyncAcquisitions(final K key, final long tokens, final Executor executor) {
        if (tokens > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("tokens too large for async chaining");
        }
        CompletionStage<Permit> stage = acquireAsyncSingle(key, executor);
        for (int i = 1; i < tokens; i++) {
            stage = stage.thenCompose(permit -> acquireAsyncSingle(key, executor));
        }
        return stage.thenApply(lastPermit -> new SimplePermit(true, tokens, lastPermit.remainingTokens(), 0));
    }

    private CompletionStage<Permit> acquireAsyncSingle(final K key, final Executor executor) {
        final CompletableFuture<Permit> future = new CompletableFuture<>();
        scheduleAsync(key, 1, executor, future);
        return future;
    }

    private void scheduleAsync(final K key, final long tokens, final Executor executor, final CompletableFuture<Permit> future) {
        try {
            final Bucket bucket = store.getOrCreate(key, () -> BucketFactory.create(bucketSpec));
            final long now = bucketSpec.timeSource().nanoTime();
            final Permit permit = bucket.tryAcquire(tokens, now);

            if (permit.granted()) {
                future.complete(permit);
            } else {
                final long delayNanos = permit.retryAfterNanos();

                if (executor instanceof final ScheduledExecutorService scheduler) {
                    scheduler.schedule(() -> scheduleAsync(key, tokens, executor, future), delayNanos, TimeUnit.NANOSECONDS);
                } else {
                    future.completeExceptionally(new IllegalArgumentException("Async acquisition with delay requires a ScheduledExecutorService"));
                }
            }
        } catch (final Exception e) {
            future.completeExceptionally(e);
        }
    }
}
