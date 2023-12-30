package com.refinedmods.refinedstorage2.platform.common.storage.portablegrid;

import com.refinedmods.refinedstorage2.platform.common.support.AbstractBlockEntityTicker;

import java.util.function.Supplier;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

class PortableGridBlockEntityTicker extends AbstractBlockEntityTicker<AbstractPortableGridBlockEntity> {
    PortableGridBlockEntityTicker(
        final Supplier<BlockEntityType<AbstractPortableGridBlockEntity>> allowedTypeSupplier
    ) {
        super(allowedTypeSupplier);
    }

    @Override
    public void tick(final Level level,
                     final BlockPos pos,
                     final BlockState state,
                     final AbstractPortableGridBlockEntity blockEntity) {
        blockEntity.updateDiskStateIfNecessaryInLevel();
    }
}
