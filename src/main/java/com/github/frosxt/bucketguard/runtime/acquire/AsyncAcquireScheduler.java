package com.github.frosxt.bucketguard.runtime.acquire;

import com.github.frosxt.bucketguard.api.Permit;
import com.github.frosxt.bucketguard.api.spec.TokenBucketSpec;
import com.github.frosxt.bucketguard.runtime.bucket.Bucket;
import com.github.frosxt.bucketguard.runtime.bucket.SimplePermit;

import java.util.Objects;
import java.util.concurrent.*;

/**
 * Handles asynchronous acquisition scheduling.
 */
public final class AsyncAcquireScheduler {
    private final Bucket bucket;
    private final TokenBucketSpec spec;

    public AsyncAcquireScheduler(final Bucket bucket, final TokenBucketSpec spec) {
        this.bucket = bucket;
        this.spec = spec;
    }

    /**
     * Acquires tokens asynchronously.
     *
     * @param tokens   number of tokens
     * @param executor executor for scheduling
     * @return completion stage
     */
    public CompletionStage<Permit> acquireAsync(final long tokens, final Executor executor) {
        if (tokens < 1) {
            throw new IllegalArgumentException("tokens must be >= 1");
        }
        if (tokens > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("tokens too large for async chaining");
        }
        Objects.requireNonNull(executor, "executor");

        if (!spec.allowBurst() && tokens > 1) {
            return chainAsyncAcquisitions(tokens, executor);
        }

        final CompletableFuture<Permit> future = new CompletableFuture<>();
        scheduleAsync(tokens, executor, future);
        return future;
    }

    private CompletionStage<Permit> chainAsyncAcquisitions(final long tokens, final Executor executor) {
        CompletionStage<Permit> stage = acquireAsyncSingle(executor);
        for (int i = 1; i < tokens; i++) {
            stage = stage.thenCompose(permit -> acquireAsyncSingle(executor));
        }
        return stage.thenApply(lastPermit -> new SimplePermit(true, tokens, lastPermit.remainingTokens(), 0));
    }

    private CompletionStage<Permit> acquireAsyncSingle(final Executor executor) {
        final CompletableFuture<Permit> future = new CompletableFuture<>();
        scheduleAsync(1, executor, future);
        return future;
    }

    private void scheduleAsync(final long tokens, final Executor executor, final CompletableFuture<Permit> future) {
        try {
            final long now = spec.timeSource().nanoTime();
            final Permit permit = bucket.tryAcquire(tokens, now);
            if (permit.granted()) {
                future.complete(permit);
            } else {
                final long delayNanos = permit.retryAfterNanos();

                if (executor instanceof final ScheduledExecutorService scheduler) {
                    scheduler.schedule(() -> scheduleAsync(tokens, executor, future), delayNanos, TimeUnit.NANOSECONDS);
                } else {
                    future.completeExceptionally(new IllegalArgumentException("Async acquisition with delay requires a ScheduledExecutorService"));
                }
            }
        } catch (final Exception e) {
            future.completeExceptionally(e);
        }
    }
}
