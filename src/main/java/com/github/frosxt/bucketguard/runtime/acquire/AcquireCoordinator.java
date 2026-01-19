package com.github.frosxt.bucketguard.runtime.acquire;

import com.github.frosxt.bucketguard.api.Permit;
import com.github.frosxt.bucketguard.api.spec.TokenBucketSpec;
import com.github.frosxt.bucketguard.runtime.bucket.Bucket;
import com.github.frosxt.bucketguard.runtime.bucket.SimplePermit;

import java.util.concurrent.locks.LockSupport;

/**
 * Coordinates blocking and non-blocking acquisition attempts.
 */
public final class AcquireCoordinator {
    private final Bucket bucket;
    private final TokenBucketSpec spec;

    public AcquireCoordinator(final Bucket bucket, final TokenBucketSpec spec) {
        this.bucket = bucket;
        this.spec = spec;
    }

    /**
     * Attempts to acquire tokens immediately.
     *
     * @param tokens number of tokens
     * @return result permit
     */
    public Permit tryAcquire(final long tokens) {
        if (tokens < 1) {
            throw new IllegalArgumentException("tokens must be >= 1");
        }

        if (!spec.allowBurst() && tokens > 1) {
            return new SimplePermit(false, tokens, 0, spec.refillPeriod().toNanos());
        }

        return bucket.tryAcquire(tokens, spec.timeSource().nanoTime());
    }

    /**
     * Acquires tokens, blocking if necessary.
     *
     * @param tokens number of tokens
     * @return granted permit
     * @throws InterruptedException if interrupted
     */
    public Permit acquire(final long tokens) throws InterruptedException {
        if (tokens < 1) {
            throw new IllegalArgumentException("tokens must be >= 1");
        }

        if (!spec.allowBurst() && tokens > 1) {
            long remaining = 0;
            for (long i = 0; i < tokens; i++) {
                final Permit p = acquireSingle();
                remaining = p.remainingTokens();
            }
            return new SimplePermit(true, tokens, remaining, 0);
        }

        return acquireSingle(tokens);
    }

    private Permit acquireSingle() throws InterruptedException {
        return acquireSingle(1);
    }

    private Permit acquireSingle(final long tokens) throws InterruptedException {
        while (true) {
            final long now = spec.timeSource().nanoTime();
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
}
