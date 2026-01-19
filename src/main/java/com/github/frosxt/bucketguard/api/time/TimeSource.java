package com.github.frosxt.bucketguard.api.time;

/**
 * Abstraction for time measurement.
 * <p>
 * Defaults to {@link System#nanoTime()}.
 */
@FunctionalInterface
public interface TimeSource {

    /**
     * @return the current value of the running time source, in nanoseconds.
     */
    long nanoTime();

    /**
     * @return the system time source using {@link System#nanoTime()}.
     */
    static TimeSource system() {
        return System::nanoTime;
    }
}
