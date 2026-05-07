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
     * Returns the prefix Text {@code "<glyph> <rank> | "} for use in front of a player nametag:
     * <ul>
     *   <li>The kit glyph uses the custom mcduelstagger font and inherits the renderer's
     *       default color (so it doesn't fight with team / LuckPerms colors).</li>
     *   <li>The rank label (e.g. {@code HD}, {@code Di}, {@code HG}) is the only piece tinted
     *       with the rank's RGB color.</li>
     * </ul>
     * The top-level Text is intentionally empty-styled so the player's display name, when
     * appended, keeps its own style with no color bleed from this prefix.
     */
    public static Text prefix(TierPicker.Result r) {
        String glyph = new String(Character.toChars(r.kit().glyphCodepoint()));
        // Glyph: custom font, NO color (inherits whatever default the renderer applies).
        Style glyphStyle = Style.EMPTY.withFont(FONT);
        // Rank label: rank-colored, default font.
        Style rankStyle = Style.EMPTY.withColor(TextColor.fromRgb(r.rank().colorRgb()));

        MutableText t = Text.empty();
        t.append(Text.literal(glyph).setStyle(glyphStyle));
        t.append(Text.literal(" "));
        t.append(Text.literal(r.rank().display()).setStyle(rankStyle));
        t.append(Text.literal(" | "));
        return t;
    }
}
