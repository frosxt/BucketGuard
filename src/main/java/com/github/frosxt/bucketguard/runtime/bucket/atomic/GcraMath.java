package com.github.frosxt.bucketguard.runtime.bucket.atomic;

/**
 * Math utilities for Generic Cell Rate Algorithm (GCRA).
 */
public final class GcraMath {

    private GcraMath() {
        throw new UnsupportedOperationException("This class cannot be instantiated!");
    }

    public static long calculateCostNanos(final long tokens, final long emissionIntervalNanos, final boolean strictMath) {
        if (strictMath) {
            try {
                return Math.multiplyExact(tokens, emissionIntervalNanos);
            } catch (final ArithmeticException e) {
                throw new IllegalArgumentException("Token request overflow", e);
            }
        }

        return tokens * emissionIntervalNanos;
    }

    public static long calculatePotentialTat(final long baseTime, final long costNanos, final boolean strictMath) {
        if (strictMath) {
            try {
                return Math.addExact(baseTime, costNanos);
            } catch (final ArithmeticException e) {
                throw new IllegalStateException("Time overflow", e);
            }
        }

        return baseTime + costNanos;
    }
}
