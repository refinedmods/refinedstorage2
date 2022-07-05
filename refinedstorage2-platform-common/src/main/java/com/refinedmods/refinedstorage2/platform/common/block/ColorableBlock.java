package com.refinedmods.refinedstorage2.platform.common.block;

import com.refinedmods.refinedstorage2.platform.common.content.BlockColorMap;

import net.minecraft.world.level.block.Block;

public interface ColorableBlock<T extends Block> {
    BlockColorMap<T> getBlockColorMap();
}
