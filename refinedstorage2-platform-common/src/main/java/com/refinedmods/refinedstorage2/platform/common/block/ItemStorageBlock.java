package com.refinedmods.refinedstorage2.platform.common.block;

import com.refinedmods.refinedstorage2.platform.apiimpl.storage.type.ItemStorageType;
import com.refinedmods.refinedstorage2.platform.common.block.entity.storage.ItemStorageBlockBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class ItemStorageBlock extends StorageBlock {
    private final ItemStorageType.Variant variant;

    public ItemStorageBlock(ItemStorageType.Variant variant) {
        super(BlockConstants.STONE_PROPERTIES);
        this.variant = variant;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ItemStorageBlockBlockEntity(pos, state, variant);
    }
}
