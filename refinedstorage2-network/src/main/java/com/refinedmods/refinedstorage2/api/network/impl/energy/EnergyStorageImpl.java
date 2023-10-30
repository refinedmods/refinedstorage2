package com.refinedmods.refinedstorage2.api.network.impl.energy;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.core.CoreValidations;
import com.refinedmods.refinedstorage2.api.network.energy.EnergyStorage;

public class EnergyStorageImpl implements EnergyStorage {
    private final long capacity;
    private long stored;

    /**
     * @param capacity the capacity, must be larger than 0
     */
    public EnergyStorageImpl(final long capacity) {
        CoreValidations.validateNotNegative(capacity, "Capacity must be non-negative");
        this.capacity = capacity;
    }

    @Override
    public long getStored() {
        return stored;
    }

    @Override
    public long getCapacity() {
        return capacity;
    }

    @Override
    public long receive(final long amount, final Action action) {
        final long spaceRemaining = capacity - stored;
        final long maxReceive = Math.min(amount, spaceRemaining);
        if (maxReceive > 0 && action == Action.EXECUTE) {
            stored += maxReceive;
            changed();
        }
        return maxReceive;
    }

    @Override
    public long extract(final long amount, final Action action) {
        final long maxExtract = Math.min(stored, amount);
        if (maxExtract > 0 && action == Action.EXECUTE) {
            stored -= maxExtract;
            changed();
        }
        return maxExtract;
    }

    protected void changed() {
        // no op
    }
}
