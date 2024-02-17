package com.refinedmods.refinedstorage2.platform.forge.support.containermenu;

import com.refinedmods.refinedstorage2.platform.common.support.containermenu.TransferDestination;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.wrapper.InvWrapper;

public record ContainerTransferDestination(Container destination) implements TransferDestination {
    @Override
    public ItemStack transfer(final ItemStack stack) {
        final InvWrapper destinationInv = new InvWrapper(destination);
        return ItemHandlerHelper.insertItem(destinationInv, stack, false);
    }
}
