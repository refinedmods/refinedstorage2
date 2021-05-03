package com.refinedmods.refinedstorage2.core.network;

import com.refinedmods.refinedstorage2.core.util.Action;

import com.google.common.base.Preconditions;

public class EnergyStorageImpl implements EnergyStorage {
    private final long capacity;
    private long stored;

    public EnergyStorageImpl(long capacity) {
        Preconditions.checkArgument(capacity >= 0, "Capacity must be 0 or larger than 0");
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
    public long receive(long amount, Action action) {
        if (stored + amount > capacity) {
            return receivePartly(amount, action);
        } else {
            return receiveCompletely(amount, action);
        }
    }

    private long receiveCompletely(long amount, Action action) {
        if (action == Action.EXECUTE) {
            stored += amount;
        }
        return 0;
    }

    private long receivePartly(long amount, Action action) {
        long remainder = (stored + amount) - capacity;
        if (action == Action.EXECUTE) {
            stored = capacity;
        }
        return remainder;
    }

    @Override
    public long extract(long amount, Action action) {
        if (amount > stored) {
            return extractCompletely(action);
        }
        return extractPartly(amount, action);
    }

    private long extractPartly(long amount, Action action) {
        if (action == Action.EXECUTE) {
            this.stored -= amount;
        }
        return amount;
    }

    private long extractCompletely(Action action) {
        long extracted = stored;
        if (action == Action.EXECUTE) {
            this.stored = 0;
        }
        return extracted;
    }
}
