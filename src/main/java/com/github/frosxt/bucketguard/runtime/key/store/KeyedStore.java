package com.github.frosxt.bucketguard.runtime.key.store;

import com.github.frosxt.bucketguard.runtime.bucket.Bucket;

import java.util.Map;
import java.util.function.Supplier;

/**
 * Internal interface for keyed bucket stores.
 *
 * @param <K> the key type
 */
public interface KeyedStore<K> {

    /**
     * Gets or creates a bucket for the key.
     */
    Bucket getOrCreate(K key, Supplier<Bucket> factory);

    /**
     * Gets the bucket for the key, or null if absent.
     */
    Bucket get(K key);

    /**
     * Returns the current size.
     */
    int size();

    /**
     * Prunes expired entries (no-op for non-expiring stores).
     */
    default void prune() {
    }

    /**
     * Returns a bounded snapshot of entries.
     */
    Map<K, Bucket> snapshot(int limit);
}
