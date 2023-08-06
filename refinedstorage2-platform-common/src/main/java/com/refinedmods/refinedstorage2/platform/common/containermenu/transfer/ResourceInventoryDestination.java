package com.refinedmods.refinedstorage2.platform.common.containermenu.transfer;

import com.refinedmods.refinedstorage2.platform.common.containermenu.AbstractResourceContainerMenu;

import net.minecraft.world.item.ItemStack;

public record ResourceInventoryDestination(AbstractResourceContainerMenu destination)
    implements TransferDestination {
    @Override
    public ItemStack transfer(final ItemStack stack) {
        destination.addToResourceSlotIfNotExisting(stack);
        return null;
    }
}
