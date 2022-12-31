package com.refinedmods.refinedstorage2.api.network.impl.energy;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.network.energy.EnergyStorage;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.0")
public class InfiniteEnergyStorage implements EnergyStorage {
    @Override
    public long getStored() {
        return Long.MAX_VALUE;
    }

    @Override
    public long getCapacity() {
        return Long.MAX_VALUE;
    }

    @Override
    public long receive(final long amount, final Action action) {
        return 0;
    }

    @Override
    public long extract(final long amount, final Action action) {
        return amount;
    }
}
