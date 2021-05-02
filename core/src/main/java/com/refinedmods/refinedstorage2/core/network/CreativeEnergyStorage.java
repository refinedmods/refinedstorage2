package com.refinedmods.refinedstorage2.core.network;

import com.refinedmods.refinedstorage2.core.util.Action;

public class CreativeEnergyStorage implements EnergyStorage {
    @Override
    public long getStored() {
        return Long.MAX_VALUE;
    }

    @Override
    public long getCapacity() {
        return Long.MAX_VALUE;
    }

    @Override
    public void setCapacity(long capacity) {
    }

    @Override
    public long receive(long amount, Action action) {
        return amount;
    }

    @Override
    public long extract(long amount, Action action) {
        return amount;
    }
}
