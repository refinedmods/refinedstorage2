package com.refinedmods.refinedstorage.neoforge.support.energy;

import com.refinedmods.refinedstorage.common.api.support.energy.EnergyItemContext;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import static java.util.Objects.requireNonNull;

public class ItemAccessEnergyItemContext implements EnergyItemContext {
    private final ItemAccess itemAccess;

    public ItemAccessEnergyItemContext(final ItemAccess itemAccess) {
        this.itemAccess = itemAccess;
    }

    @Override
    public ItemStack copyStack() {
        return itemAccess.getResource().toStack();
    }

    @Override
    @SuppressWarnings("deprecation")
    public void setStack(final ItemStack stack) {
        final Transaction.Lifecycle lifecycle = Transaction.getLifecycle();
        // We cannot get the current transaction (to change the stack) when it's closing.
        // If this is called while a transaction is closing, this is probably called as part of
        // revertToSnapshot. We can just ignore it here and assume transaction semantics will roll back to the correct
        // stack.
        if (lifecycle == Transaction.Lifecycle.CLOSING || lifecycle == Transaction.Lifecycle.ROOT_CLOSING) {
            return;
        }
        if (lifecycle == Transaction.Lifecycle.NONE) {
            try (Transaction newTransaction = Transaction.openRoot()) {
                itemAccess.exchange(ItemResource.of(stack), stack.getCount(), newTransaction);
                newTransaction.commit();
            }
        } else if (lifecycle == Transaction.Lifecycle.OPEN) {
            itemAccess.exchange(ItemResource.of(stack), stack.getCount(),
                requireNonNull(Transaction.getCurrentOpenedTransaction()));
        }
    }
}
