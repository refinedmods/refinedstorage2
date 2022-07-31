package com.refinedmods.refinedstorage2.platform.common.containermenu.transfer;

import com.refinedmods.refinedstorage2.platform.common.containermenu.AbstractResourceFilterContainerMenu;

import net.minecraft.world.item.ItemStack;

public record FilterInventoryDestination(AbstractResourceFilterContainerMenu destination)
    implements TransferDestination {
    @Override
    public ItemStack transfer(final ItemStack stack) {
        destination.addToFilterIfNotExisting(stack);
        return null;
    }
}
