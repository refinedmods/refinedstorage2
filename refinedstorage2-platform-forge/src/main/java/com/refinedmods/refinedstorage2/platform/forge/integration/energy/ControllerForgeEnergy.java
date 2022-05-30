package com.refinedmods.refinedstorage2.platform.forge.integration.energy;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.network.energy.EnergyStorageImpl;
import com.refinedmods.refinedstorage2.platform.common.Platform;

import net.minecraftforge.energy.IEnergyStorage;

public class ControllerForgeEnergy extends EnergyStorageImpl implements IEnergyStorage {
    private final Runnable listener;

    public ControllerForgeEnergy(Runnable listener) {
        super((int) Platform.INSTANCE.getConfig().getController().getEnergyCapacity());
        this.listener = listener;
    }

    public void setSilently(long amount) {
        this.extract(Long.MAX_VALUE, Action.EXECUTE);
        this.receive(amount, Action.EXECUTE);
    }

    @Override
    public long receive(long amount, Action action) {
        long received = super.receive(amount, action);
        if (received > 0 && action == Action.EXECUTE) {
            listener.run();
        }
        return received;
    }

    @Override
    public long extract(long amount, Action action) {
        long extracted = super.extract(amount, action);
        if (extracted > 0 && action == Action.EXECUTE) {
            listener.run();
        }
        return extracted;
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        return (int) this.receive(maxReceive, simulate ? Action.SIMULATE : Action.EXECUTE);
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        return 0;
    }

    @Override
    public int getEnergyStored() {
        return (int) this.getStored();
    }

    @Override
    public int getMaxEnergyStored() {
        return (int) this.getCapacity();
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
