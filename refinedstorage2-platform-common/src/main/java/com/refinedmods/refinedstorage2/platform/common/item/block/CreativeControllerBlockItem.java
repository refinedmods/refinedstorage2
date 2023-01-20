package com.refinedmods.refinedstorage2.platform.common.item.block;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class CreativeControllerBlockItem extends AbstractNamedBlockItem {
    public CreativeControllerBlockItem(final Block block, final Component name) {
        super(block, new Item.Properties().stacksTo(1), name);
    }
}
