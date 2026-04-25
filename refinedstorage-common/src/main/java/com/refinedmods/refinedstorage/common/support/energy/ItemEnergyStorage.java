package com.refinedmods.refinedstorage.common.support.energy;

import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.network.energy.EnergyStorage;
import com.refinedmods.refinedstorage.api.network.impl.energy.AbstractListeningEnergyStorage;
import com.refinedmods.refinedstorage.common.api.support.energy.EnergyItemContext;
import com.refinedmods.refinedstorage.common.content.DataComponents;

import net.minecraft.world.item.ItemStack;

public class ItemEnergyStorage extends AbstractListeningEnergyStorage {
    private final EnergyItemContext context;

    public ItemEnergyStorage(final ItemStack stack, final EnergyStorage delegate, final EnergyItemContext context) {
        super(delegate);
        this.context = context;
        updateStored(stack, delegate);
    }

    private static void updateStored(final ItemStack stack, final EnergyStorage delegate) {
        final Long stored = stack.get(DataComponents.INSTANCE.getEnergy());
        if (stored != null && stored > 0) {
            delegate.receive(stored, Action.EXECUTE);
        }
    }

    @Override
    protected void onStoredChanged(final long stored) {
        final ItemStack copiedStack = context.copyStack();
        copiedStack.set(DataComponents.INSTANCE.getEnergy(), stored);
        context.setStack(copiedStack);
    }
}
