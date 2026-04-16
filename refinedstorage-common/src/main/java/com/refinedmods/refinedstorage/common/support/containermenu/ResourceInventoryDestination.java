package com.refinedmods.refinedstorage.common.support.containermenu;

import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

record ResourceInventoryDestination(AbstractResourceContainerMenu destination) implements TransferDestination {
    @Override
    @Nullable
    public ItemStack transfer(final ItemStack stack) {
        destination.addToResourceSlotIfNotExisting(stack);
        return null;
    }
}
