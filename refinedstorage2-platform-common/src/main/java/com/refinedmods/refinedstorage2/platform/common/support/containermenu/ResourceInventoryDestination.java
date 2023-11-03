package com.refinedmods.refinedstorage2.platform.common.support.containermenu;

import net.minecraft.world.item.ItemStack;

record ResourceInventoryDestination(AbstractResourceContainerMenu destination) implements TransferDestination {
    @Override
    public ItemStack transfer(final ItemStack stack) {
        destination.addToResourceSlotIfNotExisting(stack);
        return null;
    }
}
