package com.refinedmods.refinedstorage.neoforge.support.energy;

import com.refinedmods.refinedstorage.common.api.support.energy.EnergyItemContext;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public class EnergyItemContextItemAccess implements ItemAccess {
    private final EnergyItemContext context;

    public EnergyItemContextItemAccess(final EnergyItemContext context) {
        this.context = context;
    }

    @Override
    public ItemResource getResource() {
        return ItemResource.of(context.copyStack());
    }

    @Override
    public int getAmount() {
        return context.copyStack().getCount();
    }

    @Override
    public int insert(final ItemResource resource, final int amount, final TransactionContext transaction) {
        context.setStack(resource.toStack(amount));
        return amount;
    }

    @Override
    public int extract(final ItemResource resource, final int amount, final TransactionContext transaction) {
        final ItemStack existingStack = context.copyStack();
        final ItemResource existingResource = ItemResource.of(existingStack);
        if (!existingResource.equals(resource)) {
            return 0;
        }
        final int extractedAmount = Math.min(existingStack.getCount(), amount);
        context.setStack(existingResource.toStack(existingStack.getCount() - extractedAmount));
        return extractedAmount;
    }
}
