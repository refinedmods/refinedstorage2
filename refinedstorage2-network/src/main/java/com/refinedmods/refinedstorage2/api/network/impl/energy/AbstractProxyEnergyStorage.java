package com.refinedmods.refinedstorage2.api.network.impl.energy;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.network.energy.EnergyStorage;

public abstract class AbstractProxyEnergyStorage implements EnergyStorage {
    private final EnergyStorage energyStorage;

    protected AbstractProxyEnergyStorage(final EnergyStorage energyStorage) {
        this.energyStorage = energyStorage;
    }

    @Override
    public long getStored() {
        return energyStorage.getStored();
    }

    @Override
    public long getCapacity() {
        return energyStorage.getCapacity();
    }

    @Override
    public long receive(final long amount, final Action action) {
        return energyStorage.receive(amount, action);
    }

    @Override
    public long extract(final long amount, final Action action) {
        return energyStorage.extract(amount, action);
    }
}
