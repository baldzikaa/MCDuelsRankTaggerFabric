package com.mcduelstagger.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = "mcduelstagger")
public final class ModConfig implements ConfigData {
    public int configVersion = 1;

    @ConfigEntry.Category("general")
    public boolean enabled = true;
    @ConfigEntry.Category("general")
    public boolean showOnSelf = true;
    @ConfigEntry.Category("general")
    public boolean showOnSneaking = false;

    @ConfigEntry.Category("kits")
    public boolean kitCrystal = true;
    @ConfigEntry.Category("kits")
    public boolean kitAxe     = true;
    @ConfigEntry.Category("kits")
    public boolean kitMace    = true;
    @ConfigEntry.Category("kits")
    public boolean kitNethop  = true;
    @ConfigEntry.Category("kits")
    public boolean kitPot     = true;
    @ConfigEntry.Category("kits")
    public boolean kitSmp       = true;
    @ConfigEntry.Category("kits")
    public boolean kitSpearMace = true;
    @ConfigEntry.Category("kits")
    public boolean kitSword     = true;
    @ConfigEntry.Category("kits")
    public boolean kitUhc       = true;

    public java.util.EnumSet<com.mcduelstagger.rank.Kit> allowedKits() {
        var s = java.util.EnumSet.noneOf(com.mcduelstagger.rank.Kit.class);
        if (kitCrystal)   s.add(com.mcduelstagger.rank.Kit.CRYSTAL);
        if (kitAxe)       s.add(com.mcduelstagger.rank.Kit.AXE);
        if (kitMace)      s.add(com.mcduelstagger.rank.Kit.MACE);
        if (kitNethop)    s.add(com.mcduelstagger.rank.Kit.NETHOP);
        if (kitPot)       s.add(com.mcduelstagger.rank.Kit.POT);
        if (kitSmp)       s.add(com.mcduelstagger.rank.Kit.SMP);
        if (kitSpearMace) s.add(com.mcduelstagger.rank.Kit.SPEARMACE);
        if (kitSword)     s.add(com.mcduelstagger.rank.Kit.SWORD);
        if (kitUhc)       s.add(com.mcduelstagger.rank.Kit.UHC);
        return s;
    }
}
