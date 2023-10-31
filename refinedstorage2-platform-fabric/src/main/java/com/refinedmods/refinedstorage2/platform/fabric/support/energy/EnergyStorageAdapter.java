package com.refinedmods.refinedstorage2.platform.fabric.support.energy;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.network.energy.EnergyStorage;

import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;

public class EnergyStorageAdapter extends SnapshotParticipant<Long> implements team.reborn.energy.api.EnergyStorage {
    private final EnergyStorage energyStorage;

    public EnergyStorageAdapter(final EnergyStorage energyStorage) {
        this.energyStorage = energyStorage;
    }

    public EnergyStorage getEnergyStorage() {
        return energyStorage;
    }

    @Override
    public long insert(final long maxAmount, final TransactionContext transaction) {
        final long insertedSimulated = energyStorage.receive(maxAmount, Action.SIMULATE);
        if (insertedSimulated > 0) {
            updateSnapshots(transaction);
        }
        return energyStorage.receive(maxAmount, Action.EXECUTE);
    }

    @Override
    public long extract(final long maxAmount, final TransactionContext transaction) {
        final long extractedSimulated = energyStorage.extract(maxAmount, Action.SIMULATE);
        if (extractedSimulated > 0) {
            updateSnapshots(transaction);
        }
        return energyStorage.extract(maxAmount, Action.EXECUTE);
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
    protected void readSnapshot(final Long snapshot) {
        energyStorage.extract(Long.MAX_VALUE, Action.EXECUTE);
        energyStorage.receive(snapshot, Action.EXECUTE);
    }
}
