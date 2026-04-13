package com.refinedmods.refinedstorage.neoforge.support.containermenu;

import com.refinedmods.refinedstorage.common.support.containermenu.TransferDestination;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.VanillaContainerWrapper;
import net.neoforged.neoforge.transfer.transaction.Transaction;

public record ContainerTransferDestination(Container destination) implements TransferDestination {
    @Override
    public ItemStack transfer(final ItemStack stack) {
        final ResourceHandler<ItemResource> target = VanillaContainerWrapper.of(destination);
        try (Transaction tx = Transaction.openRoot()) {
            final ItemResource resource = ItemResource.of(stack);
            final long inserted = target.insert(resource, stack.getCount(), tx);
            tx.commit();
            final long remainder = stack.getCount() - inserted;
            if (remainder == 0) {
                return ItemStack.EMPTY;
            } else {
                final ItemStack remainderStack = stack.copy();
                remainderStack.setCount((int) remainder);
                return remainderStack;
            }
        }
    }
}
