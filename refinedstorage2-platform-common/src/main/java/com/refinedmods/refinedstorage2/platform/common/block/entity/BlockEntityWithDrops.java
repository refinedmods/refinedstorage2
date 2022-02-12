package com.refinedmods.refinedstorage2.platform.common.block.entity;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;

public interface BlockEntityWithDrops {
    NonNullList<ItemStack> getDrops();
}
