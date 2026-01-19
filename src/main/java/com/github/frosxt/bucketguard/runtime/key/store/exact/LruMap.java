package com.github.frosxt.bucketguard.runtime.key.store.exact;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Specialized LinkedHashMap for LRU access order.
 */
public class LruMap<K> extends LinkedHashMap<K, StoreEntry<K>> {

    /**
     * Creates a new LruMap.
     * 
     * @param initialCapacity initial size
     * @param loadFactor      load factor
     */
    public LruMap(final int initialCapacity, final float loadFactor) {
        super(initialCapacity, loadFactor, true);
    }

    @Override
    protected boolean removeEldestEntry(final Map.Entry<K, StoreEntry<K>> eldest) {
        return false;
    }
}
