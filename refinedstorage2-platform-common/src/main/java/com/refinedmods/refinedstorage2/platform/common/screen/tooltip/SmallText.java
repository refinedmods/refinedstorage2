package com.refinedmods.refinedstorage2.platform.common.screen.tooltip;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.FormattedCharSequence;
import org.joml.Matrix4f;

public final class SmallText {
    private SmallText() {
    }

    public static float getScale() {
        return Minecraft.getInstance().isEnforceUnicode() ? 1F : 0.7F;
    }

    public static void render(final Font font,
                              final FormattedCharSequence text,
                              final int x,
                              final int y,
                              final float scale,
                              final Matrix4f pose,
                              final MultiBufferSource.BufferSource buffer) {
        final Matrix4f scaled = new Matrix4f(pose);
        scaled.scale(scale, scale, 1);
        font.drawInBatch(
            text,
            x / scale,
            (y / scale) + 1,
            -1,
            true,
            scaled,
            buffer,
            Font.DisplayMode.NORMAL,
            0,
            15728880
        );
    }
}
