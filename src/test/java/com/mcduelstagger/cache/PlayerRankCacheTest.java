package com.mcduelstagger.cache;

import com.mcduelstagger.rank.Kit;
import com.mcduelstagger.rank.Rank;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PlayerRankCacheTest {
    private final UUID U = UUID.randomUUID();

    @Test void absentByDefault() {
        var c = new PlayerRankCache(() -> Instant.EPOCH);
        assertTrue(c.get(U).isEmpty());
    }
    @Test void hitRoundtrip() {
        var c = new PlayerRankCache(() -> Instant.ofEpochSecond(1000));
        c.putHit(U, Rank.HD, Kit.CRYSTAL);
        var e = c.get(U).orElseThrow();
        assertEquals(CacheStatus.HIT, e.status());
        assertEquals(Rank.HD, e.rank());
        assertEquals(Kit.CRYSTAL, e.kit());
    }
    @Test void hitExpiresAfterTenMinutes() {
        long[] now = { 1000_000L };
        var c = new PlayerRankCache(() -> Instant.ofEpochSecond(now[0]));
        c.putHit(U, Rank.HD, Kit.CRYSTAL);
        now[0] += Duration.ofMinutes(9).toSeconds();
        assertTrue(c.get(U).isPresent());
        now[0] += Duration.ofMinutes(2).toSeconds();
        assertTrue(c.get(U).isEmpty());
    }
    @Test void missExpiresAfterThirtyMinutes() {
        long[] now = { 0L };
        var c = new PlayerRankCache(() -> Instant.ofEpochSecond(now[0]));
        c.putMiss(U);
        now[0] += Duration.ofMinutes(29).toSeconds();
        assertEquals(CacheStatus.MISS, c.get(U).orElseThrow().status());
        now[0] += Duration.ofMinutes(2).toSeconds();
        assertTrue(c.get(U).isEmpty());
    }
    @Test void failedBackoffDoubles() {
        long[] now = { 0L };
        var c = new PlayerRankCache(() -> Instant.ofEpochSecond(now[0]));
        c.putFailed(U);
        assertEquals(CacheStatus.FAILED, c.get(U).orElseThrow().status());
        now[0] += 30;
        assertTrue(c.get(U).isPresent());
        now[0] += 31;
        assertTrue(c.get(U).isEmpty());

        c.putFailed(U); c.putFailed(U); c.putFailed(U); c.putFailed(U);
        assertEquals(300, c.get(U).orElseThrow().expiresAt().getEpochSecond() - now[0]);
    }
    @Test void putHitAfterFailedResetsFailureCount() {
        long[] now = { 0L };
        var c = new PlayerRankCache(() -> Instant.ofEpochSecond(now[0]));
        c.putFailed(U); c.putFailed(U); c.putFailed(U);
        c.putHit(U, Rank.I, Kit.AXE);
        c.putFailed(U);
        assertEquals(60, c.get(U).orElseThrow().expiresAt().getEpochSecond() - now[0]);
    }
    @Test void evictExpired() {
        long[] now = { 0L };
        var c = new PlayerRankCache(() -> Instant.ofEpochSecond(now[0]));
        UUID a = UUID.randomUUID(), b = UUID.randomUUID();
        c.putMiss(a);
        now[0] += Duration.ofMinutes(31).toSeconds();
        c.putMiss(b);
        c.evictExpired();
        assertTrue(c.get(a).isEmpty());
        assertTrue(c.get(b).isPresent());
    }
}
