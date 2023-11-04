package com.refinedmods.refinedstorage2.platform.common.portablegrid;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class PortableGridItem extends BlockItem {
    public PortableGridItem(final Block block) {
        super(block, new Item.Properties().stacksTo(1));
    }
}
