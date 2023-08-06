package com.refinedmods.refinedstorage2.platform.common.screen.tooltip;


import com.refinedmods.refinedstorage2.platform.api.upgrade.UpgradeMapping;

import java.util.Objects;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;

public class UpgradeItemClientTooltipComponent implements ClientTooltipComponent {
    private final UpgradeMapping mapping;

    public UpgradeItemClientTooltipComponent(final UpgradeMapping mapping) {
        this.mapping = mapping;
    }

    @Override
    public int getHeight() {
        return 18;
    }

    @Override
    public int getWidth(final Font font) {
        return 16 + 4 + font.width(mapping.upgradeDisplayName());
    }

    @Override
    public void renderImage(final Font font, final int x, final int y, final GuiGraphics graphics) {
        graphics.renderItem(mapping.displayItemStack(), x, y);
        graphics.renderItemDecorations(font, mapping.displayItemStack(), x, y);
        graphics.drawString(
            font,
            mapping.upgradeDisplayName(),
            x + 16 + 4,
            y + 4,
            Objects.requireNonNullElse(ChatFormatting.WHITE.getColor(), 15)
        );
    }
}
