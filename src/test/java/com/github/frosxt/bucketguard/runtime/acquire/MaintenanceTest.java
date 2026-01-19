package com.github.frosxt.bucketguard.runtime.acquire;

import com.github.frosxt.bucketguard.api.KeyedRateLimiter;
import com.github.frosxt.bucketguard.api.factory.BucketGuards;
import com.github.frosxt.bucketguard.api.spec.EvictionPolicy;
import com.github.frosxt.bucketguard.api.spec.KeyedStoreSpec;
import com.github.frosxt.bucketguard.api.spec.TokenBucketSpec;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static org.junit.jupiter.api.Assertions.*;

class MaintenanceTest {

    @Test
    void startMaintenanceSchedulesPrune() throws InterruptedException {
        final TokenBucketSpec spec = TokenBucketSpec.builder().capacity(10).build();
        final KeyedStoreSpec<String> storeSpec = KeyedStoreSpec.<String>builder()
                .evictionPolicy(EvictionPolicy.EXPIRE_AFTER_ACCESS)
                .expireAfterAccess(Duration.ofMillis(50))
                .maintenanceEnabled(true)
                .maintenancePeriod(Duration.ofMillis(100))
                .build();

        try (final KeyedRateLimiter<String> limiter = BucketGuards.keyedTokenBucket(spec, storeSpec)) {
            final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
            try {
                // Add a key
                limiter.tryAcquire("test-key");
                assertEquals(1, limiter.snapshotAll().keyCount());

                // Start maintenance
                limiter.startMaintenance(scheduler);
                assertTrue(limiter.isMaintenanceRunning());

                // Entry should expire and get pruned by scheduled maintenance
                Thread.sleep(300);

                // Maintenance should have run and pruned
                assertEquals(0, limiter.snapshotAll().keyCount());
            } finally {
                scheduler.shutdownNow();
            }
        }
    }

    @Test
    void closeCancelsTask() {
        final TokenBucketSpec spec = TokenBucketSpec.builder().capacity(10).build();
        final KeyedStoreSpec<String> storeSpec = KeyedStoreSpec.<String>builder().build();

        final KeyedRateLimiter<String> limiter = BucketGuards.keyedTokenBucket(spec, storeSpec);
        final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        try {
            limiter.startMaintenance(scheduler, Duration.ofSeconds(1));
            assertTrue(limiter.isMaintenanceRunning());

            limiter.close();
            assertFalse(limiter.isMaintenanceRunning());
        } finally {
            scheduler.shutdownNow();
        }
    }

    @Test
    void idempotentStartDoesNotDoubleSchedule() {
        final TokenBucketSpec spec = TokenBucketSpec.builder().capacity(10).build();
        final KeyedStoreSpec<String> storeSpec = KeyedStoreSpec.<String>builder().build();

        try (final KeyedRateLimiter<String> limiter = BucketGuards.keyedTokenBucket(spec, storeSpec)) {
            final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

            try {
                limiter.startMaintenance(scheduler, Duration.ofSeconds(1));
                assertTrue(limiter.isMaintenanceRunning());

                // Call again - should be idempotent
                limiter.startMaintenance(scheduler, Duration.ofSeconds(1));
                assertTrue(limiter.isMaintenanceRunning());

                // Still only one task running (can't directly verify, but no exception)
            } finally {
                scheduler.shutdownNow();
            }
        }
    }

    @Test
    void maintenanceDefaultPeriodFromSpec() {
        final TokenBucketSpec spec = TokenBucketSpec.builder().capacity(10).build();
        final KeyedStoreSpec<String> storeSpec = KeyedStoreSpec.<String>builder()
                .maintenancePeriod(Duration.ofMillis(500))
                .build();

        assertEquals(Duration.ofMillis(500), storeSpec.maintenancePeriod());
    }

    @Test
    void maintenanceDefaultPeriodIfNotSet() {
        final KeyedStoreSpec<String> storeSpec = KeyedStoreSpec.<String>builder().build();
        assertEquals(Duration.ofSeconds(5), storeSpec.maintenancePeriod());
    }
}
