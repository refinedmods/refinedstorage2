package com.refinedmods.refinedstorage2.platform.common.screen.tooltip;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import org.joml.Matrix4f;

public class SmallTextClientTooltipComponent implements ClientTooltipComponent {
    private final Component text;
    private final float scale;

    public SmallTextClientTooltipComponent(final Component text) {
        this.text = text;
        this.scale = SmallText.getScale();
    }

    @Override
    public void renderText(final Font font,
                           final int x,
                           final int y,
                           final Matrix4f pose,
                           final MultiBufferSource.BufferSource buffer) {
        SmallText.render(font, text.getVisualOrderText(), x, y, scale, pose, buffer);
    }

    @Override
    public int getHeight() {
        return 9;
    }

    @Override
    public int getWidth(final Font font) {
        return (int) (font.width(text) * scale);
    }
}
