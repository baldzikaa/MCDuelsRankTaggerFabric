package com.mcduelstagger.rank;

import com.mcduelstagger.api.KitEntry;
import com.mcduelstagger.api.Profile;
import org.junit.jupiter.api.Test;

import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class TierPickerTest {
    private static final UUID U = UUID.fromString("b876ec32-e396-476b-a115-8438d83c67d4");

    private static Profile profile(Map<String, KitEntry> kits) {
        return new Profile(U, "x", kits);
    }
    private static Map<String, KitEntry> kits(Object... pairs) {
        Map<String, KitEntry> m = new LinkedHashMap<>();
        for (int i = 0; i < pairs.length; i += 3) {
            m.put((String) pairs[i],
                  new KitEntry((String) pairs[i+1], (int) pairs[i+2], 1));
        }
        return m;
    }

    @Test void singleKitReturnsThatKit() {
        var r = TierPicker.pick(profile(kits("crystal", "high_dueler", 2400)),
                                EnumSet.allOf(Kit.class)).orElseThrow();
        assertEquals(Rank.HD, r.rank());
        assertEquals(Kit.CRYSTAL, r.kit());
    }
    @Test void emptyKitsReturnsEmpty() {
        assertTrue(TierPicker.pick(profile(Map.of()), EnumSet.allOf(Kit.class)).isEmpty());
    }
    @Test void picksHigherTierAcrossKits() {
        var r = TierPicker.pick(profile(kits(
            "axe", "gold", 1500,
            "uhc", "high_diamond", 2200
        )), EnumSet.allOf(Kit.class)).orElseThrow();
        assertEquals(Rank.HDI, r.rank());
        assertEquals(Kit.UHC, r.kit());
    }
    @Test void tieBreaksOnEloThenAlpha() {
        var r = TierPicker.pick(profile(kits(
            "uhc",     "high_dueler", 2400,
            "crystal", "high_dueler", 2500
        )), EnumSet.allOf(Kit.class)).orElseThrow();
        assertEquals(Kit.CRYSTAL, r.kit());

        var r2 = TierPicker.pick(profile(kits(
            "uhc",     "high_dueler", 2500,
            "crystal", "high_dueler", 2500
        )), EnumSet.allOf(Kit.class)).orElseThrow();
        assertEquals(Kit.CRYSTAL, r2.kit());
    }
    @Test void disabledKitsAreFilteredOut() {
        var r = TierPicker.pick(profile(kits(
            "crystal", "high_dueler", 2500,
            "uhc",     "diamond",     2000
        )), EnumSet.of(Kit.UHC)).orElseThrow();
        assertEquals(Rank.DI, r.rank());
        assertEquals(Kit.UHC, r.kit());
    }
    @Test void allKitsDisabledReturnsEmpty() {
        var p = profile(kits("crystal", "high_dueler", 2500));
        assertTrue(TierPicker.pick(p, EnumSet.noneOf(Kit.class)).isEmpty());
    }
    @Test void unknownKitOrRankInJsonIgnored() {
        var r = TierPicker.pick(profile(kits(
            "spearmace", "high_dueler", 9999,
            "axe",       "gold",        1200
        )), EnumSet.allOf(Kit.class)).orElseThrow();
        assertEquals(Rank.G, r.rank());
        assertEquals(Kit.AXE, r.kit());
    }
}
