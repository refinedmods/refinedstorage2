package com.refinedmods.refinedstorage2.fabric.block.entity;

import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;

public interface BlockEntityWithDrops {
    DefaultedList<ItemStack> getDrops();
}
