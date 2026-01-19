package com.github.frosxt.bucketguard.api;

import java.time.Duration;

/**
 * A snapshot of a rate limiter's state.
 *
 * @param capacity        maximum tokens in the bucket
 * @param availableTokens currently available tokens
 * @param refillTokens    tokens added per period
 * @param refillPeriod    period for refill
 */
public record LimiterStats(long capacity, long availableTokens, long refillTokens, Duration refillPeriod) {
}
