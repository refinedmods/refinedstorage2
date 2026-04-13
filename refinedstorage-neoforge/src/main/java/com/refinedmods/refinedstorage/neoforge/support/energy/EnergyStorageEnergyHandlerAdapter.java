package com.refinedmods.refinedstorage.neoforge.support.energy;

import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.network.energy.EnergyStorage;

import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public class EnergyStorageEnergyHandlerAdapter extends SnapshotJournal<Long> implements EnergyHandler {
    private final EnergyStorage energyStorage;

    public EnergyStorageEnergyHandlerAdapter(final EnergyStorage energyStorage) {
        this.energyStorage = energyStorage;
    }

    public EnergyStorage getEnergyStorage() {
        return energyStorage;
    }

    @Override
    public long getAmountAsLong() {
        return energyStorage.getStored();
    }

    @Override
    public long getCapacityAsLong() {
        return energyStorage.getCapacity();
    }

    @Override
    public int insert(final int amount, final TransactionContext transaction) {
        final long insertedSimulated = energyStorage.receive(amount, Action.SIMULATE);
        if (insertedSimulated > 0) {
            updateSnapshots(transaction);
        }
        return (int) energyStorage.receive(amount, Action.EXECUTE);
    }

    @Override
    public int extract(final int amount, final TransactionContext transaction) {
        return 0;
    }

    @Override
    protected Long createSnapshot() {
        return energyStorage.getStored();
    }

    @Override
    protected void revertToSnapshot(final Long snapshot) {
        energyStorage.extract(Long.MAX_VALUE, Action.EXECUTE);
        energyStorage.receive(snapshot, Action.EXECUTE);
    }
}
