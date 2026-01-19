package com.github.frosxt.bucketguard.runtime.acquire;

import com.github.frosxt.bucketguard.api.RateLimiter;
import com.github.frosxt.bucketguard.api.factory.BucketGuards;
import com.github.frosxt.bucketguard.api.spec.ContentionStrategy;
import com.github.frosxt.bucketguard.api.spec.TokenBucketSpec;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ConcurrencyTest {

    @Test
    void testHighContentionAtomic() throws InterruptedException {
        runContentionTest(ContentionStrategy.ATOMIC);
    }

    @Test
    void testHighContentionStriped() throws InterruptedException {
        runContentionTest(ContentionStrategy.STRIPED);
    }

    private void runContentionTest(final ContentionStrategy strategy) throws InterruptedException {
        final int threads = 16;
        final int requestsPerThread = 10_000;

        final TokenBucketSpec spec = TokenBucketSpec.builder()
                .capacity(100_000)
                .refillTokens(10_000)
                .refillPeriod(Duration.ofSeconds(1))
                .contentionStrategy(strategy)
                .build();

        final RateLimiter limiter = BucketGuards.tokenBucket(spec);
        final ExecutorService pool = Executors.newFixedThreadPool(threads);
        final CountDownLatch latch = new CountDownLatch(threads);
        final AtomicLong grantedCount = new AtomicLong();

        for (int i = 0; i < threads; i++) {
            pool.submit(() -> {
                for (int j = 0; j < requestsPerThread; j++) {
                    if (limiter.tryAcquire().granted()) {
                        grantedCount.incrementAndGet();
                    }
                }
                latch.countDown();
            });
        }

        latch.await(30, TimeUnit.SECONDS);
        pool.shutdown();

        assertTrue(grantedCount.get() > 0);
        // Assert no negative tokens in snapshot
        assertTrue(limiter.snapshot().availableTokens() >= 0);
    }
}
