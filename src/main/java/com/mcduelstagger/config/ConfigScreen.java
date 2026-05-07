package com.mcduelstagger.config;

import com.mcduelstagger.RankLookupService;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.function.Supplier;

public final class ConfigScreen {
    private ConfigScreen() {}

    public static Screen build(Screen parent, Supplier<RankLookupService> serviceSupplier) {
        ModConfig cfg = ConfigHolder.get();
        ConfigBuilder b = ConfigBuilder.create()
            .setParentScreen(parent)
            .setTitle(Text.translatable("config.mcduelstagger.title"))
            .setSavingRunnable(() -> {
                ConfigHolder.save();
                var svc = serviceSupplier.get();
                if (svc != null) svc.setAllowedKits(cfg.allowedKits());
            });

        ConfigEntryBuilder e = b.entryBuilder();

        ConfigCategory general = b.getOrCreateCategory(Text.translatable("config.mcduelstagger.general"));
        general.addEntry(e.startBooleanToggle(Text.translatable("config.mcduelstagger.enabled"), cfg.enabled).setDefaultValue(true).setSaveConsumer(v -> cfg.enabled = v).build());
        general.addEntry(e.startBooleanToggle(Text.translatable("config.mcduelstagger.showOnSelf"), cfg.showOnSelf).setDefaultValue(true).setSaveConsumer(v -> cfg.showOnSelf = v).build());
        general.addEntry(e.startBooleanToggle(Text.translatable("config.mcduelstagger.showOnSneaking"), cfg.showOnSneaking).setDefaultValue(false).setSaveConsumer(v -> cfg.showOnSneaking = v).build());

        ConfigCategory kits = b.getOrCreateCategory(Text.translatable("config.mcduelstagger.kits"));
        kits.addEntry(e.startBooleanToggle(Text.translatable("config.mcduelstagger.kit.crystal"), cfg.kitCrystal).setDefaultValue(true).setSaveConsumer(v -> cfg.kitCrystal = v).build());
        kits.addEntry(e.startBooleanToggle(Text.translatable("config.mcduelstagger.kit.axe"),     cfg.kitAxe)    .setDefaultValue(true).setSaveConsumer(v -> cfg.kitAxe = v).build());
        kits.addEntry(e.startBooleanToggle(Text.translatable("config.mcduelstagger.kit.mace"),    cfg.kitMace)   .setDefaultValue(true).setSaveConsumer(v -> cfg.kitMace = v).build());
        kits.addEntry(e.startBooleanToggle(Text.translatable("config.mcduelstagger.kit.nethop"),  cfg.kitNethop) .setDefaultValue(true).setSaveConsumer(v -> cfg.kitNethop = v).build());
        kits.addEntry(e.startBooleanToggle(Text.translatable("config.mcduelstagger.kit.pot"),     cfg.kitPot)    .setDefaultValue(true).setSaveConsumer(v -> cfg.kitPot = v).build());
        kits.addEntry(e.startBooleanToggle(Text.translatable("config.mcduelstagger.kit.smp"),       cfg.kitSmp)      .setDefaultValue(true).setSaveConsumer(v -> cfg.kitSmp = v).build());
        kits.addEntry(e.startBooleanToggle(Text.translatable("config.mcduelstagger.kit.spearmace"), cfg.kitSpearMace).setDefaultValue(true).setSaveConsumer(v -> cfg.kitSpearMace = v).build());
        kits.addEntry(e.startBooleanToggle(Text.translatable("config.mcduelstagger.kit.sword"),     cfg.kitSword)    .setDefaultValue(true).setSaveConsumer(v -> cfg.kitSword = v).build());
        kits.addEntry(e.startBooleanToggle(Text.translatable("config.mcduelstagger.kit.uhc"),     cfg.kitUhc)    .setDefaultValue(true).setSaveConsumer(v -> cfg.kitUhc = v).build());

        ConfigCategory cache = b.getOrCreateCategory(Text.translatable("config.mcduelstagger.cache"));
        cache.addEntry(e.startBooleanToggle(Text.translatable("config.mcduelstagger.clearCache"), false)
            .setSaveConsumer(v -> { if (v) { var svc = serviceSupplier.get(); if (svc != null) svc.clear(); } })
            .build());

        return b.build();
    }
}
