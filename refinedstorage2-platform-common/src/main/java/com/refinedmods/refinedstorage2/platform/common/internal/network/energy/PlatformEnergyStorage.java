package com.refinedmods.refinedstorage2.platform.common.internal.network.energy;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.network.energy.EnergyStorage;

public class PlatformEnergyStorage implements EnergyStorage {
    private final EnergyStorage energyStorage;
    private final Runnable listener;

    public PlatformEnergyStorage(EnergyStorage energyStorage, Runnable listener) {
        this.energyStorage = energyStorage;
        this.listener = listener;
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
    public long receive(long amount, Action action) {
        long remainder = energyStorage.receive(amount, action);
        boolean receivedSometing = remainder != amount;
        if (action == Action.EXECUTE && receivedSometing) {
            listener.run();
        }
        return remainder;
    }

    public void receiveSilently(long amount) {
        energyStorage.receive(amount, Action.EXECUTE);
    }

    @Override
    public long extract(long amount, Action action) {
        long extracted = energyStorage.extract(amount, action);
        if (action == Action.EXECUTE && extracted > 0) {
            listener.run();
        }
        return extracted;
    }
}
