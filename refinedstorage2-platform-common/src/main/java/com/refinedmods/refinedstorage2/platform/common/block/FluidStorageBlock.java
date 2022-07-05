package com.refinedmods.refinedstorage2.platform.common.block;

import com.refinedmods.refinedstorage2.platform.apiimpl.storage.type.FluidStorageType;
import com.refinedmods.refinedstorage2.platform.common.block.entity.storage.FluidStorageBlockBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class FluidStorageBlock extends AbstractStorageBlock {
    private final FluidStorageType.Variant variant;

    public FluidStorageBlock(final FluidStorageType.Variant variant) {
        super(BlockConstants.STONE_PROPERTIES);
        this.variant = variant;
    }

    @Override
    public BlockEntity newBlockEntity(final BlockPos pos, final BlockState state) {
        return new FluidStorageBlockBlockEntity(pos, state, variant);
    }
}
