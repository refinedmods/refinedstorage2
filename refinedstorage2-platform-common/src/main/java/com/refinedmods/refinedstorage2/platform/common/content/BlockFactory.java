package com.refinedmods.refinedstorage2.platform.common.content;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;

@FunctionalInterface
public interface BlockFactory<T extends Block> {
    T createBlock(DyeColor color, MutableComponent name);
}
