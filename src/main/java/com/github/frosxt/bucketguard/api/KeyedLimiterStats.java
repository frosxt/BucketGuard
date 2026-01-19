package com.github.frosxt.bucketguard.api;

import java.util.Map;

/**
 * A snapshot of a keyed rate limiter's state.
 *
 * @param keyCount total number of keys currently tracked (approximate)
 * @param samples  a sample of stats for specific keys (could be empty or limited)
 */
public record KeyedLimiterStats(int keyCount, Map<?, LimiterStats> samples) {
}
