package com.refinedmods.refinedstorage2.platform.common.item.block;

import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;

public class CableBlockItem extends NamedBlockItem {
    public CableBlockItem(final Block block, final Component name) {
        super(block, new Properties(), name);
    }
}
