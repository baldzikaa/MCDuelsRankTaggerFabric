package com.mcduelstagger.rank;

import org.junit.jupiter.api.Test;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

class RankTest {
    @Test void mapsAllTenApiStrings() {
        assertEquals(Rank.HD, Rank.fromApi("high_dueler").orElseThrow());
        assertEquals(Rank.D, Rank.fromApi("dueler").orElseThrow());
        assertEquals(Rank.HDI, Rank.fromApi("high_diamond").orElseThrow());
        assertEquals(Rank.DI, Rank.fromApi("diamond").orElseThrow());
        assertEquals(Rank.HG, Rank.fromApi("high_gold").orElseThrow());
        assertEquals(Rank.G, Rank.fromApi("gold").orElseThrow());
        assertEquals(Rank.HS, Rank.fromApi("high_silver").orElseThrow());
        assertEquals(Rank.S, Rank.fromApi("silver").orElseThrow());
        assertEquals(Rank.HI, Rank.fromApi("high_iron").orElseThrow());
        assertEquals(Rank.I, Rank.fromApi("iron").orElseThrow());
    }
    @Test void unknownStringsReturnEmpty() {
        assertEquals(Optional.empty(), Rank.fromApi("foo"));
        assertEquals(Optional.empty(), Rank.fromApi(""));
        assertEquals(Optional.empty(), Rank.fromApi(null));
    }
    @Test void displayLabelsMatchMcduelsAbbreviations() {
        assertEquals("HD",  Rank.HD.display());
        assertEquals("D",   Rank.D.display());
        assertEquals("HDi", Rank.HDI.display());
        assertEquals("Di",  Rank.DI.display());
        assertEquals("HG",  Rank.HG.display());
        assertEquals("G",   Rank.G.display());
        assertEquals("HS",  Rank.HS.display());
        assertEquals("S",   Rank.S.display());
        assertEquals("HI",  Rank.HI.display());
        assertEquals("I",   Rank.I.display());
    }
    @Test void rankOrderingHigherIsBetter() {
        assertTrue(Rank.HD.tier() < Rank.D.tier());
        assertTrue(Rank.D.tier() < Rank.HDI.tier());
        assertTrue(Rank.HI.tier() < Rank.I.tier());
    }
    @Test void colorsAreSpecHexValues() {
        assertEquals(0xFF3030, Rank.HD.colorRgb());
        assertEquals(0xC02020, Rank.D.colorRgb());
        assertEquals(0x5EE9F4, Rank.HDI.colorRgb());
        assertEquals(0x3398A8, Rank.DI.colorRgb());
        assertEquals(0xFFD700, Rank.HG.colorRgb());
        assertEquals(0xB8860B, Rank.G.colorRgb());
        assertEquals(0xC0C0C0, Rank.HS.colorRgb());
        assertEquals(0x808080, Rank.S.colorRgb());
        assertEquals(0xA89684, Rank.HI.colorRgb());
        assertEquals(0x6B5944, Rank.I.colorRgb());
    }
}
