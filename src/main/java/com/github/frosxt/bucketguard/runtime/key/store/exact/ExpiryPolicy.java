package com.github.frosxt.bucketguard.runtime.key.store.exact;

import com.github.frosxt.bucketguard.api.time.TimeSource;

/**
 * Encapsulates expiration logic.
 */
public final class ExpiryPolicy {
    private final long expireNanos;
    private final TimeSource timeSource;

    /**
     * Creates an expiry policy.
     * 
     * @param expireNanos duration in nanos, 0 or less means disabled
     * @param timeSource  source of time
     */
    public ExpiryPolicy(final long expireNanos, final TimeSource timeSource) {
        this.expireNanos = expireNanos;
        this.timeSource = timeSource;
    }

    /**
     * @return current time in nanos
     */
    public long now() {
        return timeSource.nanoTime();
    }

    /**
     * Checks if expired.
     * 
     * @param lastAccessNanos time of last access
     * @param now             current time
     * @return true if expired
     */
    public boolean isExpired(final long lastAccessNanos, final long now) {
        if (expireNanos <= 0) {
            return false;
        }
        return (now - lastAccessNanos) >= expireNanos;
    }

    public boolean isEnabled() {
        return expireNanos > 0;
    }
}
