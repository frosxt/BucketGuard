package com.github.frosxt.bucketguard.runtime.limiter.keyed;

import com.github.frosxt.bucketguard.api.KeyedLimiterStats;
import com.github.frosxt.bucketguard.api.KeyedRateLimiter;
import com.github.frosxt.bucketguard.api.LimiterStats;
import com.github.frosxt.bucketguard.api.Permit;
import com.github.frosxt.bucketguard.api.spec.KeyedStoreSpec;
import com.github.frosxt.bucketguard.api.spec.TokenBucketSpec;
import com.github.frosxt.bucketguard.runtime.acquire.keyed.KeyedAcquireCoordinator;
import com.github.frosxt.bucketguard.runtime.acquire.keyed.KeyedSnapshots;
import com.github.frosxt.bucketguard.runtime.acquire.keyed.MaintenanceController;
import com.github.frosxt.bucketguard.runtime.key.store.KeyedStore;
import com.github.frosxt.bucketguard.runtime.key.store.KeyedStoreFactory;

import java.time.Duration;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;

public class StandardKeyedRateLimiter<K> implements KeyedRateLimiter<K> {
    private final KeyedStore<K> store;
    private final TokenBucketSpec bucketSpec;
    private final KeyedAcquireCoordinator<K> acquireCoordinator;
    private final MaintenanceController<K> maintenanceController;

    /**
     * Creates a new StandardKeyedRateLimiter.
     * 
     * @param bucketSpec bucket configuration, not null
     * @param storeSpec  store configuration, not null
     */
    public StandardKeyedRateLimiter(final TokenBucketSpec bucketSpec, final KeyedStoreSpec<K> storeSpec) {
        this.bucketSpec = bucketSpec;
        this.store = KeyedStoreFactory.create(storeSpec, bucketSpec.timeSource());
        this.acquireCoordinator = new KeyedAcquireCoordinator<>(store, bucketSpec);
        this.maintenanceController = new MaintenanceController<>(store, storeSpec);
    }

    @Override
    public Permit tryAcquire(final K key) {
        return tryAcquire(key, 1);
    }

    @Override
    public Permit tryAcquire(final K key, final long tokens) {
        return acquireCoordinator.tryAcquire(key, tokens);
    }

    @Override
    public Permit acquire(final K key) throws InterruptedException {
        return acquire(key, 1);
    }

    @Override
    public Permit acquire(final K key, final long tokens) throws InterruptedException {
        return acquireCoordinator.acquire(key, tokens);
    }

    @Override
    public CompletionStage<Permit> acquireAsync(final K key, final Executor executor) {
        return acquireAsync(key, 1, executor);
    }

    @Override
    public CompletionStage<Permit> acquireAsync(final K key, final long tokens, final Executor executor) {
        return acquireCoordinator.acquireAsync(key, tokens, executor);
    }

    @Override
    public LimiterStats snapshot(final K key) {
        return KeyedSnapshots.snapshot(key, store, bucketSpec);
    }

    @Override
    public KeyedLimiterStats snapshotAll() {
        return snapshotSample(Integer.MAX_VALUE);
    }

    @Override
    public KeyedLimiterStats snapshotSample(final int limit) {
        return KeyedSnapshots.snapshotSample(limit, store);
    }

    @Override
    public void prune() {
        maintenanceController.prune();
    }

    @Override
    public void startMaintenance(final ScheduledExecutorService scheduler) {
        maintenanceController.startMaintenance(scheduler);
    }

    @Override
    public void startMaintenance(final ScheduledExecutorService scheduler, final Duration period) {
        maintenanceController.startMaintenance(scheduler, period);
    }

    @Override
    public boolean isMaintenanceRunning() {
        return maintenanceController.isMaintenanceRunning();
    }

    @Override
    public void close() {
        maintenanceController.close();
    }
}
