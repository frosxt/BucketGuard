package com.github.frosxt.bucketguard.runtime.acquire.keyed;

import com.github.frosxt.bucketguard.api.spec.KeyedStoreSpec;
import com.github.frosxt.bucketguard.runtime.key.store.KeyedStore;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public final class MaintenanceController<K> {
    private final KeyedStore<K> store;
    private final KeyedStoreSpec<K> storeSpec;

    private final Object maintenanceLock = new Object();
    private volatile ScheduledFuture<?> maintenanceTask;

    public MaintenanceController(final KeyedStore<K> store, final KeyedStoreSpec<K> storeSpec) {
        this.store = store;
        this.storeSpec = storeSpec;
    }

    /**
     * Prunes expired keys from the store.
     */
    public void prune() {
        store.prune();
    }

    /**
     * Starts maintenance with default period.
     * 
     * @param scheduler scheduler to use
     */
    public void startMaintenance(final ScheduledExecutorService scheduler) {
        startMaintenance(scheduler, storeSpec.maintenancePeriod());
    }

    /**
     * Starts maintenance with specified period.
     * 
     * @param scheduler scheduler to use
     * @param period    execution period
     */
    public void startMaintenance(final ScheduledExecutorService scheduler, final Duration period) {
        Objects.requireNonNull(scheduler, "scheduler");
        Objects.requireNonNull(period, "period");

        synchronized (maintenanceLock) {
            if (maintenanceTask != null && !maintenanceTask.isDone()) {
                return;
            }

            maintenanceTask = scheduler.scheduleWithFixedDelay(this::pruneSafely, period.toNanos(), period.toNanos(), TimeUnit.NANOSECONDS);
        }
    }

    /**
     * Safely executes prune, swallowing exceptions to prevent scheduler thread death.
     */
    private void pruneSafely() {
        try {
            prune();
        } catch (final Exception ignored) {
        }
    }

    /**
     * @return true if maintenance is running
     */
    public boolean isMaintenanceRunning() {
        final ScheduledFuture<?> task = maintenanceTask;
        return task != null && !task.isDone();
    }

    /**
     * Stops the maintenance task.
     */
    public void close() {
        synchronized (maintenanceLock) {
            if (maintenanceTask != null) {
                maintenanceTask.cancel(false);
                maintenanceTask = null;
            }
        }
    }
}
