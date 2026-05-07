package com.mcduelstagger.rank;

import java.util.Optional;

public enum Rank {
    // MCDuels rank ladder, highest tier first.
    HD ("high_dueler",  1, 0xFF3030, "HD"),   // high dueler  — bright red
    D  ("dueler",       2, 0xC02020, "D"),    // dueler       — red
    HDI("high_diamond", 3, 0x5EE9F4, "HDi"),  // high diamond — diamond cyan
    DI ("diamond",      4, 0x3398A8, "Di"),   // diamond      — darker diamond
    HG ("high_gold",    5, 0xFFD700, "HG"),   // high gold    — gold
    G  ("gold",         6, 0xB8860B, "G"),    // gold         — darker gold
    HS ("high_silver",  7, 0xC0C0C0, "HS"),   // high silver  — silver
    S  ("silver",       8, 0x808080, "S"),    // silver       — darker silver
    HI ("high_iron",    9, 0xA89684, "HI"),   // high iron    — warm iron grey
    I  ("iron",        10, 0x6B5944, "I");    // iron         — darker iron

    private final String apiString;
    private final int tier;
    private final int colorRgb;
    private final String display;

    Rank(String apiString, int tier, int colorRgb, String display) {
        this.apiString = apiString;
        this.tier = tier;
        this.colorRgb = colorRgb;
        this.display = display;
    }
    public String apiString()  { return apiString; }
    public int    tier()       { return tier; }
    public int    colorRgb()   { return colorRgb; }
    public String display()    { return display; }

    public static Optional<Rank> fromApi(String apiString) {
        if (apiString == null) return Optional.empty();
        for (Rank r : values()) if (r.apiString.equals(apiString)) return Optional.of(r);
        return Optional.empty();
    }
}
