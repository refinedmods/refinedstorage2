package com.refinedmods.refinedstorage2.platform.common.block.entity;

import com.refinedmods.refinedstorage2.api.network.impl.energy.EnergyStorageImpl;

import net.minecraft.world.level.block.entity.BlockEntity;

public class BlockEntityEnergyStorage extends EnergyStorageImpl {
    private final BlockEntity blockEntity;

    public BlockEntityEnergyStorage(final long capacity, final BlockEntity blockEntity) {
        super(capacity);
        this.blockEntity = blockEntity;
    }

    @Override
    protected void changed() {
        super.changed();
        blockEntity.setChanged();
    }
}
