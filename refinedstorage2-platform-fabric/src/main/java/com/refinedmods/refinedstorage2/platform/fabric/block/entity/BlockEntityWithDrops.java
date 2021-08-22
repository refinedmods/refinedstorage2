package com.refinedmods.refinedstorage2.platform.fabric.block.entity;

import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;

public interface BlockEntityWithDrops {
    DefaultedList<ItemStack> getDrops();
}
