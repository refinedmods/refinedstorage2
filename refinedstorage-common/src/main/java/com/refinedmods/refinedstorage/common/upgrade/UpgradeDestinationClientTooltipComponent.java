package com.refinedmods.refinedstorage.common.upgrade;

import com.refinedmods.refinedstorage.common.api.upgrade.UpgradeMapping;

import java.util.Set;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;

public class UpgradeDestinationClientTooltipComponent implements ClientTooltipComponent {
    private final Set<UpgradeMapping> mappings;

    public UpgradeDestinationClientTooltipComponent(final Set<UpgradeMapping> mappings) {
        this.mappings = mappings;
    }

    @Override
    public int getHeight(final Font font) {
        return 18 * mappings.size();
    }

    @Override
    public void extractImage(final Font font, final int x, final int y, final int w, final int h,
                             final GuiGraphicsExtractor graphics) {
        int yy = y;
        for (final UpgradeMapping mapping : mappings) {
            renderMapping(font, x, yy, graphics, mapping);
            yy += 18;
        }
    }

    private void renderMapping(final Font font,
                               final int x,
                               final int y,
                               final GuiGraphicsExtractor graphics,
                               final UpgradeMapping mapping) {
        final ItemStack destinationStack = mapping.destination().getStackRepresentation();
        graphics.item(destinationStack, x, y);
        graphics.itemDecorations(font, destinationStack, x, y);
        final Component destinationDisplayName = getDestinationDisplayName(mapping);
        graphics.text(
            font,
            destinationDisplayName,
            x + 16 + 4,
            y + 4,
            0xFFAAAAAA
        );
    }

    private static MutableComponent getDestinationDisplayName(final UpgradeMapping mapping) {
        return mapping.destination().getName().copy()
            .append(" ")
            .append("(")
            .append(String.valueOf(mapping.maxAmount()))
            .append(")");
    }

    @Override
    public int getWidth(final Font font) {
        int width = 0;
        for (final UpgradeMapping destination : mappings) {
            final int destinationWidth = 16 + 4 + font.width(getDestinationDisplayName(destination));
            if (destinationWidth > width) {
                width = destinationWidth;
            }
        }
        return width;
    }
}
