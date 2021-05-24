package com.refinedmods.refinedstorage2.core.network.node.controller;

import com.refinedmods.refinedstorage2.core.Rs2World;
import com.refinedmods.refinedstorage2.core.network.energy.CreativeEnergyStorage;
import com.refinedmods.refinedstorage2.core.network.energy.EnergyStorage;
import com.refinedmods.refinedstorage2.core.network.energy.EnergyStorageImpl;
import com.refinedmods.refinedstorage2.core.network.node.NetworkNodeImpl;
import com.refinedmods.refinedstorage2.core.util.Action;
import com.refinedmods.refinedstorage2.core.util.Position;

public class ControllerNetworkNode extends NetworkNodeImpl implements EnergyStorage {
    private final EnergyStorage energyStorage;

    public ControllerNetworkNode(Rs2World world, Position pos, long stored, long capacity, ControllerType type) {
        super(world, pos);
        this.energyStorage = buildEnergyStorage(capacity, type);
        this.energyStorage.receive(stored, Action.EXECUTE);
    }

    private static EnergyStorage buildEnergyStorage(long capacity, ControllerType type) {
        return type == ControllerType.CREATIVE ? new CreativeEnergyStorage() : new EnergyStorageImpl(capacity);
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
        return redstoneMode.isActive(world.isPowered(position));
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
    protected long getEnergyUsage() {
        return 0;
    }
}
