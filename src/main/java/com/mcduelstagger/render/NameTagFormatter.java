package com.mcduelstagger.render;

import com.mcduelstagger.rank.TierPicker;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Identifier;

public final class NameTagFormatter {
    private static final Identifier FONT = Identifier.of("mcduelstagger", "default");

    private NameTagFormatter() {}

    /**
     * Returns "<glyph> " — only the kit glyph (custom font + rank color), then a plain space.
     * Top-level Text is intentionally empty so the appended player name keeps its own style
     * (team / LuckPerms color) and the rank color does NOT bleed onto the name.
     */
    public static Text prefix(TierPicker.Result r) {
        String glyph = new String(Character.toChars(r.kit().glyphCodepoint()));
        // Glyph: custom font, NO color (renders in default text color).
        Style glyphStyle = Style.EMPTY.withFont(FONT);
        // Rank label ("HT3", "LT4", ...): rank-colored, default font.
        Style rankStyle = Style.EMPTY.withColor(TextColor.fromRgb(r.rank().colorRgb()));

        MutableText t = Text.empty();
        t.append(Text.literal(glyph).setStyle(glyphStyle));
        t.append(Text.literal(" "));
        t.append(Text.literal(r.rank().display()).setStyle(rankStyle));
        t.append(Text.literal(" | "));
        return t;
    }
}
