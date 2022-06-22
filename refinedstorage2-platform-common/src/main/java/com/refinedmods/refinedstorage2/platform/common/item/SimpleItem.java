package com.refinedmods.refinedstorage2.platform.common.item;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;

public final class SimpleItem extends Item {
    public SimpleItem(CreativeModeTab tab) {
        super(new Item.Properties().tab(tab));
    }
}
