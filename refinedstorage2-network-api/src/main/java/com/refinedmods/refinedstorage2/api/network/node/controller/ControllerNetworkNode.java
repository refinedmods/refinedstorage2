package com.refinedmods.refinedstorage2.api.network.node.controller;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.network.energy.EnergyStorage;
import com.refinedmods.refinedstorage2.api.network.node.NetworkNodeImpl;

public class ControllerNetworkNode extends NetworkNodeImpl implements EnergyStorage {
    private EnergyStorage energyStorage;

    public void setEnergyStorage(EnergyStorage energyStorage) {
        this.energyStorage = energyStorage;
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

    @Override
    public boolean isActive() {
        return activenessProvider.getAsBoolean();
    }

    @Override
    public long getStored() {
        if (!isActive()) {
            return 0;
        }
        return getActualStored();
    }

    public long getActualStored() {
        return energyStorage.getStored();
    }

    @Override
    public long getCapacity() {
        if (!isActive()) {
            return 0;
        }
        return getActualCapacity();
    }

    public long getActualCapacity() {
        return energyStorage.getCapacity();
    }

    @Override
    public long receive(long amount, Action action) {
        return energyStorage.receive(amount, action);
    }

    @Override
    public long extract(long amount, Action action) {
        return energyStorage.extract(amount, action);
    }

    @Override
    public long getEnergyUsage() {
        return 0L;
    }
}
