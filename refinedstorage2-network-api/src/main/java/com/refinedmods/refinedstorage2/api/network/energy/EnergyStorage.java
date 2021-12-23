package com.refinedmods.refinedstorage2.api.network.energy;

import com.refinedmods.refinedstorage2.api.core.Action;

public interface EnergyStorage {
    long getStored();

    long getCapacity();

    long receive(long amount, Action action);

    long extract(long amount, Action action);
}
