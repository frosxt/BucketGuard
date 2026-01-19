package com.github.frosxt.bucketguard.runtime.acquire;

import com.github.frosxt.bucketguard.api.Permit;
import com.github.frosxt.bucketguard.api.RateLimiter;
import com.github.frosxt.bucketguard.api.factory.BucketGuards;
import com.github.frosxt.bucketguard.api.spec.TokenBucketSpec;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;

public class AllowBurstTest {

    @Test
    void testAllowBurstFalse_AllowsSingleToken() {
        final TokenBucketSpec spec = TokenBucketSpec.builder()
                .capacity(10)
                .refillTokens(1)
                .refillPeriod(Duration.ofSeconds(1))
                .allowBurst(false)
                .build();

        final RateLimiter limiter = BucketGuards.tokenBucket(spec);

        // First acquisition should succeed because we allow 1 interval of burst effectively current flow)
        final Permit p1 = limiter.tryAcquire(1);
        Assertions.assertTrue(p1.granted(), "First permit should be granted even with allowBurst=false");
    }

    @Test
    void testAllowBurstFalse_PreventsBurst() {
        final TokenBucketSpec spec = TokenBucketSpec.builder()
                .capacity(10)
                .refillTokens(1)
                .refillPeriod(Duration.ofSeconds(1)) // Rate = 1 token / sec
                .allowBurst(false)
                .build();

        // With allowBurst=false, we expect behavior similar to capacity=1

        final RateLimiter limiter = BucketGuards.tokenBucket(spec);

        // Acquire 1 (ok)
        Assertions.assertTrue(limiter.tryAcquire(1).granted());

        // Immediate second acquire should fail (because we don't accumulate capacity)
        // With standard capacity=10, we could acquire 10 in a row.
        // With allowBurst=false, we should generally be limited to the rate.
        // Since we just consumed 1, we are "at limit" of the immediate window.

        final Permit p2 = limiter.tryAcquire(1);
        Assertions.assertFalse(p2.granted(), "Should not allow burst of 2 immediately when allowBurst=false and rate is 1/s");
    }

    @Test
    void testAllowBurstTrue_AllowsBurst() {
        final TokenBucketSpec spec = TokenBucketSpec.builder()
                .capacity(10)
                .refillTokens(1)
                .refillPeriod(Duration.ofSeconds(1))
                .allowBurst(true)
                .build();

        final RateLimiter limiter = BucketGuards.tokenBucket(spec);

        // Should be able to acquire 10 immediately
        for (int i = 0; i < 10; i++) {
            Assertions.assertTrue(limiter.tryAcquire(1).granted(), "Should allow burst up to capacity");
        }

        Assertions.assertFalse(limiter.tryAcquire(1).granted(), "Should exhaust capacity");
    }
}
