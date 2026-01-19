package com.github.frosxt.bucketguard.runtime.key.store.exact;

import com.github.frosxt.bucketguard.api.time.TimeSource;
import com.github.frosxt.bucketguard.runtime.bucket.Bucket;
import com.github.frosxt.bucketguard.runtime.key.store.KeyedStore;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Adapter wrapping ExactKeyedStore as KeyedStore interface.
 *
 * @param <K> the key type
 */
public final class ExactKeyedStoreAdapter<K> implements KeyedStore<K> {
    private final ExactKeyedStore<K> delegate;

    public ExactKeyedStoreAdapter(final int maxKeys, final long expireNanos, final Consumer<K> removalListener, final TimeSource timeSource) {
        this.delegate = new ExactKeyedStore<>(maxKeys, expireNanos, removalListener, timeSource);
    }

    @Override
    public Bucket getOrCreate(final K key, final Supplier<Bucket> factory) {
        return delegate.getOrCreate(key, factory);
    }

    @Override
    public Bucket get(final K key) {
        return delegate.get(key);
    }

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public void prune() {
        delegate.prune();
    }

    @Override
    public Map<K, Bucket> snapshot(final int limit) {
        return delegate.snapshot(limit);
    }
}
