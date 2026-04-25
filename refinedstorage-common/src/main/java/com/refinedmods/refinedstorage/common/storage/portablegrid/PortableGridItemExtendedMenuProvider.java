package com.refinedmods.refinedstorage.common.storage.portablegrid;

import com.refinedmods.refinedstorage.api.network.energy.EnergyStorage;
import com.refinedmods.refinedstorage.common.api.grid.Grid;
import com.refinedmods.refinedstorage.common.api.support.slotreference.PlayerSlotReference;
import com.refinedmods.refinedstorage.common.grid.GridData;
import com.refinedmods.refinedstorage.common.grid.PortableGridData;
import com.refinedmods.refinedstorage.common.storage.DiskInventory;
import com.refinedmods.refinedstorage.common.support.containermenu.ExtendedMenuProvider;

import java.util.Optional;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamEncoder;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jspecify.annotations.Nullable;

class PortableGridItemExtendedMenuProvider implements ExtendedMenuProvider<PortableGridData> {
    private final Component name;
    private final Grid grid;
    private final EnergyStorage energyStorage;
    private final DiskInventory diskInventory;
    private final PlayerSlotReference playerSlotReference;

    PortableGridItemExtendedMenuProvider(
        final Component name,
        final Grid grid,
        final EnergyStorage energyStorage,
        final DiskInventory diskInventory,
        final PlayerSlotReference playerSlotReference
    ) {
        this.name = name;
        this.grid = grid;
        this.energyStorage = energyStorage;
        this.diskInventory = diskInventory;
        this.playerSlotReference = playerSlotReference;
    }

    @Override
    public PortableGridData getMenuData() {
        return new PortableGridData(
            GridData.of(grid),
            energyStorage.getStored(),
            energyStorage.getCapacity(),
            Optional.of(playerSlotReference)
        );
    }

    @Override
    public StreamEncoder<RegistryFriendlyByteBuf, PortableGridData> getMenuCodec() {
        return PortableGridData.STREAM_CODEC;
    }

    @Override
    public Component getDisplayName() {
        return name;
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(final int syncId, final Inventory inventory, final Player player) {
        return new PortableGridItemContainerMenu(syncId, inventory, diskInventory, grid, energyStorage,
            playerSlotReference);
    }
}
