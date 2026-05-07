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
    private static volatile McDuelsClient CLIENT;

    public static RankLookupService lookupService() { return SERVICE; }

    /** Save the config off the render thread. Cheap and idempotent — safe to call from anywhere. */
    public static void scheduleConfigSave() {
        ScheduledExecutorService s = SCHEDULER;
        if (s == null) return;
        s.submit(() -> {
            try { ConfigHolder.save(); }
            catch (Throwable t) { LOG.warn("config save failed", t); }
        });
    }

    @Override public void onInitializeClient() {
        ConfigHolder.register();

        CACHE_FILE = FabricLoader.getInstance().getConfigDir()
            .resolve(MOD_ID).resolve("cache.json");
        CACHE = new PlayerRankCache();
        CACHE.loadAll(CachePersistence.load(CACHE_FILE));

        CLIENT = new McDuelsClient();
        SERVICE = new RankLookupService(CLIENT, CACHE, ConfigHolder.get().allowedKits());

        Keybind.register();
        DebugCommand.register(CACHE);

        SCHEDULER = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, MOD_ID + "-scheduler"); t.setDaemon(true); return t;
        });
        SCHEDULER.scheduleAtFixedRate(() -> {
            try { CachePersistence.save(CACHE_FILE, CACHE.snapshotForPersistence()); }
            catch (Throwable t) { LOG.warn("cache save failed", t); }
        }, 30, 30, TimeUnit.SECONDS);
        SCHEDULER.scheduleAtFixedRate(CACHE::evictExpired, 5, 5, TimeUnit.MINUTES);

        ClientLifecycleEvents.CLIENT_STOPPING.register(c -> {
            // Run the final save on the scheduler thread (off the render thread) and wait
            // briefly so the cache is durable before we tear the executor down.
            try {
                SCHEDULER.submit(() -> CachePersistence.save(CACHE_FILE, CACHE.snapshotForPersistence()))
                         .get(2, TimeUnit.SECONDS);
            } catch (Throwable t) {
                LOG.warn("final cache save failed", t);
            }
            SCHEDULER.shutdownNow();
            CLIENT.close();
        });

        LOG.info("MCDuels Rank Tagger initialized");
    }
}
