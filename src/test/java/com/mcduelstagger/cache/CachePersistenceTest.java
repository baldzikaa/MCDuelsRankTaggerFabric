package com.mcduelstagger.cache;

import com.mcduelstagger.rank.Kit;
import com.mcduelstagger.rank.Rank;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CachePersistenceTest {
    @Test void roundTripHitAndMiss(@TempDir Path tmp) throws Exception {
        Path file = tmp.resolve("cache.json");
        UUID a = UUID.randomUUID(), b = UUID.randomUUID();
        Instant t = Instant.ofEpochSecond(1_700_000_000);

        var entries = new java.util.concurrent.ConcurrentHashMap<UUID, CacheEntry>();
        entries.put(a, new CacheEntry(CacheStatus.HIT, Rank.HT1, Kit.CRYSTAL, t, t.plusSeconds(3600), 0));
        entries.put(b, new CacheEntry(CacheStatus.MISS, null, null, t, t.plusSeconds(3600), 0));
        entries.put(UUID.randomUUID(), new CacheEntry(CacheStatus.FAILED, null, null, t, t.plusSeconds(60), 2));

        CachePersistence.save(file, entries);
        Map<UUID, CacheEntry> loaded = CachePersistence.load(file);

        assertEquals(2, loaded.size(), "FAILED entries must not be persisted");
        assertEquals(Rank.HT1, loaded.get(a).rank());
        assertEquals(CacheStatus.MISS, loaded.get(b).status());
    }
    @Test void corruptFileReturnsEmptyMap(@TempDir Path tmp) throws Exception {
        Path file = tmp.resolve("cache.json");
        Files.writeString(file, "{not json");
        Map<UUID, CacheEntry> loaded = CachePersistence.load(file);
        assertTrue(loaded.isEmpty());
    }
    @Test void missingFileReturnsEmptyMap(@TempDir Path tmp) {
        assertTrue(CachePersistence.load(tmp.resolve("nope.json")).isEmpty());
    }
    @Test void unknownVersionReturnsEmptyMap(@TempDir Path tmp) throws Exception {
        Path file = tmp.resolve("cache.json");
        Files.writeString(file, "{\"version\":99,\"entries\":{}}");
        assertTrue(CachePersistence.load(file).isEmpty());
    }
}
