package com.refinedmods.refinedstorage2.core.network.node.controller;

import com.refinedmods.refinedstorage2.core.Rs2World;
import com.refinedmods.refinedstorage2.core.network.EnergyStorage;
import com.refinedmods.refinedstorage2.core.network.EnergyStorageImpl;
import com.refinedmods.refinedstorage2.core.network.node.NetworkNodeImpl;
import com.refinedmods.refinedstorage2.core.network.node.NetworkNodeReference;
import com.refinedmods.refinedstorage2.core.util.Action;
import com.refinedmods.refinedstorage2.core.util.Position;

public class ControllerNetworkNode extends NetworkNodeImpl implements EnergyStorage {
    private final EnergyStorage energyStorage;

    public ControllerNetworkNode(Rs2World world, Position pos, NetworkNodeReference ref, long capacity) {
        super(world, pos, ref);
        this.energyStorage = new EnergyStorageImpl(capacity);
    }

    public ControllerEnergyState getState() {
        double pct = (double) energyStorage.getStored() / (double) energyStorage.getCapacity();
        if (pct == 0) {
            return ControllerEnergyState.OFF;
        } else if (pct <= 0.1) {
            return ControllerEnergyState.NEARLY_OFF;
        } else if (pct <= 0.3) {
            return ControllerEnergyState.NEARLY_ON;
        }
        return ControllerEnergyState.ON;
    }

    @Override
    public long getStored() {
        return energyStorage.getStored();
    }

    @Override
    public long getCapacity() {
        return energyStorage.getCapacity();
    }

    @Override
    public void setCapacity(long capacity) {
        throw new UnsupportedOperationException();
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
        return 0;
    }
}
