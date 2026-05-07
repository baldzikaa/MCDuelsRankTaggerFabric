package com.mcduelstagger.rank;

import org.junit.jupiter.api.Test;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

class RankTest {
    @Test void mapsAllTenApiStrings() {
        assertEquals(Rank.HT1, Rank.fromApi("high_dueler").orElseThrow());
        assertEquals(Rank.LT1, Rank.fromApi("dueler").orElseThrow());
        assertEquals(Rank.HT2, Rank.fromApi("high_diamond").orElseThrow());
        assertEquals(Rank.LT2, Rank.fromApi("diamond").orElseThrow());
        assertEquals(Rank.HT3, Rank.fromApi("high_gold").orElseThrow());
        assertEquals(Rank.LT3, Rank.fromApi("gold").orElseThrow());
        assertEquals(Rank.HT4, Rank.fromApi("high_silver").orElseThrow());
        assertEquals(Rank.LT4, Rank.fromApi("silver").orElseThrow());
        assertEquals(Rank.HT5, Rank.fromApi("high_iron").orElseThrow());
        assertEquals(Rank.LT5, Rank.fromApi("iron").orElseThrow());
    }
    @Test void unknownStringsReturnEmpty() {
        assertEquals(Optional.empty(), Rank.fromApi("foo"));
        assertEquals(Optional.empty(), Rank.fromApi(""));
        assertEquals(Optional.empty(), Rank.fromApi(null));
    }
    @Test void displayLabelsMatchMcduelsAbbreviations() {
        assertEquals("HD",  Rank.HT1.display());
        assertEquals("D",   Rank.LT1.display());
        assertEquals("HDi", Rank.HT2.display());
        assertEquals("Di",  Rank.LT2.display());
        assertEquals("HG",  Rank.HT3.display());
        assertEquals("G",   Rank.LT3.display());
        assertEquals("HS",  Rank.HT4.display());
        assertEquals("S",   Rank.LT4.display());
        assertEquals("HI",  Rank.HT5.display());
        assertEquals("I",   Rank.LT5.display());
    }
    @Test void rankOrderingHigherIsBetter() {
        assertTrue(Rank.HT1.tier() < Rank.LT1.tier());
        assertTrue(Rank.LT1.tier() < Rank.HT2.tier());
        assertTrue(Rank.HT5.tier() < Rank.LT5.tier());
    }
    @Test void colorsAreSpecHexValues() {
        assertEquals(0xFF3030, Rank.HT1.colorRgb());
        assertEquals(0xC02020, Rank.LT1.colorRgb());
        assertEquals(0x5EE9F4, Rank.HT2.colorRgb());
        assertEquals(0x3398A8, Rank.LT2.colorRgb());
        assertEquals(0xFFD700, Rank.HT3.colorRgb());
        assertEquals(0xB8860B, Rank.LT3.colorRgb());
        assertEquals(0xC0C0C0, Rank.HT4.colorRgb());
        assertEquals(0x808080, Rank.LT4.colorRgb());
        assertEquals(0xA89684, Rank.HT5.colorRgb());
        assertEquals(0x6B5944, Rank.LT5.colorRgb());
    }
}
