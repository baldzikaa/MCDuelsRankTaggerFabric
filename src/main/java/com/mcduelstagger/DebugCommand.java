package com.mcduelstagger;

import com.mcduelstagger.cache.CacheStatus;
import com.mcduelstagger.cache.PlayerRankCache;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;

import java.util.UUID;

/** Dev-time client command: /mcduelstagger lookup <name>. Useful for smoke-testing the cache + API path. */
public final class DebugCommand {
    private DebugCommand() {}

    public static void register(PlayerRankCache cache) {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
            dispatcher.register(ClientCommandManager.literal("mcduelstagger")
                .then(ClientCommandManager.literal("lookup")
                    .then(ClientCommandManager.argument("name", StringArgumentType.string())
                        .executes(ctx -> {
                            String name = StringArgumentType.getString(ctx, "name");
                            UUID uuid = resolveUuid(name);

                            // Cache hit on the spot — print and done.
                            var existing = cache.get(uuid);
                            if (existing.isPresent()) {
                                ctx.getSource().sendFeedback(Text.literal(formatEntry(existing.get())));
                                return 1;
                            }

                            // Cache miss: kick off the fetch, then poll briefly for the result.
                            ctx.getSource().sendFeedback(Text.literal("Looking up " + name + "..."));
                            var svc = ModEntry.lookupService();
                            if (svc == null) {
                                ctx.getSource().sendFeedback(Text.literal("Service not ready."));
                                return 0;
                            }
                            svc.lookup(uuid, name);

                            // Wait for the in-flight future off the render thread, then post the
                            // result back on the render thread (chat must be sent there).
                            Thread waiter = new Thread(() -> {
                                try {
                                    svc.awaitInFlight(uuid, 10_000);
                                } catch (Exception ignored) {
                                    // Timed out or threw — fall through to read whatever's cached.
                                }
                                MinecraftClient mc = MinecraftClient.getInstance();
                                mc.execute(() -> {
                                    ClientPlayerEntity self = mc.player;
                                    if (self == null) return;
                                    String msg = cache.get(uuid)
                                        .map(DebugCommand::formatEntry)
                                        .orElse("Lookup timed out for " + name + ".");
                                    self.sendMessage(Text.literal(msg), false);
                                });
                            }, "mcduelstagger-lookup-waiter");
                            waiter.setDaemon(true);
                            waiter.start();
                            return 1;
                        })))));
    }

    private static String formatEntry(com.mcduelstagger.cache.CacheEntry e) {
        if (e.status() == CacheStatus.HIT) return "Cached HIT: " + e.rank() + " " + e.kit();
        if (e.status() == CacheStatus.MISS) return "Player not found on MCDuels (cached MISS).";
        return "Cached " + e.status();
    }

    private static UUID resolveUuid(String name) {
        var mc = MinecraftClient.getInstance();
        if (mc.getNetworkHandler() != null) {
            for (var p : mc.getNetworkHandler().getPlayerList()) {
                if (p.getProfile().name().equalsIgnoreCase(name)) {
                    return p.getProfile().id();
                }
            }
        }
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes());
    }
}
