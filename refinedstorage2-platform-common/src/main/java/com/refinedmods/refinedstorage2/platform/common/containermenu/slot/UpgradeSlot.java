package com.refinedmods.refinedstorage2.platform.common.containermenu.slot;

import com.refinedmods.refinedstorage2.platform.api.upgrade.ApplicableUpgrade;
import com.refinedmods.refinedstorage2.platform.common.block.entity.UpgradeContainer;

import java.util.Set;

import net.minecraft.world.inventory.Slot;

public class UpgradeSlot extends Slot {
    private final UpgradeContainer upgradeContainer;

    public UpgradeSlot(final UpgradeContainer upgradeContainer, final int index, final int x, final int y) {
        super(upgradeContainer, index, x, y);
        this.upgradeContainer = upgradeContainer;
    }

    public Set<ApplicableUpgrade> getApplicableUpgrades() {
        return upgradeContainer.getApplicableUpgrades();
    }
}
