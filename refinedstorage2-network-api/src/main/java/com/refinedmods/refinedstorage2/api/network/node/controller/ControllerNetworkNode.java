package com.refinedmods.refinedstorage2.api.network.node.controller;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.network.energy.EnergyStorage;
import com.refinedmods.refinedstorage2.api.network.node.AbstractNetworkNode;

import javax.annotation.Nullable;

public class ControllerNetworkNode extends AbstractNetworkNode implements EnergyStorage {
    @Nullable
    private EnergyStorage energyStorage;

    public void setEnergyStorage(@Nullable final EnergyStorage energyStorage) {
        this.energyStorage = energyStorage;
    }

    public ControllerEnergyState getState() {
        if (!isActive() || energyStorage == null) {
            return ControllerEnergyState.OFF;
        }
        final double pct = (double) energyStorage.getStored() / (double) energyStorage.getCapacity();
        return getState(pct);
    }

    private ControllerEnergyState getState(final double pct) {
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
    public long getStored() {
        if (!isActive()) {
            return 0;
        }
        return getActualStored();
    }

    public long getActualStored() {
        return energyStorage == null ? 0L : energyStorage.getStored();
    }

    @Override
    public long getCapacity() {
        if (!isActive()) {
            return 0;
        }
        return getActualCapacity();
    }

    public long getActualCapacity() {
        return energyStorage == null ? 0L : energyStorage.getCapacity();
    }

    @Override
    public long receive(final long amount, final Action action) {
        if (energyStorage == null) {
            return 0L;
        }
        return energyStorage.receive(amount, action);
    }

    @Override
    public long extract(final long amount, final Action action) {
        if (energyStorage == null) {
            return 0L;
        }
        return energyStorage.extract(amount, action);
    }

    @Override
    public long getEnergyUsage() {
        return 0L;
    }
}
