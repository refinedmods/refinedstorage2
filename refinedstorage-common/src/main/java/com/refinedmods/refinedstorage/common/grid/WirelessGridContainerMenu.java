package com.refinedmods.refinedstorage.common.grid;

import com.refinedmods.refinedstorage.common.api.grid.Grid;
import com.refinedmods.refinedstorage.common.api.support.slotreference.PlayerSlotReference;
import com.refinedmods.refinedstorage.common.content.Menus;

import net.minecraft.world.entity.player.Inventory;

public class WirelessGridContainerMenu extends AbstractGridContainerMenu {
    public WirelessGridContainerMenu(final int syncId,
                                     final Inventory playerInventory,
                                     final WirelessGridData wirelessGridData) {
        super(Menus.INSTANCE.getWirelessGrid(), syncId, playerInventory, wirelessGridData.gridData());
        this.disabledSlot = wirelessGridData.playerSlotReference();
        resized(0, 0, 0);
    }

    WirelessGridContainerMenu(final int syncId,
                              final Inventory playerInventory,
                              final Grid grid,
                              final PlayerSlotReference playerSlotReference) {
        super(Menus.INSTANCE.getWirelessGrid(), syncId, playerInventory, grid);
        this.disabledSlot = playerSlotReference;
        resized(0, 0, 0);
    }
}
