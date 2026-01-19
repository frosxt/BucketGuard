package com.github.frosxt.bucketguard.api.spec;

import com.github.frosxt.bucketguard.api.spec.builder.KeyedStoreSpecBuilder;

import java.time.Duration;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Configuration for the key storage of a
 * {@link com.github.frosxt.bucketguard.api.KeyedRateLimiter}.
 *
 * @param <K> the type of key
 */
public final class KeyedStoreSpec<K> {
    private static final Duration DEFAULT_MAINTENANCE_PERIOD = Duration.ofSeconds(5);

    private final int maxKeys;
    private final Duration expireAfterAccess;
    private final EvictionPolicy evictionPolicy;
    private final Consumer<K> removalListener;
    private final boolean maintenanceEnabled;
    private final Duration maintenancePeriod;

    public KeyedStoreSpec(final KeyedStoreSpecBuilder<K> builder) {
        this.maxKeys = builder.getMaxKeys();
        this.expireAfterAccess = builder.getExpireAfterAccess();
        this.evictionPolicy = builder.getEvictionPolicy();
        this.removalListener = builder.getRemovalListener();
        this.maintenanceEnabled = builder.isMaintenanceEnabled();
        this.maintenancePeriod = builder.getMaintenancePeriod() != null ? builder.getMaintenancePeriod() : DEFAULT_MAINTENANCE_PERIOD;

        validate();
    }

    private void validate() {
        Objects.requireNonNull(evictionPolicy, "evictionPolicy");
        if (evictionPolicy == EvictionPolicy.LRU && maxKeys <= 0) {
            throw new IllegalArgumentException("maxKeys must be > 0 when EvictionPolicy is LRU");
        }
        if (evictionPolicy == EvictionPolicy.EXPIRE_AFTER_ACCESS
                && (expireAfterAccess == null || expireAfterAccess.isZero() || expireAfterAccess.isNegative())) {
            throw new IllegalArgumentException(
                    "expireAfterAccess must be > 0 when EvictionPolicy is EXPIRE_AFTER_ACCESS");
        }
        if (evictionPolicy == EvictionPolicy.EXPIRE_AFTER_ACCESS) {
            try {
                expireAfterAccess.toNanos();
            } catch (final ArithmeticException e) {
                throw new IllegalArgumentException("expireAfterAccess overflow", e);
            }
        }
        if (maintenancePeriod != null && (maintenancePeriod.isZero() || maintenancePeriod.isNegative())) {
            throw new IllegalArgumentException("maintenancePeriod must be > 0");
        }
        if (maintenancePeriod != null) {
            try {
                maintenancePeriod.toNanos();
            } catch (final ArithmeticException e) {
                throw new IllegalArgumentException("maintenancePeriod overflow", e);
            }
        }
    }

    public static <K> KeyedStoreSpecBuilder<K> builder() {
        return new KeyedStoreSpecBuilder<>();
    }

    /**
     * @return the maximum number of keys allowed in the store, or -1 if unbounded.
     */
    public int maxKeys() {
        return maxKeys;
    }

    /**
     * @return the duration after which an idle key is candidate for eviction, or null if not applicable.
     *         <p>
     *         Eviction occurs on access (if expired) and during {@code prune()}
     *         calls.
     *         Use scheduled maintenance for active expiration of idle keys.
     */
    public Duration expireAfterAccess() {
        return expireAfterAccess;
    }

    /**
     * @return the eviction policy.
     */
    public EvictionPolicy evictionPolicy() {
        return evictionPolicy;
    }

    /**
     * @return the listener invoked when a key is evicted, or null.
     */
    public Consumer<K> removalListener() {
        return removalListener;
    }

    /**
     * @return {@code true} if maintenance should be started automatically when a
     *         scheduler is provided.
     */
    public boolean maintenanceEnabled() {
        return maintenanceEnabled;
    }

    /**
     * @return the period for scheduled maintenance tasks. Defaults to 5 seconds.
     */
    public Duration maintenancePeriod() {
        return maintenancePeriod;
    }
}
