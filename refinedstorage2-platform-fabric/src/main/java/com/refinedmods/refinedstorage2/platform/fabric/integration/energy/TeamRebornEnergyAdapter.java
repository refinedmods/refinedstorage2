package com.refinedmods.refinedstorage2.platform.fabric.integration.energy;

import com.refinedmods.refinedstorage2.api.core.Action;

import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import team.reborn.energy.api.EnergyStorage;

public class TeamRebornEnergyAdapter extends SnapshotParticipant<Long> implements EnergyStorage {
    private final com.refinedmods.refinedstorage2.api.network.energy.EnergyStorage energyStorage;

    public TeamRebornEnergyAdapter(com.refinedmods.refinedstorage2.api.network.energy.EnergyStorage energyStorage) {
        this.energyStorage = energyStorage;
    }

    @Override
    public long insert(long maxAmount, TransactionContext transaction) {
        long remainder = energyStorage.receive(maxAmount, Action.EXECUTE);
        boolean insertedSomething = remainder != maxAmount;
        if (insertedSomething) {
            updateSnapshots(transaction);
        }
        return maxAmount - remainder;
    }

    @Override
    public long extract(long maxAmount, TransactionContext transaction) {
        long extracted = energyStorage.extract(maxAmount, Action.EXECUTE);
        if (extracted > 0) {
            updateSnapshots(transaction);
        }
        return extracted;
    }

    @Override
    public long getAmount() {
        return energyStorage.getStored();
    }

    @Override
    public long getCapacity() {
        return energyStorage.getCapacity();
    }

    @Override
    protected Long createSnapshot() {
        return energyStorage.getStored();
    }

    @Override
    protected void readSnapshot(Long snapshot) {
        energyStorage.extract(Long.MAX_VALUE, Action.EXECUTE);
        energyStorage.receive(snapshot, Action.EXECUTE);
    }
}
