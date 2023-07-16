package com.refinedmods.refinedstorage2.platform.common.screen.tooltip;

import com.refinedmods.refinedstorage2.platform.api.resource.filter.FilteredResource;

import java.util.Objects;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;

public class FilteredResourceClientTooltipComponent<T> implements ClientTooltipComponent {
    private final FilteredResource<T> filteredResource;
    private final Component name;

    public FilteredResourceClientTooltipComponent(final FilteredResource<T> filteredResource) {
        this.filteredResource = filteredResource;
        this.name = getNameWithAmount(filteredResource);
    }

    @Override
    public int getHeight() {
        return 18;
    }

    @Override
    public int getWidth(final Font font) {
        return 16 + 4 + font.width(name);
    }

    @Override
    public void renderImage(final Font font, final int x, final int y, final GuiGraphics graphics) {
        filteredResource.render(graphics, x, y);
        graphics.drawString(
            font,
            name,
            x + 16 + 4,
            y + 4,
            Objects.requireNonNullElse(ChatFormatting.GRAY.getColor(), 11184810)
        );
    }

    private static <T> Component getNameWithAmount(final FilteredResource<T> filteredResource) {
        final String amount = filteredResource.getDisplayedAmount();
        final Component displayName = filteredResource.getDisplayName();
        if (amount.isEmpty()) {
            return displayName;
        }
        return displayName.copy().append(" (").append(amount).append(")");
    }
}
