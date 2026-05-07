package com.mcduelstagger.rank;

import java.util.Optional;

public enum Kit {
    AXE(    "axe",     "axe",    0xE002),
    CRYSTAL("crystal", "cpvp",   0xE001),
    MACE(   "mace",    "mace",   0xE003),
    NETHOP( "nethop",  "nethop", 0xE004),
    POT(    "pot",     "pot",    0xE005),
    SMP(    "smp",     "smp",    0xE006),
    SPEARMACE("spear_mace", "smace", 0xE009),
    SWORD(  "sword",   "sword",  0xE007),
    UHC(    "uhc",     "uhc",    0xE008);

    private final String apiId;
    private final String abbrev;
    private final int glyphCodepoint;

    Kit(String apiId, String abbrev, int glyphCodepoint) {
        this.apiId = apiId;
        this.abbrev = abbrev;
        this.glyphCodepoint = glyphCodepoint;
    }
    public String apiId()         { return apiId; }
    public String abbrev()        { return abbrev; }
    public int    glyphCodepoint(){ return glyphCodepoint; }

    public static Optional<Kit> fromApi(String apiId) {
        if (apiId == null) return Optional.empty();
        for (Kit k : values()) if (k.apiId.equals(apiId)) return Optional.of(k);
        return Optional.empty();
    }
}
