package com.refinedmods.refinedstorage2.platform.common.item.block;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.block.Block;

public class CableBlockItem extends AbstractNamedBlockItem {
    public CableBlockItem(final Block block, final CreativeModeTab tab, final Component name) {
        super(block, new Properties().tab(tab), name);
    }
}
