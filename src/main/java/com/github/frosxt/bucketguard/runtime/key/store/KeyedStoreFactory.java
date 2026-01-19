package com.github.frosxt.bucketguard.runtime.key.store;

import com.github.frosxt.bucketguard.api.spec.EvictionPolicy;
import com.github.frosxt.bucketguard.api.spec.KeyedStoreSpec;
import com.github.frosxt.bucketguard.api.time.TimeSource;
import com.github.frosxt.bucketguard.runtime.key.store.concurrent.ConcurrentKeyedStore;
import com.github.frosxt.bucketguard.runtime.key.store.exact.ExactKeyedStoreAdapter;

/**
 * Factory for creating keyed stores based on spec configuration.
 */
public final class KeyedStoreFactory {

    private KeyedStoreFactory() {
        throw new UnsupportedOperationException("This class cannot be instantiated!");
    }

    /**
     * Creates a KeyedStore based on the spec's eviction policy.
     *
     * @param spec       the store specification
     * @param timeSource the time source for expiry
     * @param <K>        the key type
     * @return a new KeyedStore instance
     */
    public static <K> KeyedStore<K> create(final KeyedStoreSpec<K> spec, final TimeSource timeSource) {
        final EvictionPolicy policy = spec.evictionPolicy();

        if (policy == EvictionPolicy.NONE) {
            return new ConcurrentKeyedStore<>();
        }

        final int maxKeys = spec.maxKeys();
        final long expireNanos;
        if (policy == EvictionPolicy.EXPIRE_AFTER_ACCESS && spec.expireAfterAccess() != null) {
            expireNanos = spec.expireAfterAccess().toNanos();
        } else {
            expireNanos = 0;
        }

        return new ExactKeyedStoreAdapter<>(maxKeys, expireNanos, spec.removalListener(), timeSource);
    }
}
