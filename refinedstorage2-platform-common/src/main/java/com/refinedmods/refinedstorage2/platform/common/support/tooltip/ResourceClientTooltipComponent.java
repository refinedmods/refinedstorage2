package com.refinedmods.refinedstorage2.platform.common.support.tooltip;

import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.support.resource.ResourceAmountTemplate;
import com.refinedmods.refinedstorage2.platform.api.support.resource.ResourceRendering;

import java.util.Objects;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;

public class ResourceClientTooltipComponent implements ClientTooltipComponent {
    private final ResourceAmountTemplate resourceAmount;
    private final Component name;

    public ResourceClientTooltipComponent(final ResourceAmountTemplate resourceAmount) {
        this.resourceAmount = resourceAmount;
        this.name = getNameWithAmount(resourceAmount);
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
        PlatformApi.INSTANCE.getResourceRendering(resourceAmount.getResource()).render(
            resourceAmount.getResource(),
            graphics,
            x,
            y
        );
        graphics.drawString(
            font,
            name,
            x + 16 + 4,
            y + 4,
            Objects.requireNonNullElse(ChatFormatting.GRAY.getColor(), 11184810)
        );
    }

    private static Component getNameWithAmount(final ResourceAmountTemplate resourceAmount) {
        final ResourceRendering rendering = PlatformApi.INSTANCE.getResourceRendering(
            resourceAmount.getResource()
        );
        final String amount = rendering.getDisplayedAmount(resourceAmount.getAmount(), true);
        final Component displayName = rendering.getDisplayName(resourceAmount.getResource());
        if (amount.isEmpty()) {
            return displayName;
        }
        return displayName.copy().append(" (").append(amount).append(")");
    }
}
