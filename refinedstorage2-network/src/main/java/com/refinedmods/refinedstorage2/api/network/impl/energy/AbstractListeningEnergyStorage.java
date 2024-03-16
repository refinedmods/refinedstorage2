package com.refinedmods.refinedstorage2.api.network.impl.energy;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.network.energy.EnergyStorage;

public abstract class AbstractListeningEnergyStorage extends AbstractProxyEnergyStorage {
    protected AbstractListeningEnergyStorage(final EnergyStorage delegate) {
        super(delegate);
    }

    @Override
    public long receive(final long amount, final Action action) {
        final long received = super.receive(amount, action);
        if (received > 0 && action == Action.EXECUTE) {
            onStoredChanged(getStored());
        }
        return received;
    }

    @Override
    public long extract(final long amount, final Action action) {
        final long extracted = super.extract(amount, action);
        if (extracted > 0 && action == Action.EXECUTE) {
            onStoredChanged(getStored());
        }
        return extracted;
    }

    protected abstract void onStoredChanged(long stored);
}
