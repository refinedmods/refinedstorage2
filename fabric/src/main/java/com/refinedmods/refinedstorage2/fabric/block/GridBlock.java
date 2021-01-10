package com.refinedmods.refinedstorage2.fabric.block;

import com.refinedmods.refinedstorage2.fabric.block.entity.grid.GridBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.world.BlockView;
import org.jetbrains.annotations.Nullable;

public class GridBlock extends NetworkNodeBlock {
    public GridBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected boolean hasBiDirection() {
        return true;
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockView world) {
        return new GridBlockEntity();
    }
}
