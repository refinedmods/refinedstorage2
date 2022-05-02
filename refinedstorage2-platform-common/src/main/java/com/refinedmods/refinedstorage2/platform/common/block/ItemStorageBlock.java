package com.refinedmods.refinedstorage2.platform.common.block;

import com.refinedmods.refinedstorage2.platform.common.block.entity.storage.ItemStorageBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.internal.storage.type.ItemStorageType;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

// TODO: Dismantling
public class ItemStorageBlock extends StorageBlock {
    private final ItemStorageType.Variant variant;

    public ItemStorageBlock(ItemStorageType.Variant variant) {
        super(BlockConstants.STONE_PROPERTIES);
        this.variant = variant;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ItemStorageBlockEntity(pos, state, variant);
    }
}
