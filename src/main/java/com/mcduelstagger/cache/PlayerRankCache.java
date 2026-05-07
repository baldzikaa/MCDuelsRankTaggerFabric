package com.mcduelstagger.cache;

import com.mcduelstagger.rank.Kit;
import com.mcduelstagger.rank.Rank;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public final class PlayerRankCache {
    public static final Duration TTL_HIT  = Duration.ofMinutes(10);
    public static final Duration TTL_MISS = Duration.ofMinutes(30);
    public static final Duration TTL_FAILED_BASE = Duration.ofSeconds(60);
    public static final Duration TTL_FAILED_CAP  = Duration.ofMinutes(5);

    private final ConcurrentHashMap<UUID, CacheEntry> entries = new ConcurrentHashMap<>();
    private final Supplier<Instant> clock;

    public PlayerRankCache() { this(Instant::now); }
    public PlayerRankCache(Supplier<Instant> clock) { this.clock = clock; }

    public Optional<CacheEntry> get(UUID uuid) {
        CacheEntry e = entries.get(uuid);
        if (e == null) return Optional.empty();
        if (e.isExpired(clock.get())) {
            entries.remove(uuid, e);
            return Optional.empty();
        }
        return Optional.of(e);
    }

    public void putHit(UUID uuid, Rank rank, Kit kit) {
        Instant now = clock.get();
        entries.put(uuid, new CacheEntry(
            CacheStatus.HIT, rank, kit, now, now.plus(TTL_HIT), 0));
    }

    public void putMiss(UUID uuid) {
        Instant now = clock.get();
        entries.put(uuid, new CacheEntry(
            CacheStatus.MISS, null, null, now, now.plus(TTL_MISS), 0));
    }

    public void putFailed(UUID uuid) {
        Instant now = clock.get();
        // Atomic compute so concurrent failures for the same UUID don't lose backoff increments.
        entries.compute(uuid, (k, prev) -> {
            int prevFailures = (prev != null && prev.status() == CacheStatus.FAILED) ? prev.failureCount() : 0;
            int next = prevFailures + 1;
            long seconds = Math.min(
                TTL_FAILED_CAP.getSeconds(),
                TTL_FAILED_BASE.getSeconds() * (1L << Math.min(next - 1, 4)));
            return new CacheEntry(CacheStatus.FAILED, null, null, now, now.plusSeconds(seconds), next);
        });
    }

    public void clear() { entries.clear(); }

    public void evictExpired() {
        Instant now = clock.get();
        entries.entrySet().removeIf(e -> e.getValue().isExpired(now));
    }

    /** Snapshot of current entries for the persistence layer — caller may iterate freely. */
    public java.util.Map<UUID, CacheEntry> snapshotForPersistence() {
        return java.util.Map.copyOf(entries);
    }

    public void loadAll(java.util.Map<UUID, CacheEntry> snapshot) {
        Instant now = clock.get();
        snapshot.forEach((k, v) -> {
            if (!v.isExpired(now) && v.status() != CacheStatus.FAILED) entries.put(k, v);
        });
    }
}
