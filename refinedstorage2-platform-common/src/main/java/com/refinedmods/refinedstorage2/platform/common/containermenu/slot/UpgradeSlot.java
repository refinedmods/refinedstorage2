package com.refinedmods.refinedstorage2.platform.common.containermenu.slot;

import com.refinedmods.refinedstorage2.platform.api.upgrade.UpgradeMapping;
import com.refinedmods.refinedstorage2.platform.common.block.entity.UpgradeContainer;
import com.refinedmods.refinedstorage2.platform.common.screen.tooltip.UpgradeItemClientTooltipComponent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
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
        for (final UpgradeMapping upgrade : upgradeContainer.getAllowedUpgrades()) {
            lines.add(new UpgradeItemClientTooltipComponent(upgrade));
        }
        return lines;
    }
}
