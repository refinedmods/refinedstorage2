package com.refinedmods.refinedstorage2.api.network.impl.energy;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.network.energy.EnergyStorage;

import java.util.ArrayList;
import java.util.List;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.0")
public class CompositeEnergyStorage implements EnergyStorage {
    private final List<EnergyStorage> sources = new ArrayList<>();

    public void addSource(final EnergyStorage source) {
        sources.add(source);
    }

    public void removeSource(final EnergyStorage source) {
        sources.remove(source);
    }

    @Override
    public long getStored() {
        long stored = 0;
        for (final EnergyStorage source : sources) {
            if (stored + source.getStored() < 0) {
                return Long.MAX_VALUE;
            }
            stored += source.getStored();
        }
        return stored;
    }

    @Override
    public long getCapacity() {
        long capacity = 0;
        for (final EnergyStorage source : sources) {
            if (capacity + source.getCapacity() < 0) {
                return Long.MAX_VALUE;
            }
            capacity += source.getCapacity();
        }
        return capacity;
    }

    @Override
    public long receive(final long amount, final Action action) {
        long inserted = 0;
        for (final EnergyStorage source : sources) {
            inserted += source.receive(amount - inserted, action);
            if (inserted == amount) {
                break;
            }
        }
        return inserted;
    }

    @Override
    public long extract(final long amount, final Action action) {
        long extracted = 0;
        for (final EnergyStorage source : sources) {
            extracted += source.extract(amount - extracted, action);
            if (extracted == amount) {
                break;
            }
        }
        return extracted;
    }
}
