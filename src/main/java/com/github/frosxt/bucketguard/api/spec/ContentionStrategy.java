package com.github.frosxt.bucketguard.api.spec;

/**
 * Strategy for handling concurrency contention in the bucket state.
 */
public enum ContentionStrategy {
    /**
     * Automatically choose the strategy based on the environment or heuristics.
     */
    AUTO,
    /**
     * Use atomic operations (CAS) on a single shared state.
     * Suitable for low-to-moderate contention.
     */
    ATOMIC,
    /**
     * Use striped locking or sharded state to reduce CAS failure rates.
     * Suitable for high contention.
     */
    STRIPED
}
