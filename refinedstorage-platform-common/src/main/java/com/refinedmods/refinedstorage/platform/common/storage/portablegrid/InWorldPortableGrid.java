package com.refinedmods.refinedstorage.platform.common.storage.portablegrid;

import com.refinedmods.refinedstorage.api.network.energy.EnergyStorage;
import com.refinedmods.refinedstorage.api.storage.StateTrackedStorage;
import com.refinedmods.refinedstorage.platform.common.storage.DiskInventory;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

class InWorldPortableGrid extends PortableGrid {
    private final AbstractPortableGridBlockEntity blockEntity;

    InWorldPortableGrid(final EnergyStorage energyStorage,
                        final DiskInventory diskInventory,
                        final StateTrackedStorage.Listener diskListener,
                        final AbstractPortableGridBlockEntity blockEntity) {
        super(energyStorage, diskInventory, diskListener);
        this.blockEntity = blockEntity;
    }

    @Override
    public boolean isGridActive() {
        final Level level = blockEntity.getLevel();
        final BlockPos worldPosition = blockEntity.getBlockPos();
        return super.isGridActive()
            && level != null
            && blockEntity.getRedstoneMode().isActive(level.hasNeighborSignal(worldPosition));
    }
}
