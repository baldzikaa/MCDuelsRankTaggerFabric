package com.mcduelstagger.config;

import com.mcduelstagger.ModEntry;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public final class ModMenuEntry implements ModMenuApi {
    @Override public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> com.mcduelstagger.config.ConfigScreen.build(parent, ModEntry::lookupService);
    }
}
