package com.refinedmods.refinedstorage2.core.network;

import com.refinedmods.refinedstorage2.core.util.Action;

import java.util.ArrayList;
import java.util.List;

public class CompositeEnergyStorage implements EnergyStorage {
    private List<EnergyStorage> sources = new ArrayList<>();

    public void setSources(List<EnergyStorage> sources) {
        this.sources = sources;
    }

    @Override
    public long getStored() {
        return sources.stream().mapToLong(EnergyStorage::getStored).sum();
    }

    @Override
    public long getCapacity() {
        return sources.stream().mapToLong(EnergyStorage::getCapacity).sum();
    }

    @Override
    public void setCapacity(long capacity) {
        throw new UnsupportedOperationException("Modify the capacity of an individual storage instead");
    }

    @Override
    public long receive(long amount, Action action) {
        long remainder = amount;
        for (EnergyStorage source : sources) {
            remainder = source.receive(remainder, action);
            if (remainder == 0) {
                break;
            }
        }
        return remainder;
    }

    @Override
    public long extract(long amount, Action action) {
        long extracted = 0;
        for (EnergyStorage source : sources) {
            extracted += source.extract(amount - extracted, action);
            if (extracted == amount) {
                break;
            }
        }
        return extracted;
    }
}
