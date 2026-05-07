package com.mcduelstagger;

import com.mcduelstagger.api.McDuelsClient;
import com.mcduelstagger.cache.CacheEntry;
import com.mcduelstagger.cache.CacheStatus;
import com.mcduelstagger.cache.PlayerRankCache;
import com.mcduelstagger.rank.Kit;
import com.mcduelstagger.rank.TierPicker;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public final class RankLookupService {
    private final McDuelsClient client;
    private final PlayerRankCache cache;
    private volatile Set<Kit> allowedKits;
    private final ConcurrentHashMap<UUID, CompletableFuture<Void>> inFlight = new ConcurrentHashMap<>();

    public RankLookupService(McDuelsClient client, PlayerRankCache cache, Set<Kit> allowedKits) {
        this.client = client;
        this.cache = cache;
        this.allowedKits = EnumSet.copyOf(allowedKits);
    }

    public void setAllowedKits(Set<Kit> allowed) { this.allowedKits = EnumSet.copyOf(allowed); }

    public Optional<TierPicker.Result> lookup(UUID uuid, String username) {
        Optional<CacheEntry> existing = cache.get(uuid);
        if (existing.isPresent()) {
            CacheEntry e = existing.get();
            if (e.status() == CacheStatus.HIT)
                return Optional.of(new TierPicker.Result(e.rank(), e.kit()));
            return Optional.empty();
        }
        triggerFetch(uuid, username);
        return Optional.empty();
    }

    public void awaitInFlight(UUID uuid, long timeoutMs) throws Exception {
        CompletableFuture<Void> f = inFlight.get(uuid);
        if (f != null) f.get(timeoutMs, java.util.concurrent.TimeUnit.MILLISECONDS);
    }

    private void triggerFetch(UUID uuid, String username) {
        inFlight.computeIfAbsent(uuid, k -> {
            CompletableFuture<Void> done = client.fetchByUsername(username)
                .handle((profile, err) -> {
                    if (err != null) {
                        cache.putFailed(uuid);
                    } else if (profile.isEmpty()) {
                        cache.putMiss(uuid);
                    } else {
                        Optional<TierPicker.Result> r = TierPicker.pick(profile.get(), allowedKits);
                        if (r.isPresent()) cache.putHit(uuid, r.get().rank(), r.get().kit());
                        else               cache.putMiss(uuid);
                    }
                    return null;
                });
            done.whenComplete((v, t) -> inFlight.remove(uuid));
            return done;
        });
    }

    public void clear() { cache.clear(); inFlight.clear(); }
}
