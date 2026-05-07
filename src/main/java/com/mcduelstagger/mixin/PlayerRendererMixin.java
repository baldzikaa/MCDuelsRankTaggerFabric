package com.mcduelstagger.mixin;

import com.mcduelstagger.ModEntry;
import com.mcduelstagger.config.ConfigHolder;
import com.mcduelstagger.rank.TierPicker;
import com.mcduelstagger.render.NameTagFormatter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.text.MutableText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

/**
 * In 1.21.4 Mojang refactored entity rendering to consume an EntityRenderState (a snapshot
 * computed from the entity each frame) instead of taking the entity directly. The right place
 * to influence the floating nametag is now {@link PlayerEntityRenderer#updateRenderState}: it's
 * called once per frame per visible player, has access to both the live entity AND the state,
 * and the state's {@code displayName} field is what the renderer ultimately draws above the head.
 *
 * We let the vanilla method populate the state, then prepend our rank glyph to displayName.
 */
@Mixin(PlayerEntityRenderer.class)
public abstract class PlayerRendererMixin {

    @Inject(method = "updateRenderState(Lnet/minecraft/client/network/AbstractClientPlayerEntity;Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;F)V",
            at = @At("TAIL"))
    private void mcduelstagger$prefixDisplayName(AbstractClientPlayerEntity player,
                                                 PlayerEntityRenderState state,
                                                 float tickDelta,
                                                 CallbackInfo ci) {
        if (state.displayName == null) return;

        var cfg = ConfigHolder.get();
        if (cfg == null || !cfg.enabled) return;
        if (state.sneaking && !cfg.showOnSneaking) return;

        var mc = MinecraftClient.getInstance();
        if (!cfg.showOnSelf && player == mc.player) return;
        if (mc.getNetworkHandler() == null) return;
        if (mc.getNetworkHandler().getPlayerListEntry(player.getUuid()) == null) return;

        var svc = ModEntry.lookupService();
        if (svc == null) return;

        Optional<TierPicker.Result> r = svc.lookup(player.getUuid(), player.getGameProfile().getName());
        if (r.isEmpty()) return;

        MutableText combined = NameTagFormatter.prefix(r.get()).copy();
        combined.append(state.displayName);
        state.displayName = combined;
    }
}
