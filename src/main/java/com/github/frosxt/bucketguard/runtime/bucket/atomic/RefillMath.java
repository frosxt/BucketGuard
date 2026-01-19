package com.github.frosxt.bucketguard.runtime.bucket.atomic;

import java.time.Duration;

/**
 * Math utilities for token bucket refill calculations.
 */
public final class RefillMath {

    private RefillMath() {
        throw new UnsupportedOperationException("This class cannot be instantiated!");
    }

    public static long calculateEmissionIntervalNanos(final long refillTokens, final Duration refillPeriod) {
        if (refillTokens > refillPeriod.toNanos()) {
            return 0;
        }

        return refillPeriod.toNanos() / refillTokens;
    }

    /**
     * Calculates burst offset.
     *
     * @param allowBurst            whether bursting is allowed
     * @param capacity              bucket capacity
     * @param emissionIntervalNanos emission interval
     * @param strictMath            if true, use overflow-safe math
     * @return burst offset in nanos
     */
    public static long calculateBurstOffsetNanos(final boolean allowBurst, final long capacity, final long emissionIntervalNanos, final boolean strictMath) {
        if (allowBurst) {
            if (strictMath) {
                try {
                    return Math.multiplyExact(capacity, emissionIntervalNanos);
                } catch (final ArithmeticException e) {
                    throw new IllegalArgumentException("Burst offset overflow (capacity * emissionInterval)", e);
                }
            }
            return capacity * emissionIntervalNanos;
        }

        return emissionIntervalNanos;
    }
}
