package com.refinedmods.refinedstorage2.platform.common.containermenu.transfer;

import javax.annotation.Nullable;

import net.minecraft.world.item.ItemStack;

public interface TransferDestination {
    @Nullable
    ItemStack transfer(ItemStack stack);
}
