package com.github.frosxt.bucketguard.runtime.acquire.keyed;

import com.github.frosxt.bucketguard.api.KeyedLimiterStats;
import com.github.frosxt.bucketguard.api.LimiterStats;
import com.github.frosxt.bucketguard.api.spec.TokenBucketSpec;
import com.github.frosxt.bucketguard.runtime.bucket.Bucket;
import com.github.frosxt.bucketguard.runtime.key.store.KeyedStore;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class KeyedSnapshots {

    private KeyedSnapshots() {
        throw new UnsupportedOperationException("This class cannot be instantiated!");
    }

    /**
     * Captures a snapshot for a single key.
     * <p>
     * For unknown keys, returns initial bucket state:
     * <ul>
     * <li>allowBurst=true: availableTokens = capacity</li>
     * <li>allowBurst=false: availableTokens = 1 (steady-state throttle)</li>
     * </ul>
     *
     * @param key        the key
     * @param store      the store
     * @param bucketSpec the spec (for default values if key missing)
     * @param <K>        key type
     * @return stats
     */
    public static <K> LimiterStats snapshot(final K key, final KeyedStore<K> store, final TokenBucketSpec bucketSpec) {
        final Bucket b = store.get(key);
        if (b == null) {
            final long available = bucketSpec.allowBurst() ? bucketSpec.capacity() : 1;
            return new LimiterStats(bucketSpec.capacity(), available, bucketSpec.refillTokens(),
                    bucketSpec.refillPeriod());
        }
        return b.snapshot();
    }

    /**
     * Captures a sample of snapshots from the store.
     *
     * @param limit max number of entries
     * @param store the store
     * @param <K>   key type
     * @return keyed stats
     */
    public static <K> KeyedLimiterStats snapshotSample(final int limit, final KeyedStore<K> store) {
        if (limit < 0) {
            throw new IllegalArgumentException("limit must be >= 0");
        }

        final Map<K, Bucket> bucketSnapshot = store.snapshot(limit);
        final int size = store.size();

        final Map<K, LimiterStats> statsMap = HashMap.newHashMap(bucketSnapshot.size());
        for (final Map.Entry<K, Bucket> entry : bucketSnapshot.entrySet()) {
            statsMap.put(entry.getKey(), entry.getValue().snapshot());
        }

        return new KeyedLimiterStats(size, Collections.unmodifiableMap(statsMap));
    }
}
