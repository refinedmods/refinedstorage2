package com.refinedmods.refinedstorage2.platform.common.storage.portablegrid;

import com.refinedmods.refinedstorage2.api.network.energy.EnergyStorage;
import com.refinedmods.refinedstorage2.platform.api.grid.Grid;
import com.refinedmods.refinedstorage2.platform.api.storage.StorageContainerItem;
import com.refinedmods.refinedstorage2.platform.common.grid.AbstractGridContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.storage.DiskInventory;
import com.refinedmods.refinedstorage2.platform.common.support.containermenu.ValidatedSlot;
import com.refinedmods.refinedstorage2.platform.common.support.energy.EnergyContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.support.energy.EnergyInfo;

import javax.annotation.Nullable;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;

public abstract class AbstractPortableGridContainerMenu extends AbstractGridContainerMenu
    implements EnergyContainerMenu {
    private final SimpleContainer diskInventory;
    private final EnergyInfo energyInfo;

    @Nullable
    private Slot diskSlot;

    AbstractPortableGridContainerMenu(
        final MenuType<? extends AbstractGridContainerMenu> menuType,
        final int syncId,
        final Inventory playerInventory,
        final FriendlyByteBuf buf
    ) {
        super(menuType, syncId, playerInventory, buf);
        this.diskInventory = new SimpleContainer(1);
        this.energyInfo = EnergyInfo.forClient(playerInventory.player, buf.readLong(), buf.readLong());
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
    public void addSlots(final int playerInventoryY) {
        super.addSlots(playerInventoryY);
        diskSlot = new ValidatedSlot(
            diskInventory,
            0,
            -19,
            8,
            stack -> stack.getItem() instanceof StorageContainerItem
        );
        addSlot(diskSlot);
        transferManager.addBiTransfer(playerInventory, diskInventory);
    }

    @Override
    public EnergyInfo getEnergyInfo() {
        return energyInfo;
    }
}
