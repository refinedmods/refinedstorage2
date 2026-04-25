package com.refinedmods.refinedstorage.common.storage.portablegrid;

import com.refinedmods.refinedstorage.api.network.energy.EnergyStorage;
import com.refinedmods.refinedstorage.common.api.grid.Grid;
import com.refinedmods.refinedstorage.common.api.support.slotreference.PlayerSlotReference;
import com.refinedmods.refinedstorage.common.content.Menus;
import com.refinedmods.refinedstorage.common.grid.PortableGridData;
import com.refinedmods.refinedstorage.common.storage.DiskInventory;

import net.minecraft.world.entity.player.Inventory;

public class PortableGridItemContainerMenu extends AbstractPortableGridContainerMenu {
    public PortableGridItemContainerMenu(final int syncId,
                                         final Inventory playerInventory,
                                         final PortableGridData portableGridData) {
        super(Menus.INSTANCE.getPortableGridItem(), syncId, playerInventory, portableGridData);
        this.disabledSlot = portableGridData.slotReference().orElse(null);
        resized(0, 0, 0);
    }

    PortableGridItemContainerMenu(final int syncId,
                                  final Inventory playerInventory,
                                  final DiskInventory diskInventory,
                                  final Grid grid,
                                  final EnergyStorage energyStorage,
                                  final PlayerSlotReference playerSlotReference) {
        super(
            Menus.INSTANCE.getPortableGridItem(),
            syncId,
            playerInventory,
            diskInventory,
            grid,
            energyStorage
        );
        this.disabledSlot = playerSlotReference;
        resized(0, 0, 0);
    }
}
