package com.github.frosxt.bucketguard.api.spec;

/**
 * Policy for evicting keys from a keyed rate limiter.
 */
public enum EvictionPolicy {
    /**
     * No eviction policy.
     * Note: This requires bounded capacity via maxKeys to prevent out-of-memory
     * errors
     * if the key set is unbounded.
     */
    NONE,
    /**
     * Least Recently Used eviction.
     */
    LRU,
    /**
     * Expire keys after a duration of inactivity (access).
     */
    EXPIRE_AFTER_ACCESS
}
