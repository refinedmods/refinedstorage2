package com.refinedmods.refinedstorage2.platform.common.screen;

import com.refinedmods.refinedstorage2.platform.common.containermenu.RegulatorUpgradeContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.screen.amount.AbstractSingleAmountScreen;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class RegulatorUpgradeScreen extends AbstractSingleAmountScreen<RegulatorUpgradeContainerMenu> {
    public RegulatorUpgradeScreen(final RegulatorUpgradeContainerMenu menu,
                                  final Inventory playerInventory,
                                  final Component text) {
        super(menu, playerInventory, text, menu.getAmount(), 1);
    }
}
