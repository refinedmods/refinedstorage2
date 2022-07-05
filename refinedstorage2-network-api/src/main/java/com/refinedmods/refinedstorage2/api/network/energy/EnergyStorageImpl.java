package com.refinedmods.refinedstorage2.api.network.energy;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.core.CoreValidations;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.0")
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
        if (stored + amount > capacity) {
            return receivePartly(action);
        } else {
            return receiveCompletely(amount, action);
        }
    }

    private long receiveCompletely(final long amount, final Action action) {
        if (action == Action.EXECUTE) {
            stored += amount;
        }
        return amount;
    }

    private long receivePartly(final Action action) {
        final long spaceRemainingInStorage = capacity - stored;
        if (spaceRemainingInStorage == 0) {
            return 0;
        }
        if (action == Action.EXECUTE) {
            stored += spaceRemainingInStorage;
        }
        return spaceRemainingInStorage;
    }

    @Override
    public long extract(final long amount, final Action action) {
        if (amount > stored) {
            return extractCompletely(action);
        }
        return extractPartly(amount, action);
    }

    private long extractPartly(final long amount, final Action action) {
        if (action == Action.EXECUTE) {
            this.stored -= amount;
        }
        return amount;
    }

    private long extractCompletely(final Action action) {
        final long extracted = stored;
        if (action == Action.EXECUTE) {
            this.stored = 0;
        }
        return extracted;
    }
}
