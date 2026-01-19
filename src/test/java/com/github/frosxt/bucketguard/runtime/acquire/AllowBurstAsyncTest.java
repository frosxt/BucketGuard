package com.github.frosxt.bucketguard.runtime.acquire;

import com.github.frosxt.bucketguard.api.KeyedRateLimiter;
import com.github.frosxt.bucketguard.api.Permit;
import com.github.frosxt.bucketguard.api.RateLimiter;
import com.github.frosxt.bucketguard.api.factory.BucketGuards;
import com.github.frosxt.bucketguard.api.spec.KeyedStoreSpec;
import com.github.frosxt.bucketguard.api.spec.TokenBucketSpec;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class AllowBurstAsyncTest {

    @Test
    void allowBurstFalseAcquireAsyncMultipleTokensEventuallyCompletes() throws Exception {
        final TokenBucketSpec spec = TokenBucketSpec.builder()
                .capacity(1)
                .refillTokens(1)
                .refillPeriod(Duration.ofMillis(50))
                .allowBurst(false)
                .build();

        final RateLimiter limiter = BucketGuards.tokenBucket(spec);
        final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        try {
            final long start = System.currentTimeMillis();

            // Request 3 tokens async; with refill at 50ms per token, should complete in
            // ~100-150ms
            final Permit permit = limiter.acquireAsync(3, scheduler)
                    .toCompletableFuture()
                    .get(500, TimeUnit.MILLISECONDS);

            final long elapsed = System.currentTimeMillis() - start;

            assertTrue(permit.granted());
            assertEquals(3, permit.tokensRequested());
            // Should take at least 100ms (2 additional tokens after first)
            assertTrue(elapsed >= 100, "Expected at least 100ms but was " + elapsed);
        } finally {
            scheduler.shutdownNow();
        }
    }

    @Test
    void allowBurstFalseKeyedAcquireAsyncMultipleTokensEventuallyCompletes() throws Exception {
        final TokenBucketSpec spec = TokenBucketSpec.builder()
                .capacity(1)
                .refillTokens(1)
                .refillPeriod(Duration.ofMillis(50))
                .allowBurst(false)
                .build();

        try (final KeyedRateLimiter<String> limiter = BucketGuards.keyedTokenBucket(spec, KeyedStoreSpec.<String>builder().build())) {
            final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
            try {
                final long start = System.currentTimeMillis();

                final Permit permit = limiter.acquireAsync("testKey", 3, scheduler)
                        .toCompletableFuture()
                        .get(500, TimeUnit.MILLISECONDS);

                final long elapsed = System.currentTimeMillis() - start;

                assertTrue(permit.granted());
                assertEquals(3, permit.tokensRequested());
                assertTrue(elapsed >= 100, "Expected at least 100ms but was " + elapsed);
            } finally {
                scheduler.shutdownNow();
            }
        }
    }

    @Test
    void allowBurstFalseTryAcquireMultiTokensDenied() {
        final TokenBucketSpec spec = TokenBucketSpec.builder()
                .capacity(10)
                .refillTokens(1)
                .refillPeriod(Duration.ofSeconds(1))
                .allowBurst(false)
                .build();

        final RateLimiter limiter = BucketGuards.tokenBucket(spec);

        // Single token should succeed
        assertTrue(limiter.tryAcquire().granted());

        // Multi-token should be denied
        assertFalse(limiter.tryAcquire(3).granted());
    }

    @Test
    void allowBurstFalseTryAcquireKeyedMultiTokensDenied() {
        final TokenBucketSpec spec = TokenBucketSpec.builder()
                .capacity(10)
                .refillTokens(1)
                .refillPeriod(Duration.ofSeconds(1))
                .allowBurst(false)
                .build();

        try (final KeyedRateLimiter<String> limiter = BucketGuards.keyedTokenBucket(spec, KeyedStoreSpec.<String>builder().build())) {
            // Single token should succeed
            assertTrue(limiter.tryAcquire("key").granted());

            // Multi-token should be denied
            assertFalse(limiter.tryAcquire("key", 3).granted());
        }
    }

    @Test
    void allowBurstTrueMultiTokenAsyncWorks() throws Exception {
        final TokenBucketSpec spec = TokenBucketSpec.builder()
                .capacity(10)
                .refillTokens(10)
                .refillPeriod(Duration.ofSeconds(1))
                .allowBurst(true)
                .build();

        final RateLimiter limiter = BucketGuards.tokenBucket(spec);
        final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        try {
            // Should complete immediately since capacity allows burst
            final Permit permit = limiter.acquireAsync(5, scheduler)
                    .toCompletableFuture()
                    .get(100, TimeUnit.MILLISECONDS);

            assertTrue(permit.granted());
            assertEquals(5, permit.tokensRequested());
        } finally {
            scheduler.shutdownNow();
        }
    }
}
