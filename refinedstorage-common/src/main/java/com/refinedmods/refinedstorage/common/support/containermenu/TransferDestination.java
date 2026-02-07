package com.refinedmods.refinedstorage.common.support.containermenu;

import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

@FunctionalInterface
public interface TransferDestination {
    @Nullable
    ItemStack transfer(ItemStack stack);
}
