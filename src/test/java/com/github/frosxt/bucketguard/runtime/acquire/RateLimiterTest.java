package com.github.frosxt.bucketguard.runtime.acquire;

import com.github.frosxt.bucketguard.api.Permit;
import com.github.frosxt.bucketguard.api.RateLimiter;
import com.github.frosxt.bucketguard.api.factory.BucketGuards;
import com.github.frosxt.bucketguard.api.spec.TokenBucketSpec;
import com.github.frosxt.bucketguard.test.FakeTimeSource;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RateLimiterTest {

    @Test
    void testSimpleFlow() {
        final FakeTimeSource time = new FakeTimeSource();
        final TokenBucketSpec spec = TokenBucketSpec.builder()
                .capacity(10)
                .refillTokens(10)
                .refillPeriod(Duration.ofSeconds(1)) // 10/sec
                .timeSource(time)
                .build();

        final RateLimiter limiter = BucketGuards.tokenBucket(spec);

        assertTrue(limiter.tryAcquire(5).granted());
        assertTrue(limiter.tryAcquire(5).granted());
        assertFalse(limiter.tryAcquire(1).granted());

        time.advance(Duration.ofMillis(100).toNanos()); // 1s / 10 = 0.1s for 1 token.
        assertTrue(limiter.tryAcquire(1).granted());
    }

    @Test
    void testAcquireAsync() throws InterruptedException, ExecutionException {
        // Use real time for async test or just check immediate grant
        final TokenBucketSpec spec = TokenBucketSpec.builder()
                .capacity(10)
                .refillTokens(10)
                .refillPeriod(Duration.ofSeconds(1))
                .build();

        final RateLimiter limiter = BucketGuards.tokenBucket(spec);

        final Permit p = limiter.acquireAsync(Executors.newSingleThreadExecutor()).toCompletableFuture().get();
        assertTrue(p.granted());

        // Drain
        for (int i = 0; i < 9; i++) {
            limiter.tryAcquire();
        }
    }
}
