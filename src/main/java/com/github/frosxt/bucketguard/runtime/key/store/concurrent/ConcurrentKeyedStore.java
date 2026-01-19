package com.github.frosxt.bucketguard.runtime.key.store.concurrent;

import com.github.frosxt.bucketguard.runtime.bucket.Bucket;
import com.github.frosxt.bucketguard.runtime.key.store.KeyedStore;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * High-throughput keyed store using ConcurrentHashMap.
 * <p>
 * No eviction or expiry support. Use for EvictionPolicy.NONE.
 *
 * @param <K> the key type
 */
public final class ConcurrentKeyedStore<K> implements KeyedStore<K> {
    private final ConcurrentHashMap<K, Bucket> map = new ConcurrentHashMap<>();

    @Override
    public Bucket getOrCreate(final K key, final Supplier<Bucket> factory) {
        return map.computeIfAbsent(key, k -> factory.get());
    }

    @Override
    public Bucket get(final K key) {
        return map.get(key);
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public Map<K, Bucket> snapshot(final int limit) {
        final Map<K, Bucket> result = new HashMap<>();
        int count = 0;
        for (final var entry : map.entrySet()) {
            if (count >= limit) {
                break;
            }
            result.put(entry.getKey(), entry.getValue());
            count++;
        }

        return result;
    }
}
