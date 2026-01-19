package com.github.frosxt.bucketguard.runtime.wiring;

import com.github.frosxt.bucketguard.api.RateLimiter;
import com.github.frosxt.bucketguard.api.spec.TokenBucketSpec;
import com.github.frosxt.bucketguard.runtime.limiter.global.StandardRateLimiter;

import java.util.Objects;

public final class LimiterFactory {

    private LimiterFactory() {
        throw new UnsupportedOperationException("This class cannot be instantiated!");
    }

    /**
     * Creates a standard rate limiter.
     * 
     * @param spec config spec
     * @return the limiter
     */
    public static RateLimiter create(final TokenBucketSpec spec) {
        Objects.requireNonNull(spec, "spec");
        return new StandardRateLimiter(spec);
    }
}
