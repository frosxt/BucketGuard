package com.github.frosxt.bucketguard.runtime.bucket;

import com.github.frosxt.bucketguard.api.Permit;

/**
 * A simple immutable implementation of {@link Permit}.
 *
 * @param granted         whether the permit was granted
 * @param tokensRequested number of tokens requested
 * @param remainingTokens tokens remaining (if granted)
 * @param retryAfterNanos nanoseconds to wait before retry (if not granted)
 */
public record SimplePermit(boolean granted, long tokensRequested, long remainingTokens, long retryAfterNanos) implements Permit {
}