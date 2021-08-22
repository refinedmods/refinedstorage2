package com.refinedmods.refinedstorage2.api.network.node.controller;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.network.energy.EnergyStorage;
import com.refinedmods.refinedstorage2.api.network.energy.EnergyStorageImpl;
import com.refinedmods.refinedstorage2.api.network.energy.InfiniteEnergyStorage;
import com.refinedmods.refinedstorage2.api.network.node.NetworkNodeImpl;

public class ControllerNetworkNode extends NetworkNodeImpl implements EnergyStorage {
    private final EnergyStorage energyStorage;

    public ControllerNetworkNode(long stored, long capacity, ControllerType type) {
        this.energyStorage = buildEnergyStorage(capacity, type);
        this.energyStorage.receive(stored, Action.EXECUTE);
    }

    private static EnergyStorage buildEnergyStorage(long capacity, ControllerType type) {
        return type == ControllerType.CREATIVE ? new InfiniteEnergyStorage() : new EnergyStorageImpl(capacity);
    }

    public ControllerEnergyState getState() {
        if (!isActive()) {
            return ControllerEnergyState.OFF;
        }
        double pct = (double) energyStorage.getStored() / (double) energyStorage.getCapacity();
        if (pct >= 0.4) {
            return ControllerEnergyState.ON;
        }
        if (pct >= 0.3) {
            return ControllerEnergyState.NEARLY_ON;
        }
        if (pct >= 0.01) {
            return ControllerEnergyState.NEARLY_OFF;
        }
        return ControllerEnergyState.OFF;
    }

    public long getActualStored() {
        return energyStorage.getStored();
    }

    public long getActualCapacity() {
        return energyStorage.getCapacity();
    }

    @Override
    public long getStored() {
        if (!isActive()) {
            return 0;
        }
        return energyStorage.getStored();
    }

    @Override
    public long getCapacity() {
        if (!isActive()) {
            return 0;
        }
        return energyStorage.getCapacity();
    }

    @Override
    public long receive(long amount, Action action) {
        return energyStorage.receive(amount, action);
    }

    @Override
    public long extract(long amount, Action action) {
        if (!isActive()) {
            return 0;
        }
        return energyStorage.extract(amount, action);
    }

    @Override
    public long getEnergyUsage() {
        return 0L;
    }
}
