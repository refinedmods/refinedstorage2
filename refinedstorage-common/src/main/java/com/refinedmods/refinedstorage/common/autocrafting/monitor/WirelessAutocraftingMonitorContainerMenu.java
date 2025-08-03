package com.refinedmods.refinedstorage.common.autocrafting.monitor;

import com.refinedmods.refinedstorage.common.content.Menus;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

public class WirelessAutocraftingMonitorContainerMenu extends AbstractAutocraftingMonitorContainerMenu {
    public WirelessAutocraftingMonitorContainerMenu(final int syncId,
                                                    final Inventory playerInventory,
                                                    final AutocraftingMonitorData data) {
        super(Menus.INSTANCE.getWirelessAutocraftingMonitor(), syncId, playerInventory, data);
    }

    WirelessAutocraftingMonitorContainerMenu(final int syncId,
                                             final Inventory playerInventory,
                                             final AutocraftingMonitor autocraftingMonitor) {
        super(Menus.INSTANCE.getWirelessAutocraftingMonitor(), syncId, playerInventory.player, autocraftingMonitor);
    }

    @Override
    public boolean stillValid(final Player player) {
        return true;
    }
}
