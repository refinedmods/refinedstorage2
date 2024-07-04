package com.refinedmods.refinedstorage.platform.common.support.energy;

import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.network.energy.EnergyStorage;
import com.refinedmods.refinedstorage.api.network.impl.energy.AbstractListeningEnergyStorage;
import com.refinedmods.refinedstorage.platform.common.content.DataComponents;

import net.minecraft.world.item.ItemStack;

public class ItemEnergyStorage extends AbstractListeningEnergyStorage {
    private final ItemStack stack;

    public ItemEnergyStorage(final ItemStack stack, final EnergyStorage delegate) {
        super(delegate);
        this.stack = stack;
        final Long stored = stack.get(DataComponents.INSTANCE.getEnergy());
        if (stored != null && stored > 0) {
            delegate.receive(stored, Action.EXECUTE);
        }
    }

    @Override
    protected void onStoredChanged(final long stored) {
        stack.set(DataComponents.INSTANCE.getEnergy(), stored);
    }
}
