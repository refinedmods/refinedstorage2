package com.refinedmods.refinedstorage2.platform.common.containermenu.slot;

import com.refinedmods.refinedstorage2.platform.api.upgrade.ApplicableUpgrade;
import com.refinedmods.refinedstorage2.platform.common.block.entity.UpgradeContainer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslationAsHeading;

public class UpgradeSlot extends Slot implements SlotTooltip {
    private final UpgradeContainer upgradeContainer;

    public UpgradeSlot(final UpgradeContainer upgradeContainer, final int index, final int x, final int y) {
        super(upgradeContainer, index, x, y);
        this.upgradeContainer = upgradeContainer;
    }

    @Override
    public List<ClientTooltipComponent> getTooltip(final ItemStack carried) {
        if (!carried.isEmpty() || hasItem()) {
            return Collections.emptyList();
        }
        final List<ClientTooltipComponent> lines = new ArrayList<>();
        lines.add(ClientTooltipComponent.create(
            createTranslationAsHeading("gui", "upgrade_slot").getVisualOrderText()
        ));
        for (final ApplicableUpgrade applicableUpgrade : upgradeContainer.getApplicableUpgrades()) {
            lines.add(new ApplicableUpgradeTooltipComponent(applicableUpgrade));
        }
        return lines;
    }

    private static class ApplicableUpgradeTooltipComponent implements ClientTooltipComponent {
        private final ItemStack upgradeStack;
        private final Component name;

        private ApplicableUpgradeTooltipComponent(final ApplicableUpgrade applicableUpgrade) {
            this.upgradeStack = new ItemStack(
                applicableUpgrade.itemSupplier().get(),
                applicableUpgrade.maxAmount()
            );
            this.name = upgradeStack.getHoverName();
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
            graphics.renderItem(upgradeStack, x, y);
            graphics.renderItemDecorations(font, upgradeStack, x, y);
            graphics.drawString(
                font,
                name,
                x + 16 + 4,
                y + 4,
                Objects.requireNonNullElse(ChatFormatting.WHITE.getColor(), 15)
            );
        }
    }
}
