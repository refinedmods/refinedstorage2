package com.refinedmods.refinedstorage2.platform.common.screen.tooltip;

import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.resource.ResourceInstance;
import com.refinedmods.refinedstorage2.platform.api.resource.ResourceRendering;

import java.util.Objects;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;

public class ResourceClientTooltipComponent<T> implements ClientTooltipComponent {
    private final ResourceInstance<T> resourceInstance;
    private final Component name;

    public ResourceClientTooltipComponent(final ResourceInstance<T> resourceInstance) {
        this.resourceInstance = resourceInstance;
        this.name = getNameWithAmount(resourceInstance);
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
        PlatformApi.INSTANCE.getResourceRendering(resourceInstance.getResource()).render(
            resourceInstance.getResource(),
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

    private static <T> Component getNameWithAmount(final ResourceInstance<T> resourceInstance) {
        final ResourceRendering<T> rendering = PlatformApi.INSTANCE.getResourceRendering(
            resourceInstance.getResource()
        );
        final String amount = rendering.getDisplayedAmount(resourceInstance.getAmount());
        final Component displayName = rendering.getDisplayName(resourceInstance.getResource());
        if (amount.isEmpty()) {
            return displayName;
        }
        return displayName.copy().append(" (").append(amount).append(")");
    }
}
