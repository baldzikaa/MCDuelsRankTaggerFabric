package com.mcduelstagger.cache;

import com.mcduelstagger.rank.Kit;
import com.mcduelstagger.rank.Rank;

import java.time.Instant;

public record CacheEntry(
    CacheStatus status,
    Rank rank,
    Kit kit,
    Instant fetchedAt,
    Instant expiresAt,
    int failureCount
) {
    public boolean isExpired(Instant now) { return !now.isBefore(expiresAt); }
}
