package com.github.frosxt.bucketguard.api.spec.builder;

import com.github.frosxt.bucketguard.api.spec.EvictionPolicy;
import com.github.frosxt.bucketguard.api.spec.KeyedStoreSpec;

import java.time.Duration;
import java.util.function.Consumer;

/**
 * Builder for {@link KeyedStoreSpec}.
 * 
 * @param <K> key type
 */
public final class KeyedStoreSpecBuilder<K> {
    private int maxKeys = -1;
    private Duration expireAfterAccess = null;
    private EvictionPolicy evictionPolicy = EvictionPolicy.NONE;
    private Consumer<K> removalListener = null;
    private boolean maintenanceEnabled = false;
    private Duration maintenancePeriod = null;

    public int getMaxKeys() {
        return maxKeys;
    }

    public Duration getExpireAfterAccess() {
        return expireAfterAccess;
    }

    public EvictionPolicy getEvictionPolicy() {
        return evictionPolicy;
    }

    public Consumer<K> getRemovalListener() {
        return removalListener;
    }

    public boolean isMaintenanceEnabled() {
        return maintenanceEnabled;
    }

    public Duration getMaintenancePeriod() {
        return maintenancePeriod;
    }

    /**
     * Sets the maximum number of keys.
     * 
     * @param maxKeys must be > 0 if policy requires it.
     * @return this builder.
     */
    public KeyedStoreSpecBuilder<K> maxKeys(final int maxKeys) {
        this.maxKeys = maxKeys;
        return this;
    }

    /**
     * Sets the expire-after-access duration.
     * 
     * @param expireAfterAccess must be > 0.
     * @return this builder.
     */
    public KeyedStoreSpecBuilder<K> expireAfterAccess(final Duration expireAfterAccess) {
        this.expireAfterAccess = expireAfterAccess;
        return this;
    }

    /**
     * Sets the eviction policy.
     * 
     * @param evictionPolicy not null.
     * @return this builder.
     */
    public KeyedStoreSpecBuilder<K> evictionPolicy(final EvictionPolicy evictionPolicy) {
        this.evictionPolicy = evictionPolicy;
        return this;
    }

    /**
     * Sets a removal listener.
     * 
     * @param removalListener listener to invoke on eviction.
     * @return this builder.
     */
    public KeyedStoreSpecBuilder<K> removalListener(final Consumer<K> removalListener) {
        this.removalListener = removalListener;
        return this;
    }

    /**
     * Enables or disables automatic maintenance scheduling.
     * <p>
     * When enabled and a scheduler is passed to startMaintenance/factory,
     * prune tasks will be scheduled at the configured period.
     *
     * @param maintenanceEnabled true to enable.
     * @return this builder.
     */
    public KeyedStoreSpecBuilder<K> maintenanceEnabled(final boolean maintenanceEnabled) {
        this.maintenanceEnabled = maintenanceEnabled;
        return this;
    }

    /**
     * Sets the period for scheduled maintenance.
     * <p>
     * Defaults to 5 seconds if not specified.
     *
     * @param maintenancePeriod the period between prune executions.
     * @return this builder.
     */
    public KeyedStoreSpecBuilder<K> maintenancePeriod(final Duration maintenancePeriod) {
        this.maintenancePeriod = maintenancePeriod;
        return this;
    }

    /**
     * Builds the spec.
     * 
     * @return a new {@link KeyedStoreSpec}.
     */
    public KeyedStoreSpec<K> build() {
        return new KeyedStoreSpec<>(this);
    }
}
