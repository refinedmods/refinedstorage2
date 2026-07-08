package com.refinedmods.refinedstorage.common.support.tooltip;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.FormattedCharSequence;
import org.joml.Matrix4f;

public final class SmallText {
    public static final float DEFAULT_SCALE = 0.5F;
    public static final float TOOLTIP_SCALE = 0.7F;

    private SmallText() {
    }

    public static boolean isSmall() {
        return !Minecraft.getInstance().isEnforceUnicode();
    }

    public static float correctScale(final float requestedScale) {
        return isSmall() ? requestedScale : 1F;
    }

    public static void render(final Font font,
                              final FormattedCharSequence text,
                              final int x,
                              final int y,
                              final Matrix4f pose,
                              final MultiBufferSource.BufferSource buffer,
                              final float smallScale) {
        render(font, text, x, y, -1, pose, buffer, smallScale);
    }

    public static void render(final Font font,
                              final FormattedCharSequence text,
                              final int x,
                              final int y,
                              final int color,
                              final Matrix4f pose,
                              final MultiBufferSource.BufferSource buffer,
                              final float smallScale) {
        final float scale = correctScale(smallScale);
        final Matrix4f scaled = new Matrix4f(pose);
        scaled.scale(scale, scale, 1);
        font.drawInBatch(
            text,
            x / scale,
            (y / scale) + 1,
            color,
            true,
            scaled,
            buffer,
            Font.DisplayMode.NORMAL,
            0,
            15728880
        );
    }

    public static void render(final GuiGraphics graphics,
                              final Font font,
                              final FormattedCharSequence text,
                              final int x,
                              final int y,
                              final int color,
                              final boolean dropShadow,
                              final float smallScale) {
        final float scale = correctScale(smallScale);
        graphics.pose().pushPose();
        graphics.pose().scale(scale, scale, 1);
        graphics.drawString(font, text, (int) (x / scale), (int) (y / scale) + 1, color, dropShadow);
        graphics.pose().popPose();
    }

    public static void render(final GuiGraphics graphics,
                              final Font font,
                              final String text,
                              final int x,
                              final int y,
                              final int color,
                              final boolean dropShadow,
                              final float smallScale) {
        final float scale = correctScale(smallScale);
        graphics.pose().pushPose();
        graphics.pose().scale(scale, scale, 1);
        graphics.drawString(font, text, (int) (x / scale), (int) (y / scale) + 1, color, dropShadow);
        graphics.pose().popPose();
    }
}
