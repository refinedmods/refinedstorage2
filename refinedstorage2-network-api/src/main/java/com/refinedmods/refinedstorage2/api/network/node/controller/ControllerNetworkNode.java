package com.refinedmods.refinedstorage2.api.network.node.controller;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.network.energy.EnergyStorage;
import com.refinedmods.refinedstorage2.api.network.energy.EnergyStorageImpl;
import com.refinedmods.refinedstorage2.api.network.energy.InfiniteEnergyStorage;
import com.refinedmods.refinedstorage2.api.network.node.NetworkNodeImpl;

public class ControllerNetworkNode extends NetworkNodeImpl implements EnergyStorage {
    private final EnergyStorage energyStorage;
    private final ControllerListener listener;

    public ControllerNetworkNode(long stored, long capacity, ControllerType type, ControllerListener listener) {
        this.energyStorage = buildEnergyStorage(capacity, type);
        this.energyStorage.receive(stored, Action.EXECUTE);
        this.listener = listener;
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
        long remainder = energyStorage.receive(amount, action);
        if (remainder != amount && action == Action.EXECUTE) {
            listener.onEnergyChanged();
        }
        return remainder;
    }

    @Override
    public long extract(long amount, Action action) {
        if (!isActive()) {
            return 0;
        }
        long extracted = energyStorage.extract(amount, action);
        if (extracted > 0L && action == Action.EXECUTE) {
            listener.onEnergyChanged();
        }
        return extracted;
    }

    @Override
    public long getEnergyUsage() {
        return 0L;
    }
}
