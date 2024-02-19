package com.refinedmods.refinedstorage2.platform.common.support.energy;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.network.energy.EnergyStorage;
import com.refinedmods.refinedstorage2.api.network.impl.energy.AbstractProxyEnergyStorage;

import net.minecraft.world.level.block.entity.BlockEntity;

public class BlockEntityEnergyStorage extends AbstractProxyEnergyStorage {
    private final BlockEntity blockEntity;

    public BlockEntityEnergyStorage(final EnergyStorage delegate, final BlockEntity blockEntity) {
        super(delegate);
        this.blockEntity = blockEntity;
    }

    @Override
    public long receive(final long amount, final Action action) {
        final long received = super.receive(amount, action);
        if (received > 0 && action == Action.EXECUTE) {
            blockEntity.setChanged();
        }
        return received;
    }

    @Override
    public long extract(final long amount, final Action action) {
        final long extracted = super.extract(amount, action);
        if (extracted > 0 && action == Action.EXECUTE) {
            blockEntity.setChanged();
        }
        return extracted;
    }
}
