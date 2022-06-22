package com.refinedmods.refinedstorage2.platform.common.item;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;

public class WrenchItem extends Item {
    public WrenchItem(CreativeModeTab tab) {
        super(new Item.Properties().tab(tab).stacksTo(1));
    }
}
