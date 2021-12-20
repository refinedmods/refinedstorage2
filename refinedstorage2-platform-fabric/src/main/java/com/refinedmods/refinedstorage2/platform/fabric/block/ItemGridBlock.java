package com.refinedmods.refinedstorage2.platform.fabric.block;

import com.refinedmods.refinedstorage2.platform.fabric.Rs2Mod;
import com.refinedmods.refinedstorage2.platform.fabric.block.entity.grid.ItemGridBlockEntity;
import com.refinedmods.refinedstorage2.platform.fabric.init.BlockColorMap;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class ItemGridBlock extends GridBlock {
    private final MutableComponent name;

    public ItemGridBlock(Properties properties, MutableComponent name) {
        super(properties);
        this.name = name;
    }

    @Override
    public MutableComponent getName() {
        return this.name;
    }

    @Override
    protected BlockColorMap<?> getBlockColorMap() {
        return Rs2Mod.BLOCKS.getGrid();
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ItemGridBlockEntity(pos, state);
    }
}
