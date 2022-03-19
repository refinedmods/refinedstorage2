package com.refinedmods.refinedstorage2.api.network.energy;

import com.refinedmods.refinedstorage2.api.core.Action;

import com.google.common.base.Preconditions;
import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.0")
public class EnergyStorageImpl implements EnergyStorage {
    private final long capacity;
    private long stored;

    /**
     * @param capacity the capacity, must be larger than 0
     */
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
            return receivePartly(action);
        } else {
            return receiveCompletely(amount, action);
        }
    }

    private long receiveCompletely(long amount, Action action) {
        if (action == Action.EXECUTE) {
            stored += amount;
        }
        return amount;
    }

    private long receivePartly(Action action) {
        long spaceRemainingInStorage = capacity - stored;
        if (spaceRemainingInStorage == 0) {
            return 0;
        }
        if (action == Action.EXECUTE) {
            stored += spaceRemainingInStorage;
        }
        return spaceRemainingInStorage;
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
