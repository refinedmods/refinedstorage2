package com.refinedmods.refinedstorage.common.upgrade;


import com.refinedmods.refinedstorage.common.api.upgrade.UpgradeMapping;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class UpgradeItemClientTooltipComponent implements ClientTooltipComponent {
    private final ItemStack displayItemStack;
    private final Component displayName;

    public UpgradeItemClientTooltipComponent(final UpgradeMapping mapping) {
        this.displayItemStack = mapping.upgradeItem().getDefaultInstance();
        this.displayName = mapping.upgradeItem().getName(displayItemStack).copy()
            .append(" ")
            .append("(")
            .append(String.valueOf(mapping.maxAmount()))
            .append(")");
    }

    @Override
    public int getHeight(final Font font) {
        return 18;
    }

    @Override
    public int getWidth(final Font font) {
        return 16 + 4 + font.width(displayName);
    }

    @Override
    public void extractImage(final Font font, final int x, final int y, final int w, final int h,
                             final GuiGraphicsExtractor graphics) {
        graphics.item(displayItemStack, x, y);
        graphics.itemDecorations(font, displayItemStack, x, y);
        graphics.text(
            font,
            displayName,
            x + 16 + 4,
            y + 4,
            0xFFFFFFFF
        );
    }
}
