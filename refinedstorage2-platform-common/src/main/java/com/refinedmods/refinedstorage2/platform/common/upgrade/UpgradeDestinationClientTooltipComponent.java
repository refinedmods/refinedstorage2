package com.refinedmods.refinedstorage2.platform.common.upgrade;

import com.refinedmods.refinedstorage2.platform.api.upgrade.UpgradeMapping;

import java.util.Objects;
import java.util.Set;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.item.ItemStack;

public class UpgradeDestinationClientTooltipComponent implements ClientTooltipComponent {
    private final Set<UpgradeMapping> mappings;

    public UpgradeDestinationClientTooltipComponent(final Set<UpgradeMapping> mappings) {
        this.mappings = mappings;
    }

    @Override
    public int getHeight() {
        return 18 * mappings.size();
    }

    @Override
    public void renderImage(final Font font, final int x, final int y, final GuiGraphics graphics) {
        int yy = y;
        for (final UpgradeMapping mapping : mappings) {
            renderMapping(font, x, yy, graphics, mapping);
            yy += 18;
        }
    }

    private void renderMapping(final Font font,
                               final int x,
                               final int y,
                               final GuiGraphics graphics,
                               final UpgradeMapping mapping) {
        final ItemStack destinationStack = mapping.destination().getStackRepresentation();
        graphics.renderItem(destinationStack, x, y);
        graphics.renderItemDecorations(font, destinationStack, x, y);
        graphics.drawString(
            font,
            mapping.destinationDisplayName().copy().withStyle(ChatFormatting.GRAY),
            x + 16 + 4,
            y + 4,
            Objects.requireNonNullElse(ChatFormatting.GRAY.getColor(), 7)
        );
    }

    @Override
    public int getWidth(final Font font) {
        int width = 0;
        for (final UpgradeMapping destination : mappings) {
            final int destinationWidth = 16 + 4 + font.width(destination.destinationDisplayName());
            if (destinationWidth > width) {
                width = destinationWidth;
            }
        }
        return width;
    }
}
