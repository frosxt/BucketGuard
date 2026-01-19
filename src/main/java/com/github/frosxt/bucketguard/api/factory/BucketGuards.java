package com.github.frosxt.bucketguard.api.factory;

import com.github.frosxt.bucketguard.api.KeyedRateLimiter;
import com.github.frosxt.bucketguard.api.RateLimiter;
import com.github.frosxt.bucketguard.api.spec.KeyedStoreSpec;
import com.github.frosxt.bucketguard.api.spec.TokenBucketSpec;
import com.github.frosxt.bucketguard.api.spec.builder.KeyedStoreSpecBuilder;
import com.github.frosxt.bucketguard.api.spec.builder.TokenBucketSpecBuilder;
import com.github.frosxt.bucketguard.runtime.wiring.KeyedLimiterFactory;
import com.github.frosxt.bucketguard.runtime.wiring.LimiterFactory;

import java.util.concurrent.ScheduledExecutorService;

/**
 * Factory for creating rate limiters.
 */
public final class BucketGuards {

    private BucketGuards() {
        throw new UnsupportedOperationException("This class cannot be instantiated!");
    }

    /**
     * Creates a new RateLimiter with the specified spec.
     * 
     * @param spec configuration spec, not null
     * @return a new RateLimiter instance
     */
    public static RateLimiter tokenBucket(final TokenBucketSpec spec) {
        return LimiterFactory.create(spec);
    }

    /**
     * Creates a new KeyedRateLimiter with the specified specs.
     * 
     * @param spec      bucket configuration spec, not null
     * @param storeSpec key storage configuration spec, not null
     * @param <K>       key type
     * @return a new KeyedRateLimiter instance
     */
    public static <K> KeyedRateLimiter<K> keyedTokenBucket(final TokenBucketSpec spec, final KeyedStoreSpec<K> storeSpec) {
        return KeyedLimiterFactory.create(spec, storeSpec);
    }

    /**
     * Creates a new KeyedRateLimiter with the specified specs and optional
     * scheduler.
     * <p>
     * If {@code storeSpec.maintenanceEnabled()} is {@code true} and a scheduler is
     * provided,
     * maintenance will be auto-started using the configured
     * {@code maintenancePeriod()}.
     *
     * @param spec      bucket configuration spec, not null
     * @param storeSpec key storage configuration spec, not null
     * @param scheduler scheduler for maintenance tasks, may be null
     * @param <K>       key type
     * @return a new KeyedRateLimiter instance with maintenance optionally started
     */
    public static <K> KeyedRateLimiter<K> keyedTokenBucket(final TokenBucketSpec spec,
            final KeyedStoreSpec<K> storeSpec, final ScheduledExecutorService scheduler) {
        return KeyedLimiterFactory.create(spec, storeSpec, scheduler);
    }

    /**
     * Creates a builder for TokenBucketSpec.
     * 
     * @return a new builder
     */
    public static TokenBucketSpecBuilder tokenBucketBuilder() {
        return TokenBucketSpec.builder();
    }

    /**
     * Creates a builder for KeyedStoreSpec.
     * 
     * @param <K> the key type
     * @return a new builder
     */
    public static <K> KeyedStoreSpecBuilder<K> keyedTokenBucketBuilder() {
        return KeyedStoreSpec.builder();
    }
}
