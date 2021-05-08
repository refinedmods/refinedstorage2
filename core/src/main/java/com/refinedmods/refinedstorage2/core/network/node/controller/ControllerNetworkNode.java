package com.refinedmods.refinedstorage2.core.network.node.controller;

import com.refinedmods.refinedstorage2.core.Rs2World;
import com.refinedmods.refinedstorage2.core.network.CreativeEnergyStorage;
import com.refinedmods.refinedstorage2.core.network.EnergyStorage;
import com.refinedmods.refinedstorage2.core.network.EnergyStorageImpl;
import com.refinedmods.refinedstorage2.core.network.node.NetworkNodeImpl;
import com.refinedmods.refinedstorage2.core.network.node.NetworkNodeReference;
import com.refinedmods.refinedstorage2.core.util.Action;
import com.refinedmods.refinedstorage2.core.util.Position;

public class ControllerNetworkNode extends NetworkNodeImpl implements EnergyStorage {
    private final EnergyStorage energyStorage;

    public ControllerNetworkNode(Rs2World world, Position pos, NetworkNodeReference ref, long capacity, ControllerType type) {
        super(world, pos, ref);
        this.energyStorage = buildEnergyStorage(capacity, type);
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
        return getRedstoneMode().isActive(world.isPowered(getPosition()));
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
        if (!isActive()) {
            return amount;
        }
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
        return 0;
    }
}
