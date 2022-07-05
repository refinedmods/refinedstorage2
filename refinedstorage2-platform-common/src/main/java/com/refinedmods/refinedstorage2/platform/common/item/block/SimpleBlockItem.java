package com.refinedmods.refinedstorage2.platform.common.item.block;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public final class SimpleBlockItem extends BlockItem {
    public SimpleBlockItem(final Block block, final CreativeModeTab tab) {
        super(block, new Item.Properties().tab(tab));
    }
}
