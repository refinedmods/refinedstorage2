package com.refinedmods.refinedstorage2.core.adapter;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class FakeWorldAdapter implements WorldAdapter {
    private final Map<BlockPos, BlockEntity> blockEntities = new HashMap<>();

    @Override
    public Optional<BlockEntity> getBlockEntity(BlockPos pos) {
        return Optional.ofNullable(blockEntities.get(pos));
    }

    public <T extends BlockEntity> T setBlockEntity(BlockPos pos, T blockEntity) {
        blockEntities.put(pos, blockEntity);
        return blockEntity;
    }

    public void removeBlockEntity(BlockPos pos) {
        blockEntities.remove(pos);
    }
}
