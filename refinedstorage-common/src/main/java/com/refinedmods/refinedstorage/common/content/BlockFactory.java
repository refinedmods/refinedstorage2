package com.refinedmods.refinedstorage.common.content;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;

@FunctionalInterface
public interface BlockFactory<T extends Block> {
    T createBlock(Identifier id, DyeColor color, MutableComponent name);
}
