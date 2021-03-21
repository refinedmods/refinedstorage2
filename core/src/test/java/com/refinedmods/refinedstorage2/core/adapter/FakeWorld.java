package com.refinedmods.refinedstorage2.core.adapter;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.refinedmods.refinedstorage2.core.World;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;

public class FakeWorld implements World {
    private final Map<BlockPos, BlockEntity> blockEntities = new HashMap<>();

    @Override
    public Optional<BlockEntity> getBlockEntity(BlockPos pos) {
        return Optional.ofNullable(blockEntities.get(pos));
    }

    @Override
    public boolean isPowered(BlockPos pos) {
        return false;
    }

    public <T extends BlockEntity> T setBlockEntity(BlockPos pos, T blockEntity) {
        blockEntities.put(pos, blockEntity);
        return blockEntity;
    }

    public void removeBlockEntity(BlockPos pos) {
        blockEntities.remove(pos);
    }
}
