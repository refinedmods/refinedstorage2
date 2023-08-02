package com.refinedmods.refinedstorage2.platform.common.screen.tooltip;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.joml.Matrix4f;

public class SmallTextClientTooltipComponent implements ClientTooltipComponent {
    private final Component text;
    private final float scale;

    public SmallTextClientTooltipComponent(final Component text) {
        this.text = text;
        this.scale = getScale();
    }

    @Override
    public void renderText(final Font font,
                           final int x,
                           final int y,
                           final Matrix4f pose,
                           final MultiBufferSource.BufferSource buffer) {
        render(font, text.getVisualOrderText(), x, y, scale, pose, buffer);
    }

    @Override
    public int getHeight() {
        return 9;
    }

    @Override
    public int getWidth(final Font font) {
        return (int) (font.width(text) * scale);
    }

    public static float getScale() {
        return (Minecraft.getInstance().isEnforceUnicode()) ? 1F : 0.7F;
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
