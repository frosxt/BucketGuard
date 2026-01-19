package com.github.frosxt.bucketguard.runtime.key.store.exact;

import java.util.function.Consumer;

/**
 * Handles removal listener dispatch.
 */
public final class RemovalDispatch<K> {
    private final Consumer<K> listener;

    /**
     * Creates a dispatcher.
     * 
     * @param listener the listener (can be null)
     */
    public RemovalDispatch(final Consumer<K> listener) {
        this.listener = listener;
    }

    /**
     * Fires the listener for the given key.
     * 
     * @param key the key
     */
    public void fire(final K key) {
        if (listener != null) {
            listener.accept(key);
        }
    }
}
