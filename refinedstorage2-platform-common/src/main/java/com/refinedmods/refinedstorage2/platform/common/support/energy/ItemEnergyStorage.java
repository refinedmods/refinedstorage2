package com.refinedmods.refinedstorage2.platform.common.support.energy;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.network.energy.EnergyStorage;
import com.refinedmods.refinedstorage2.api.network.impl.energy.AbstractProxyEnergyStorage;

import net.minecraft.world.item.ItemStack;

public class ItemEnergyStorage extends AbstractProxyEnergyStorage {
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
    public long receive(final long amount, final Action action) {
        final long received = super.receive(amount, action);
        if (received > 0 && action == Action.EXECUTE) {
            updateStored();
        }
        return received;
    }

    @Override
    public long extract(final long amount, final Action action) {
        final long extracted = super.extract(amount, action);
        if (extracted > 0 && action == Action.EXECUTE) {
            updateStored();
        }
        return extracted;
    }

    private void updateStored() {
        stack.getOrCreateTag().putLong(TAG_STORED, getStored());
    }
}
