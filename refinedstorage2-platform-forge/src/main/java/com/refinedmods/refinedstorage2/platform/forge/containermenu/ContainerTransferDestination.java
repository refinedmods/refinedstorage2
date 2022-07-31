package com.refinedmods.refinedstorage2.platform.forge.containermenu;

import com.refinedmods.refinedstorage2.platform.common.containermenu.transfer.TransferDestination;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.wrapper.InvWrapper;

public record ContainerTransferDestination(Container destination) implements TransferDestination {
    @Override
    public ItemStack transfer(final ItemStack stack) {
        final InvWrapper destinationInv = new InvWrapper(destination);
        return ItemHandlerHelper.insertItem(destinationInv, stack, false);
    }
}
