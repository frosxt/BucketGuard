package com.github.frosxt.bucketguard.runtime.limiter.global;

import com.github.frosxt.bucketguard.api.LimiterStats;
import com.github.frosxt.bucketguard.api.Permit;
import com.github.frosxt.bucketguard.api.RateLimiter;
import com.github.frosxt.bucketguard.api.spec.TokenBucketSpec;
import com.github.frosxt.bucketguard.runtime.acquire.AcquireCoordinator;
import com.github.frosxt.bucketguard.runtime.acquire.AsyncAcquireScheduler;
import com.github.frosxt.bucketguard.runtime.bucket.Bucket;
import com.github.frosxt.bucketguard.runtime.bucket.BucketFactory;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;

/**
 * Standard implementation of RateLimiter.
 */
public class StandardRateLimiter implements RateLimiter {
    private final Bucket bucket;
    private final AcquireCoordinator acquireCoordinator;
    private final AsyncAcquireScheduler asyncScheduler;

    /**
     * Creates a new StandardRateLimiter.
     * 
     * @param spec configuration spec, not null
     */
    public StandardRateLimiter(final TokenBucketSpec spec) {
        this.bucket = BucketFactory.create(spec);
        this.acquireCoordinator = new AcquireCoordinator(bucket, spec);
        this.asyncScheduler = new AsyncAcquireScheduler(bucket, spec);
    }

    @Override
    public Permit tryAcquire() {
        return tryAcquire(1);
    }

    @Override
    public Permit tryAcquire(final long tokens) {
        return acquireCoordinator.tryAcquire(tokens);
    }

    @Override
    public Permit acquire() throws InterruptedException {
        return acquire(1);
    }

    @Override
    public Permit acquire(final long tokens) throws InterruptedException {
        return acquireCoordinator.acquire(tokens);
    }

    @Override
    public CompletionStage<Permit> acquireAsync(final Executor executor) {
        return acquireAsync(1, executor);
    }

    @Override
    public CompletionStage<Permit> acquireAsync(final long tokens, final Executor executor) {
        return asyncScheduler.acquireAsync(tokens, executor);
    }

    @Override
    public LimiterStats snapshot() {
        return bucket.snapshot();
    }
}
