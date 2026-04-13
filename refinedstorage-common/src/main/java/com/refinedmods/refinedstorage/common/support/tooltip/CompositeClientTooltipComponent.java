package com.refinedmods.refinedstorage.common.support.tooltip;

import java.util.List;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;

public class CompositeClientTooltipComponent implements ClientTooltipComponent {
    private final List<ClientTooltipComponent> children;

    public CompositeClientTooltipComponent(final List<ClientTooltipComponent> children) {
        this.children = children;
    }

    @Override
    public int getHeight(final Font font) {
        return children.stream().mapToInt(c -> c.getHeight(font)).sum();
    }

    @Override
    public int getWidth(final Font font) {
        return children.stream().mapToInt(c -> c.getWidth(font)).max().orElse(0);
    }

    @Override
    public void extractImage(final Font font, final int x, final int y, final int w, final int h,
                             final GuiGraphicsExtractor graphics) {
        int yy = y;
        for (final ClientTooltipComponent child : children) {
            child.extractImage(font, x, yy, w, h, graphics);
            yy += child.getHeight(font);
        }
    }

    @Override
    public void extractText(final GuiGraphicsExtractor graphics, final Font font, final int x, final int y) {
        int yy = y;
        for (final ClientTooltipComponent child : children) {
            child.extractText(graphics, font, x, yy);
            yy += child.getHeight(font);
        }
    }
}
