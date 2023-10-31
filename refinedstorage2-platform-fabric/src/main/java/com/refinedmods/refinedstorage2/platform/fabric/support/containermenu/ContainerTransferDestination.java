package com.refinedmods.refinedstorage2.platform.fabric.support.containermenu;

import com.refinedmods.refinedstorage2.platform.common.support.containermenu.TransferDestination;

import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

public record ContainerTransferDestination(Container destination) implements TransferDestination {
    @Override
    public ItemStack transfer(final ItemStack stack) {
        final Storage<ItemVariant> storage = InventoryStorage.of(destination, null);
        try (Transaction tx = Transaction.openOuter()) {
            final long inserted = storage.insert(
                ItemVariant.of(stack),
                stack.getCount(),
                tx
            );
            tx.commit();
            final long remainder = stack.getCount() - inserted;
            final ItemStack remainderStack = stack.copy();
            remainderStack.setCount((int) remainder);
            return remainderStack;
        }
    }
}
