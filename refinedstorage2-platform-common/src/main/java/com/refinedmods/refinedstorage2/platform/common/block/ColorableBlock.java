package com.refinedmods.refinedstorage2.platform.common.block;

import com.refinedmods.refinedstorage2.platform.common.content.BlockColorMap;

import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;

public interface ColorableBlock<T extends Block & BlockItemProvider> {
    BlockColorMap<T> getBlockColorMap();

    DyeColor getColor();

    default boolean canAlwaysConnect() {
        return getBlockColorMap().isDefaultColor(getColor());
    }
}
