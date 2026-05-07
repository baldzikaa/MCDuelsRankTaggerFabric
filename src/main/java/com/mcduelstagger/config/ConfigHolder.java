package com.mcduelstagger.config;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;

public final class ConfigHolder {
    private static me.shedaniel.autoconfig.ConfigHolder<ModConfig> HOLDER;

    private ConfigHolder() {}

    public static void register() {
        HOLDER = AutoConfig.register(ModConfig.class, GsonConfigSerializer::new);
    }
    public static ModConfig get()  { return HOLDER.getConfig(); }
    public static void save()      { HOLDER.save(); }
}
