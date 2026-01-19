package com.github.frosxt.bucketguard.runtime.wiring;

import com.github.frosxt.bucketguard.api.KeyedRateLimiter;
import com.github.frosxt.bucketguard.api.spec.KeyedStoreSpec;
import com.github.frosxt.bucketguard.api.spec.TokenBucketSpec;
import com.github.frosxt.bucketguard.runtime.limiter.keyed.StandardKeyedRateLimiter;

import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;

public final class KeyedLimiterFactory {

    private KeyedLimiterFactory() {
        throw new UnsupportedOperationException("This class cannot be instantiated!");
    }

    /**
     * Creates a keyed rate limiter.
     *
     * @param spec      bucket spec
     * @param storeSpec store spec
     * @param <K>       key type
     * @return the limiter
     */
    public static <K> KeyedRateLimiter<K> create(final TokenBucketSpec spec, final KeyedStoreSpec<K> storeSpec) {
        Objects.requireNonNull(spec, "spec");
        Objects.requireNonNull(storeSpec, "storeSpec");

        return new StandardKeyedRateLimiter<>(spec, storeSpec);
    }

    /**
     * Creates a keyed rate limiter and optionally starts maintenance.
     *
     * @param spec      bucket spec
     * @param storeSpec store spec
     * @param scheduler optional scheduler for maintenance (if enabled in spec)
     * @param <K>       key type
     * @return the limiter
     */
    public static <K> KeyedRateLimiter<K> create(final TokenBucketSpec spec, final KeyedStoreSpec<K> storeSpec,
            final ScheduledExecutorService scheduler) {
        Objects.requireNonNull(spec, "spec");
        Objects.requireNonNull(storeSpec, "storeSpec");

        final KeyedRateLimiter<K> limiter = new StandardKeyedRateLimiter<>(spec, storeSpec);

        if (scheduler != null && storeSpec.maintenanceEnabled()) {
            limiter.startMaintenance(scheduler, storeSpec.maintenancePeriod());
        }

        return limiter;
    }
}
