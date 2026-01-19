package com.github.frosxt.bucketguard.runtime.key.store.exact;

import com.github.frosxt.bucketguard.api.time.TimeSource;
import com.github.frosxt.bucketguard.runtime.bucket.Bucket;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Exact LRU and expire-after-access keyed store.
 */
public final class ExactKeyedStore<K> {
    private final int maxKeys;
    private final ExpiryPolicy expiryPolicy;
    private final RemovalDispatch<K> removalDispatch;

    private final Object lock = new Object();
    private final LruMap<K> map;

    public ExactKeyedStore(final int maxKeys, final long expireNanos, final Consumer<K> removalListener, final TimeSource timeSource) {
        this.maxKeys = maxKeys;
        this.expiryPolicy = new ExpiryPolicy(expireNanos, timeSource);
        this.removalDispatch = new RemovalDispatch<>(removalListener);
        this.map = new LruMap<>(16, 0.75f);
    }

    /**
     * Gets or creates a bucket.
     * 
     * @param key     the key
     * @param factory factory for new buckets
     * @return the bucket
     */
    public Bucket getOrCreate(final K key, final Supplier<Bucket> factory) {
        synchronized (lock) {
            final long now = expiryPolicy.now();

            final StoreEntry<K> existing = map.get(key);
            if (existing != null) {
                if (expiryPolicy.isExpired(existing.lastAccessNanos(), now)) {
                    map.remove(key);
                    removalDispatch.fire(key);
                } else {
                    existing.touch(now);
                    return existing.bucket();
                }
            }

            final Bucket bucket = factory.get();
            final StoreEntry<K> entry = new StoreEntry<>(key, bucket, now);
            map.put(key, entry);

            enforceMaxKeys();

            return bucket;
        }
    }

    /**
     * Gets a bucket if present.
     * 
     * @param key the key
     * @return the bucket or null
     */
    public Bucket get(final K key) {
        synchronized (lock) {
            final StoreEntry<K> entry = map.get(key);
            if (entry == null) {
                return null;
            }

            final long now = expiryPolicy.now();
            if (expiryPolicy.isExpired(entry.lastAccessNanos(), now)) {
                map.remove(key);
                removalDispatch.fire(key);
                return null;
            }

            entry.touch(now);
            return entry.bucket();
        }
    }

    /**
     * @return current size
     */
    public int size() {
        synchronized (lock) {
            return map.size();
        }
    }

    /**
     * Prunes expired entries.
     */
    public void prune() {
        if (!expiryPolicy.isEnabled()) {
            return;
        }

        synchronized (lock) {
            final long now = expiryPolicy.now();
            final var it = map.entrySet().iterator();
            while (it.hasNext()) {
                final var e = it.next();
                if (expiryPolicy.isExpired(e.getValue().lastAccessNanos(), now)) {
                    it.remove();
                    removalDispatch.fire(e.getKey());
                }
            }
        }
    }

    /**
     * Snapshot logic.
     * 
     * @param limit limit
     * @return snapshot map
     */
    public Map<K, Bucket> snapshot(final int limit) {
        synchronized (lock) {
            final Map<K, Bucket> result = new HashMap<>();
            int count = 0;
            for (final var e : map.entrySet()) {
                if (count >= limit) {
                    break;
                }
                result.put(e.getKey(), e.getValue().bucket());
                count++;
            }
            return result;
        }
    }

    public void remove(final K key) {
        synchronized (lock) {
            if (map.remove(key) != null) {
                removalDispatch.fire(key);
            }
        }
    }

    public void clear() {
        synchronized (lock) {
            for (final K key : map.keySet()) {
                removalDispatch.fire(key);
            }
            map.clear();
        }
    }

    private void enforceMaxKeys() {
        if (maxKeys <= 0) {
            return;
        }

        final var it = map.entrySet().iterator();
        while (map.size() > maxKeys && it.hasNext()) {
            final var eldest = it.next();
            it.remove();
            removalDispatch.fire(eldest.getKey());
        }
    }
}
