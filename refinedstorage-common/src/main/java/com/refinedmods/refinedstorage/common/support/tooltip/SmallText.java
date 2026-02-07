package com.refinedmods.refinedstorage.common.support.tooltip;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.FormattedCharSequence;

public final class SmallText {
    public static final float DEFAULT_SCALE = 0.5F;
    public static final float TOOLTIP_SCALE = 0.7F;

    private SmallText() {
    }

    public static boolean isSmall() {
        return !Minecraft.getInstance().isEnforceUnicode();
    }

    public static float correctScale(final float smallScale) {
        return isSmall() ? smallScale : 1F;
    }

    public static void render(final GuiGraphicsExtractor graphics,
                              final Font font,
                              final FormattedCharSequence text,
                              final int x,
                              final int y,
                              final int color,
                              final boolean dropShadow,
                              final float smallScale) {
        final float scale = correctScale(smallScale);
        graphics.pose().pushMatrix();
        graphics.pose().scale(scale, scale);
        graphics.text(font, text, (int) (x / scale), (int) (y / scale) + 1, color, dropShadow);
        graphics.pose().popMatrix();
    }

    public static void render(final GuiGraphicsExtractor graphics,
                              final Font font,
                              final String text,
                              final int x,
                              final int y,
                              final int color,
                              final boolean dropShadow,
                              final float smallScale) {
        final float scale = correctScale(smallScale);
        graphics.pose().pushMatrix();
        graphics.pose().scale(scale, scale);
        graphics.text(font, text, (int) (x / scale), (int) (y / scale) + 1, color, dropShadow);
        graphics.pose().popMatrix();
    }
}
