package com.refinedmods.refinedstorage2.platform.common.screen.tooltip;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import org.joml.Matrix4f;

public class SmallTextClientTooltipComponent implements ClientTooltipComponent {
    private final Component component;
    private final float scale;

    public SmallTextClientTooltipComponent(final Component component, final float scale) {
        this.component = component;
        this.scale = scale;
    }

    @Override
    public void renderText(final Font font,
                           final int x,
                           final int y,
                           final Matrix4f pose,
                           final MultiBufferSource.BufferSource buffer) {
        final Matrix4f scaled = new Matrix4f(pose);
        scaled.scale(scale, scale, 1);
        font.drawInBatch(
            component,
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

    @Override
    public int getHeight() {
        return 9;
    }

    @Override
    public int getWidth(final Font font) {
        return (int) (font.width(component) * scale);
    }
}
