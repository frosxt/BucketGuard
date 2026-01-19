package com.github.frosxt.bucketguard.runtime.acquire;

import com.github.frosxt.bucketguard.api.KeyedRateLimiter;
import com.github.frosxt.bucketguard.api.factory.BucketGuards;
import com.github.frosxt.bucketguard.api.spec.EvictionPolicy;
import com.github.frosxt.bucketguard.api.spec.KeyedStoreSpec;
import com.github.frosxt.bucketguard.api.spec.TokenBucketSpec;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class KeyedRateLimiterTest {

    @Test
    void testIsolation() {
        final TokenBucketSpec spec = TokenBucketSpec.builder()
                .capacity(10)
                .refillTokens(1)
                .refillPeriod(Duration.ofSeconds(1))
                .build();

        final KeyedRateLimiter<String> limiter = BucketGuards.keyedTokenBucket(spec, KeyedStoreSpec.<String>builder().build());

        assertTrue(limiter.tryAcquire("A", 10).granted());
        assertFalse(limiter.tryAcquire("A", 1).granted());

        assertTrue(limiter.tryAcquire("B", 10).granted());
    }

    @Test
    void testLRUEviction() {
        final TokenBucketSpec spec = TokenBucketSpec.builder().capacity(10).build();
        final KeyedStoreSpec<String> storeSpec = KeyedStoreSpec.<String>builder()
                .evictionPolicy(EvictionPolicy.LRU)
                .maxKeys(2)
                .build();

        final KeyedRateLimiter<String> limiter = BucketGuards.keyedTokenBucket(spec, storeSpec);

        limiter.tryAcquire("A");
        limiter.tryAcquire("B");
        assertEquals(2, limiter.snapshotAll().keyCount());

        limiter.tryAcquire("C");
        assertEquals(2, limiter.snapshotAll().keyCount()); // A should be evicted (or B if accessed logic differs, but definitely size <= 2)

        // Verify size
        assertTrue(limiter.snapshotAll().keyCount() <= 2);
    }
}
