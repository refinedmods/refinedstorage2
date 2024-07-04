package com.refinedmods.refinedstorage.platform.common.grid;

import com.refinedmods.refinedstorage.platform.api.grid.Grid;
import com.refinedmods.refinedstorage.platform.api.support.network.bounditem.SlotReference;
import com.refinedmods.refinedstorage.platform.common.content.Menus;

import net.minecraft.world.entity.player.Inventory;

public class WirelessGridContainerMenu extends AbstractGridContainerMenu {
    public WirelessGridContainerMenu(final int syncId,
                                     final Inventory playerInventory,
                                     final WirelessGridData wirelessGridData) {
        super(Menus.INSTANCE.getWirelessGrid(), syncId, playerInventory, wirelessGridData.gridData());
        this.disabledSlot = wirelessGridData.slotReference();
        onScreenReady(0);
    }

    WirelessGridContainerMenu(final int syncId,
                              final Inventory playerInventory,
                              final Grid grid,
                              final SlotReference slotReference) {
        super(Menus.INSTANCE.getWirelessGrid(), syncId, playerInventory, grid);
        this.disabledSlot = slotReference;
        onScreenReady(0);
    }
}
