package com.mcduelstagger.config;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;

public final class ConfigHolder {
    private static me.shedaniel.autoconfig.ConfigHolder<ModConfig> HOLDER;

    private ConfigHolder() {}

    public static void register() {
        HOLDER = AutoConfig.register(ModConfig.class, GsonConfigSerializer::new);
    }
    /** Returns the loaded config, or null if {@link #register()} hasn't been called yet. */
    public static ModConfig get()  { return HOLDER == null ? null : HOLDER.getConfig(); }
    public static void save()      { if (HOLDER != null) HOLDER.save(); }
}
