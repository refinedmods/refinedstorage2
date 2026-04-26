package com.refinedmods.refinedstorage.fabric.support.energy;

import com.refinedmods.refinedstorage.common.api.support.energy.EnergyItemContext;

import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;

public class ContainerItemContextEnergyItemContext implements EnergyItemContext {
    @Nullable
    private final ContainerItemContext context;

    public ContainerItemContextEnergyItemContext(@Nullable final ContainerItemContext context) {
        this.context = context;
    }

    @Override
    public ItemStack copyStack() {
        if (context == null) {
            return ItemStack.EMPTY;
        }
        return context.getItemVariant().toStack();
    }

    @Override
    @SuppressWarnings("deprecation")
    public void setStack(final ItemStack stack) {
        if (context == null) {
            return;
        }
        final Transaction.Lifecycle lifecycle = Transaction.getLifecycle();
        // We cannot get the current transaction (to change the stack) when it's closing.
        // If this is called while a transaction is closing, this is probably called as part of
        // revertToSnapshot. We can just ignore it here and assume transaction semantics will roll back to the correct
        // stack.
        if (lifecycle == Transaction.Lifecycle.CLOSING || lifecycle == Transaction.Lifecycle.OUTER_CLOSING) {
            return;
        }
        if (lifecycle == Transaction.Lifecycle.NONE) {
            try (Transaction newTransaction = Transaction.openOuter()) {
                context.exchange(ItemVariant.of(stack), stack.getCount(), newTransaction);
                newTransaction.commit();
            }
        } else if (lifecycle == Transaction.Lifecycle.OPEN) {
            context.exchange(ItemVariant.of(stack), stack.getCount(),
                requireNonNull(Transaction.getCurrentUnsafe()));
        }
    }
}
