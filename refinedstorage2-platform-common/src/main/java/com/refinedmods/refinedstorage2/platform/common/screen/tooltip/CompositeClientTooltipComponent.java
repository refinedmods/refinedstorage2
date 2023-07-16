package com.refinedmods.refinedstorage2.platform.common.screen.tooltip;

import java.util.List;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.MultiBufferSource;
import org.joml.Matrix4f;

public class CompositeClientTooltipComponent implements ClientTooltipComponent {
    private final List<ClientTooltipComponent> children;

    public CompositeClientTooltipComponent(final List<ClientTooltipComponent> children) {
        this.children = children;
    }

    @Override
    public int getHeight() {
        return children.stream().mapToInt(ClientTooltipComponent::getHeight).sum();
    }

    @Override
    public int getWidth(final Font font) {
        return children.stream().mapToInt(c -> c.getWidth(font)).max().orElse(0);
    }

    @Override
    public void renderImage(final Font font, final int x, final int y, final GuiGraphics graphics) {
        int yy = y;
        for (final ClientTooltipComponent child : children) {
            child.renderImage(font, x, yy, graphics);
            yy += child.getHeight();
        }
    }

    @Override
    public void renderText(final Font font,
                           final int x,
                           final int y,
                           final Matrix4f matrix,
                           final MultiBufferSource.BufferSource buffer) {
        int yy = y;
        for (final ClientTooltipComponent child : children) {
            child.renderText(font, x, yy, matrix, buffer);
            yy += child.getHeight();
        }
    }
}
