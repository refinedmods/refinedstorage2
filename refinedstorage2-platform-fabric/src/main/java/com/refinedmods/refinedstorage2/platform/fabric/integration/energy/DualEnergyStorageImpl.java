package com.refinedmods.refinedstorage2.platform.fabric.integration.energy;

import com.refinedmods.refinedstorage2.api.core.Action;

import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import team.reborn.energy.api.base.SimpleEnergyStorage;

public class DualEnergyStorageImpl extends SimpleEnergyStorage implements DualEnergyStorage {
    public DualEnergyStorageImpl(long capacity, long maxInsert) {
        super(capacity, maxInsert, 0);
    }

    @Override
    public long getStored() {
        return this.getAmount();
    }

    @Override
    public long receive(long amount, Action action) {
        try (Transaction tx = Transaction.openOuter()) {
            long inserted = insert(amount, tx);
            if (action == Action.EXECUTE) {
                tx.commit();
            }
            return amount - inserted;
        }
    }

    @Override
    public long extract(long amount, Action action) {
        try (Transaction tx = Transaction.openOuter()) {
            long extracted = extractByBypassingMaxExtract(amount, tx);
            if (action == Action.EXECUTE) {
                tx.commit();
            }
            return extracted;
        }
    }

    private long extractByBypassingMaxExtract(long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notNegative(maxAmount);

        long extracted = Math.min(maxAmount, amount);

        if (extracted > 0) {
            updateSnapshots(transaction);
            amount -= extracted;
            return extracted;
        }

        return 0;
    }
}
