package com.mcduelstagger.rank;

import com.mcduelstagger.api.KitEntry;
import com.mcduelstagger.api.Profile;

import java.util.*;

public final class TierPicker {
    public record Result(Rank rank, Kit kit) {}

    private TierPicker() {}

    public static Optional<Result> pick(Profile profile, Set<Kit> allowed) {
        if (profile == null || profile.kits() == null || profile.kits().isEmpty()) return Optional.empty();
        if (allowed == null || allowed.isEmpty()) return Optional.empty();

        record Candidate(Kit kit, Rank rank, int elo) {}
        List<Candidate> candidates = new ArrayList<>();
        for (Map.Entry<String, KitEntry> e : profile.kits().entrySet()) {
            Optional<Kit> k = Kit.fromApi(e.getKey());
            if (k.isEmpty() || !allowed.contains(k.get())) continue;
            Optional<Rank> r = Rank.fromApi(e.getValue().rank());
            if (r.isEmpty()) continue;
            candidates.add(new Candidate(k.get(), r.get(), e.getValue().elo()));
        }
        if (candidates.isEmpty()) return Optional.empty();

        candidates.sort(
            Comparator.<Candidate>comparingInt(c -> c.rank.tier())
                .thenComparing(Comparator.comparingInt((Candidate c) -> c.elo).reversed())
                .thenComparing(c -> c.kit.apiId())
        );
        Candidate best = candidates.get(0);
        return Optional.of(new Result(best.rank, best.kit));
    }
}
