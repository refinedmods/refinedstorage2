package com.refinedmods.refinedstorage2.core.network;

import com.refinedmods.refinedstorage2.core.util.Action;

public interface EnergyStorage {
    long getStored();

    long getCapacity();

    long receive(long amount, Action action);

    long extract(long amount, Action action);
}
