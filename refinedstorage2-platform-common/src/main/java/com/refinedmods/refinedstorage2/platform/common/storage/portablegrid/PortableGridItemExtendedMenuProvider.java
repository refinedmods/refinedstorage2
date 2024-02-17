package com.refinedmods.refinedstorage2.platform.common.storage.portablegrid;

import com.refinedmods.refinedstorage2.api.network.energy.EnergyStorage;
import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.grid.Grid;
import com.refinedmods.refinedstorage2.platform.api.support.network.bounditem.SlotReference;
import com.refinedmods.refinedstorage2.platform.common.content.ContentNames;
import com.refinedmods.refinedstorage2.platform.common.storage.DiskInventory;
import com.refinedmods.refinedstorage2.platform.common.support.containermenu.ExtendedMenuProvider;

import javax.annotation.Nullable;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

class PortableGridItemExtendedMenuProvider implements ExtendedMenuProvider {
    private final Grid grid;
    private final EnergyStorage energyStorage;
    private final DiskInventory diskInventory;
    private final SlotReference slotReference;

    PortableGridItemExtendedMenuProvider(final Grid grid,
                                         final EnergyStorage energyStorage,
                                         final DiskInventory diskInventory,
                                         final SlotReference slotReference) {
        this.grid = grid;
        this.energyStorage = energyStorage;
        this.diskInventory = diskInventory;
        this.slotReference = slotReference;
    }

    @Override
    public void writeScreenOpeningData(final ServerPlayer player, final FriendlyByteBuf buf) {
        PlatformApi.INSTANCE.writeGridScreenOpeningData(grid, buf);
        buf.writeLong(energyStorage.getStored());
        buf.writeLong(energyStorage.getCapacity());
        PlatformApi.INSTANCE.writeSlotReference(slotReference, buf);
    }

    @Override
    public Component getDisplayName() {
        return ContentNames.PORTABLE_GRID;
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(final int syncId, final Inventory inventory, final Player player) {
        return new PortableGridItemContainerMenu(syncId, inventory, diskInventory, grid, energyStorage, slotReference);
    }
}
