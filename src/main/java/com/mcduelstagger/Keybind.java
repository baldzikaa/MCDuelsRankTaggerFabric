package com.mcduelstagger;

import com.mcduelstagger.config.ConfigHolder;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public final class Keybind {
    private static KeyBinding TOGGLE;

    private Keybind() {}

    public static void register() {
        TOGGLE = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.mcduelstagger.toggle",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_UNKNOWN,
            "category.mcduelstagger"
        ));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (TOGGLE.wasPressed()) onToggle(client);
        });
    }

    private static void onToggle(MinecraftClient client) {
        var cfg = ConfigHolder.get();
        cfg.enabled = !cfg.enabled;
        // Save asynchronously so spamming the keybind doesn't block the render thread on FS I/O.
        com.mcduelstagger.ModEntry.scheduleConfigSave();
        SystemToast.show(client.getToastManager(),
            SystemToast.Type.PERIODIC_NOTIFICATION,
            Text.translatable(cfg.enabled ? "toast.mcduelstagger.on" : "toast.mcduelstagger.off"),
            Text.empty());
    }
}
