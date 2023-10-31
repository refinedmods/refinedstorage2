package com.refinedmods.refinedstorage2.platform.common.upgrade;

import com.refinedmods.refinedstorage2.platform.api.upgrade.UpgradeMapping;

import java.util.Set;

import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class UpgradeSlot extends Slot {
    private final UpgradeContainer upgradeContainer;

    public UpgradeSlot(final UpgradeContainer upgradeContainer, final int index, final int x, final int y) {
        super(upgradeContainer, index, x, y);
        this.upgradeContainer = upgradeContainer;
    }

    @Override
    public boolean mayPlace(final ItemStack stack) {
        return upgradeContainer.canPlaceItem(getContainerSlot(), stack);
    }

    public Set<UpgradeMapping> getAllowedUpgrades() {
        return upgradeContainer.getAllowedUpgrades();
    }
}
