package com.github.frosxt.bucketguard.runtime.key.store.exact;

import com.github.frosxt.bucketguard.runtime.bucket.Bucket;

/**
 * Entry holding bucket and metadata.
 * 
 * @param <K> key type
 */
public final class StoreEntry<K> {
    final K key;
    final Bucket bucket;
    volatile long lastAccessNanos;

    /**
     * Creates a new entry.
     * 
     * @param key    key
     * @param bucket bucket
     * @param now    access time
     */
    public StoreEntry(final K key, final Bucket bucket, final long now) {
        this.key = key;
        this.bucket = bucket;
        this.lastAccessNanos = now;
    }

    /**
     * Updates access time.
     * 
     * @param now current time
     */
    public void touch(final long now) {
        this.lastAccessNanos = now;
    }

    /** @return the bucket */
    public Bucket bucket() {
        return bucket;
    }

    /** @return the key */
    public K key() {
        return key;
    }

    /** @return last access time */
    public long lastAccessNanos() {
        return lastAccessNanos;
    }
}
