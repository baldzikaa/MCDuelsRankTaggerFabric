package com.mcduelstagger.rank;

import org.junit.jupiter.api.Test;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

class KitTest {
    @Test void allNineActiveKitsMapFromApi() {
        assertEquals(Kit.AXE,       Kit.fromApi("axe").orElseThrow());
        assertEquals(Kit.CRYSTAL,   Kit.fromApi("crystal").orElseThrow());
        assertEquals(Kit.MACE,      Kit.fromApi("mace").orElseThrow());
        assertEquals(Kit.NETHOP,    Kit.fromApi("nethop").orElseThrow());
        assertEquals(Kit.POT,       Kit.fromApi("pot").orElseThrow());
        assertEquals(Kit.SMP,       Kit.fromApi("smp").orElseThrow());
        assertEquals(Kit.SPEARMACE, Kit.fromApi("spear_mace").orElseThrow());
        assertEquals(Kit.SWORD,     Kit.fromApi("sword").orElseThrow());
        assertEquals(Kit.UHC,       Kit.fromApi("uhc").orElseThrow());
    }
    @Test void spearmaceWithoutUnderscoreIsRejected() {
        // The API id is "spear_mace" (with underscore). The bare "spearmace" must not match.
        assertEquals(Optional.empty(), Kit.fromApi("spearmace"));
    }
    @Test void glyphCodepointsAreUnique() {
        long unique = java.util.Arrays.stream(Kit.values())
            .mapToInt(Kit::glyphCodepoint).distinct().count();
        assertEquals(Kit.values().length, unique);
    }
    @Test void crystalAbbrevIsCpvp() {
        assertEquals("cpvp", Kit.CRYSTAL.abbrev());
    }
    @Test void unknownReturnsEmpty() {
        assertEquals(Optional.empty(), Kit.fromApi("unknown"));
        assertEquals(Optional.empty(), Kit.fromApi(null));
    }
}
