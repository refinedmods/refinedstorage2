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
        long stored = 0;
        for (EnergyStorage source : sources) {
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
        for (EnergyStorage source : sources) {
            if (capacity + source.getCapacity() < 0) {
                return Long.MAX_VALUE;
            }
            capacity += source.getCapacity();
        }
        return capacity;
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
