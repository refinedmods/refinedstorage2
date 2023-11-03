package com.refinedmods.refinedstorage2.platform.common.support;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;

public interface BlockEntityWithDrops {
    NonNullList<ItemStack> getDrops();
}
