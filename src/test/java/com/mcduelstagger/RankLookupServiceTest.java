package com.mcduelstagger;

import com.mcduelstagger.api.KitEntry;
import com.mcduelstagger.api.McDuelsClient;
import com.mcduelstagger.api.Profile;
import com.mcduelstagger.cache.CacheStatus;
import com.mcduelstagger.cache.PlayerRankCache;
import com.mcduelstagger.rank.Kit;
import com.mcduelstagger.rank.Rank;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class RankLookupServiceTest {
    private static class StubClient extends McDuelsClient {
        final Map<String, Profile> table = new ConcurrentHashMap<>();
        final Set<String> failing = ConcurrentHashMap.newKeySet();
        final AtomicInteger calls = new AtomicInteger();
        StubClient() { super(java.net.URI.create("http://localhost")); }
        @Override public CompletableFuture<Optional<Profile>> fetchByUsername(String name) {
            calls.incrementAndGet();
            if (failing.contains(name))
                return CompletableFuture.failedFuture(new McDuelsClient.TransientApiException("boom"));
            return CompletableFuture.completedFuture(Optional.ofNullable(table.get(name)));
        }
    }

    @Test void firstCallTriggersFetchAndStoresHit() throws Exception {
        var stub = new StubClient();
        stub.table.put("alice", new Profile(UUID.randomUUID(), "alice",
            Map.of("crystal", new KitEntry("high_dueler", 2400, 1))));
        var cache = new PlayerRankCache();
        var svc = new RankLookupService(stub, cache, EnumSet.allOf(Kit.class));

        UUID u = UUID.randomUUID();
        assertTrue(svc.lookup(u, "alice").isEmpty(), "synchronous miss");
        svc.awaitInFlight(u, 2_000);
        var r = svc.lookup(u, "alice").orElseThrow();
        assertEquals(Rank.HD, r.rank());
        assertEquals(Kit.CRYSTAL, r.kit());
    }
    @Test void duplicateLookupsCoalesceIntoOneFetch() throws Exception {
        var stub = new StubClient();
        stub.table.put("bob", new Profile(UUID.randomUUID(), "bob",
            Map.of("axe", new KitEntry("gold", 1000, 1))));
        var svc = new RankLookupService(stub, new PlayerRankCache(), EnumSet.allOf(Kit.class));
        UUID u = UUID.randomUUID();
        for (int i = 0; i < 50; i++) svc.lookup(u, "bob");
        svc.awaitInFlight(u, 2_000);
        assertEquals(1, stub.calls.get());
    }
    @Test void notFoundCachesMiss() throws Exception {
        var stub = new StubClient();
        var cache = new PlayerRankCache();
        var svc = new RankLookupService(stub, cache, EnumSet.allOf(Kit.class));
        UUID u = UUID.randomUUID();
        svc.lookup(u, "ghost");
        svc.awaitInFlight(u, 2_000);
        assertEquals(CacheStatus.MISS, cache.get(u).orElseThrow().status());
    }
    @Test void transientErrorMarksFailed() throws Exception {
        var stub = new StubClient();
        stub.failing.add("err");
        var cache = new PlayerRankCache();
        var svc = new RankLookupService(stub, cache, EnumSet.allOf(Kit.class));
        UUID u = UUID.randomUUID();
        svc.lookup(u, "err");
        svc.awaitInFlight(u, 2_000);
        assertEquals(CacheStatus.FAILED, cache.get(u).orElseThrow().status());
    }
}
