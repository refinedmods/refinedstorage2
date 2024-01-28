package com.refinedmods.refinedstorage2.platform.forge.support.energy;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.network.energy.EnergyStorage;

import net.neoforged.neoforge.energy.IEnergyStorage;

public class EnergyStorageAdapter implements IEnergyStorage {
    private final EnergyStorage energyStorage;

    public EnergyStorageAdapter(final EnergyStorage energyStorage) {
        this.energyStorage = energyStorage;
    }

    public EnergyStorage getEnergyStorage() {
        return energyStorage;
    }

    @Override
    public int receiveEnergy(final int maxReceive, final boolean simulate) {
        return (int) energyStorage.receive(maxReceive, simulate ? Action.SIMULATE : Action.EXECUTE);
    }

    @Override
    public int extractEnergy(final int maxExtract, final boolean simulate) {
        return (int) energyStorage.extract(maxExtract, simulate ? Action.SIMULATE : Action.EXECUTE);
    }

    @Override
    public int getEnergyStored() {
        return (int) energyStorage.getStored();
    }

    @Override
    public int getMaxEnergyStored() {
        return (int) energyStorage.getCapacity();
    }

    @Override
    public boolean canExtract() {
        return false;
    }

    @Override
    public boolean canReceive() {
        return true;
    }
}
