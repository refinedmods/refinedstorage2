package com.refinedmods.refinedstorage2.platform.common.item.block;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public final class SimpleBlockItem extends BlockItem {
    public SimpleBlockItem(final Block block) {
        super(block, new Item.Properties());
    }
}
