package com.refinedmods.refinedstorage2.platform.common.support.energy;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.network.energy.EnergyStorage;
import com.refinedmods.refinedstorage2.api.network.impl.energy.AbstractListeningEnergyStorage;

import net.minecraft.world.item.ItemStack;

public class ItemEnergyStorage extends AbstractListeningEnergyStorage {
    private static final String TAG_STORED = "stored";

    private final ItemStack stack;

    public ItemEnergyStorage(final ItemStack stack, final EnergyStorage delegate) {
        super(delegate);
        this.stack = stack;
        final long stored = stack.getTag() != null ? stack.getTag().getLong(TAG_STORED) : 0;
        if (stored > 0) {
            delegate.receive(stored, Action.EXECUTE);
        }
    }

    @Override
    protected void onStoredChanged(final long stored) {
        stack.getOrCreateTag().putLong(TAG_STORED, stored);
    }
}
