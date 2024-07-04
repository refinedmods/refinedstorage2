package com.refinedmods.refinedstorage.api.network.impl.node.controller;

import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.network.energy.EnergyProvider;
import com.refinedmods.refinedstorage.api.network.energy.EnergyStorage;
import com.refinedmods.refinedstorage.api.network.impl.node.AbstractNetworkNode;

import javax.annotation.Nullable;

public class ControllerNetworkNode extends AbstractNetworkNode implements EnergyProvider {
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
    public long extract(final long amount) {
        if (energyStorage == null) {
            return 0L;
        }
        return energyStorage.extract(amount, Action.EXECUTE);
    }

    @Override
    public long getEnergyUsage() {
        return 0L;
    }
}
