package com.mcduelstagger;

import com.mcduelstagger.api.McDuelsClient;
import com.mcduelstagger.cache.CachePersistence;
import com.mcduelstagger.cache.PlayerRankCache;
import com.mcduelstagger.config.ConfigHolder;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class ModEntry implements ClientModInitializer {
    public static final String MOD_ID = "mcduelstagger";
    public static final Logger LOG = LoggerFactory.getLogger(MOD_ID);

    private static volatile RankLookupService SERVICE;
    private static volatile PlayerRankCache CACHE;
    private static volatile Path CACHE_FILE;
    private static volatile ScheduledExecutorService SCHEDULER;

    public static RankLookupService lookupService() { return SERVICE; }

    @Override public void onInitializeClient() {
        ConfigHolder.register();

        CACHE_FILE = FabricLoader.getInstance().getConfigDir()
            .resolve(MOD_ID).resolve("cache.json");
        CACHE = new PlayerRankCache();
        CACHE.loadAll(CachePersistence.load(CACHE_FILE));

        var client = new McDuelsClient();
        SERVICE = new RankLookupService(client, CACHE, ConfigHolder.get().allowedKits());

        Keybind.register();
        DebugCommand.register(CACHE);

        SCHEDULER = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, MOD_ID + "-scheduler"); t.setDaemon(true); return t;
        });
        SCHEDULER.scheduleAtFixedRate(() -> {
            try { CachePersistence.save(CACHE_FILE, CACHE.rawForPersistence()); }
            catch (Throwable t) { LOG.warn("cache save failed", t); }
        }, 30, 30, TimeUnit.SECONDS);
        SCHEDULER.scheduleAtFixedRate(CACHE::evictExpired, 5, 5, TimeUnit.MINUTES);

        ClientLifecycleEvents.CLIENT_STOPPING.register(c -> {
            try { CachePersistence.save(CACHE_FILE, CACHE.rawForPersistence()); } catch (Throwable ignored) {}
            SCHEDULER.shutdownNow();
        });

        LOG.info("MCDuels Rank Tagger initialized");
    }
}
