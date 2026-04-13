package com.refinedmods.refinedstorage.common.storage.portablegrid;

import com.refinedmods.refinedstorage.api.network.energy.EnergyStorage;
import com.refinedmods.refinedstorage.common.api.grid.Grid;
import com.refinedmods.refinedstorage.common.api.storage.StorageContainerItem;
import com.refinedmods.refinedstorage.common.grid.AbstractGridContainerMenu;
import com.refinedmods.refinedstorage.common.grid.PortableGridData;
import com.refinedmods.refinedstorage.common.storage.DiskInventory;
import com.refinedmods.refinedstorage.common.support.FilteredContainer;
import com.refinedmods.refinedstorage.common.support.containermenu.ValidatedSlot;
import com.refinedmods.refinedstorage.common.support.energy.EnergyContainerMenu;
import com.refinedmods.refinedstorage.common.support.energy.EnergyInfo;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import org.jspecify.annotations.Nullable;

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
        this.diskInventory = new FilteredContainer(1, StorageContainerItem.VALIDATOR);
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
    public void resized(final int playerInventoryY, final int topYStart, final int topYEnd) {
        super.resized(playerInventoryY, topYStart, topYEnd);
        diskSlot = ValidatedSlot.forStorageContainer(diskInventory, 0, -19, 8);
        addSlot(diskSlot);
        transferManager.addBiTransfer(playerInventory, diskInventory);
    }

    @Override
    public EnergyInfo getEnergyInfo() {
        return energyInfo;
    }
}
