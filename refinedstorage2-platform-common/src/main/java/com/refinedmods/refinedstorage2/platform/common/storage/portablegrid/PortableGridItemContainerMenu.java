package com.refinedmods.refinedstorage2.platform.common.storage.portablegrid;

import com.refinedmods.refinedstorage2.api.network.energy.EnergyStorage;
import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.grid.Grid;
import com.refinedmods.refinedstorage2.platform.api.support.network.bounditem.SlotReference;
import com.refinedmods.refinedstorage2.platform.common.content.Menus;
import com.refinedmods.refinedstorage2.platform.common.storage.DiskInventory;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;

public class PortableGridItemContainerMenu extends AbstractPortableGridContainerMenu {
    public PortableGridItemContainerMenu(final int syncId,
                                         final Inventory playerInventory,
                                         final FriendlyByteBuf buf) {
        super(Menus.INSTANCE.getPortableGridItem(), syncId, playerInventory, buf);
        this.disabledSlot = PlatformApi.INSTANCE.getSlotReference(buf).orElse(null);
        addSlots(0);
    }

    PortableGridItemContainerMenu(final int syncId,
                                  final Inventory playerInventory,
                                  final DiskInventory diskInventory,
                                  final Grid grid,
                                  final EnergyStorage energyStorage,
                                  final SlotReference slotReference) {
        super(
            Menus.INSTANCE.getPortableGridItem(),
            syncId,
            playerInventory,
            diskInventory,
            grid,
            energyStorage
        );
        this.disabledSlot = slotReference;
        addSlots(0);
    }
}
