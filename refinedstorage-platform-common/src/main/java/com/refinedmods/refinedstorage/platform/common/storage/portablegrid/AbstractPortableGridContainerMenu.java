package com.refinedmods.refinedstorage.platform.common.storage.portablegrid;

import com.refinedmods.refinedstorage.api.network.energy.EnergyStorage;
import com.refinedmods.refinedstorage.platform.api.grid.Grid;
import com.refinedmods.refinedstorage.platform.api.storage.StorageContainerItem;
import com.refinedmods.refinedstorage.platform.common.grid.AbstractGridContainerMenu;
import com.refinedmods.refinedstorage.platform.common.grid.PortableGridData;
import com.refinedmods.refinedstorage.platform.common.storage.DiskInventory;
import com.refinedmods.refinedstorage.platform.common.support.FilteredContainer;
import com.refinedmods.refinedstorage.platform.common.support.containermenu.ValidatedSlot;
import com.refinedmods.refinedstorage.platform.common.support.energy.EnergyContainerMenu;
import com.refinedmods.refinedstorage.platform.common.support.energy.EnergyInfo;

import javax.annotation.Nullable;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;

public abstract class AbstractPortableGridContainerMenu extends AbstractGridContainerMenu
    implements EnergyContainerMenu {
    private final FilteredContainer diskInventory;
    private final EnergyInfo energyInfo;

    @Nullable
    private Slot diskSlot;

    AbstractPortableGridContainerMenu(
        final MenuType<? extends AbstractGridContainerMenu> menuType,
        final int syncId,
        final Inventory playerInventory,
        final PortableGridData portableGridData
    ) {
        super(menuType, syncId, playerInventory, portableGridData.gridData());
        this.diskInventory = new FilteredContainer(1, StorageContainerItem.stackValidator());
        this.energyInfo = EnergyInfo.forClient(
            playerInventory.player,
            portableGridData.stored(),
            portableGridData.capacity()
        );
    }

    AbstractPortableGridContainerMenu(
        final MenuType<? extends AbstractGridContainerMenu> menuType,
        final int syncId,
        final Inventory playerInventory,
        final DiskInventory diskInventory,
        final Grid grid,
        final EnergyStorage energyStorage
    ) {
        super(menuType, syncId, playerInventory, grid);
        this.diskInventory = diskInventory;
        this.energyInfo = EnergyInfo.forServer(
            playerInventory.player,
            energyStorage::getStored,
            energyStorage::getCapacity
        );
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        energyInfo.detectChanges();
    }

    @Override
    protected boolean canTransferSlot(final Slot slot) {
        return slot != diskSlot;
    }

    @Override
    public void onScreenReady(final int playerInventoryY) {
        super.onScreenReady(playerInventoryY);
        diskSlot = ValidatedSlot.forStorageContainer(diskInventory, 0, -19, 8);
        addSlot(diskSlot);
        transferManager.addBiTransfer(playerInventory, diskInventory);
    }

    @Override
    public EnergyInfo getEnergyInfo() {
        return energyInfo;
    }
}
