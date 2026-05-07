package com.mcduelstagger.cache;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mcduelstagger.rank.Kit;
import com.mcduelstagger.rank.Rank;

import com.mcduelstagger.ModEntry;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class CachePersistence {
    public static final int VERSION = 1;
    private static final Gson GSON = new Gson();

    private CachePersistence() {}

    public static void save(Path file, Map<UUID, CacheEntry> entries) {
        JsonObject root = new JsonObject();
        root.addProperty("version", VERSION);
        JsonObject map = new JsonObject();
        entries.forEach((uuid, e) -> {
            if (e.status() == CacheStatus.FAILED) return;
            JsonObject o = new JsonObject();
            o.addProperty("status", e.status().name());
            if (e.rank() != null) o.addProperty("rank", e.rank().name());
            if (e.kit()  != null) o.addProperty("kit",  e.kit().name());
            o.addProperty("fetchedAt", e.fetchedAt().toEpochMilli());
            o.addProperty("expiresAt", e.expiresAt().toEpochMilli());
            map.add(uuid.toString(), o);
        });
        root.add("entries", map);
        try {
            Path parent = file.getParent();
            if (parent != null) Files.createDirectories(parent);
            Path tmp = file.resolveSibling(file.getFileName() + ".tmp");
            Files.writeString(tmp, GSON.toJson(root));
            try {
                Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException atomicUnsupported) {
                // Network share / oddball FS — fall back to a plain replace.
                Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException ioe) {
            ModEntry.LOG.warn("cache save failed", ioe);
        }
    }

    public static Map<UUID, CacheEntry> load(Path file) {
        if (!Files.exists(file)) return Map.of();
        try {
            String body = Files.readString(file);
            JsonObject root = GSON.fromJson(body, JsonObject.class);
            if (root == null || !root.has("version") || root.get("version").getAsInt() != VERSION) return Map.of();
            JsonObject entries = root.getAsJsonObject("entries");
            if (entries == null) return Map.of();
            Map<UUID, CacheEntry> out = new HashMap<>();
            for (var entry : entries.entrySet()) {
                UUID uuid = UUID.fromString(entry.getKey());
                JsonObject o = entry.getValue().getAsJsonObject();
                CacheStatus status = CacheStatus.valueOf(o.get("status").getAsString());
                Rank rank = o.has("rank") ? Rank.valueOf(o.get("rank").getAsString()) : null;
                Kit  kit  = o.has("kit")  ? Kit .valueOf(o.get("kit").getAsString())  : null;
                Instant fetchedAt = Instant.ofEpochMilli(o.get("fetchedAt").getAsLong());
                Instant expiresAt = Instant.ofEpochMilli(o.get("expiresAt").getAsLong());
                out.put(uuid, new CacheEntry(status, rank, kit, fetchedAt, expiresAt, 0));
            }
            return out;
        } catch (IOException | JsonSyntaxException | IllegalStateException | NullPointerException | IllegalArgumentException e) {
            return Map.of();
        }
    }
}
