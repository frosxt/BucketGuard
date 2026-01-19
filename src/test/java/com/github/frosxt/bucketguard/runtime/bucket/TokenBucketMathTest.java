package com.github.frosxt.bucketguard.runtime.bucket;

import com.github.frosxt.bucketguard.api.Permit;
import com.github.frosxt.bucketguard.api.spec.TokenBucketSpec;
import com.github.frosxt.bucketguard.runtime.bucket.atomic.AtomicBucket;
import com.github.frosxt.bucketguard.test.FakeTimeSource;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class TokenBucketMathTest {

    @Test
    void testRefillLogic() {
        final FakeTimeSource time = new FakeTimeSource();
        final TokenBucketSpec spec = TokenBucketSpec.builder()
                .capacity(10)
                .refillTokens(1)
                .refillPeriod(Duration.ofSeconds(1))
                .timeSource(time)
                .build();

        final AtomicBucket bucket = new AtomicBucket(spec);

        // Initial state: full
        assertTrue(bucket.tryAcquire(10, time.nanoTime()).granted());
        assertFalse(bucket.tryAcquire(1, time.nanoTime()).granted());

        // Advance 0.5s -> no refill (integer math: 1 * 0.5 / 1 = 0)
        time.advance(Duration.ofMillis(500).toNanos());
        assertFalse(bucket.tryAcquire(1, time.nanoTime()).granted());

        // Advance another 0.5s -> total 1s -> 1 token
        time.advance(Duration.ofMillis(500).toNanos());
        assertTrue(bucket.tryAcquire(1, time.nanoTime()).granted());
        assertFalse(bucket.tryAcquire(1, time.nanoTime()).granted());
    }

    @Test
    void testRetryAfter() {
        final FakeTimeSource time = new FakeTimeSource();
        final TokenBucketSpec spec = TokenBucketSpec.builder()
                .capacity(10)
                .refillTokens(1)
                .refillPeriod(Duration.ofSeconds(1))
                .timeSource(time)
                .build();

        final AtomicBucket bucket = new AtomicBucket(spec);
        bucket.tryAcquire(10, time.nanoTime()); // drain

        final Permit p = bucket.tryAcquire(1, time.nanoTime());
        assertFalse(p.granted());
        // Deficit is 1. Refill rate 1 token / 1 sec.
        // Wait should be 1 sec.
        assertEquals(Duration.ofSeconds(1).toNanos(), p.retryAfterNanos());
    }
}
