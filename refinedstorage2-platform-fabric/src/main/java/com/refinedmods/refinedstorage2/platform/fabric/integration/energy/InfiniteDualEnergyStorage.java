package com.refinedmods.refinedstorage2.platform.fabric.integration.energy;

import com.refinedmods.refinedstorage2.api.core.Action;

import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

public class InfiniteDualEnergyStorage implements DualEnergyStorage {
    @Override
    public boolean supportsInsertion() {
        return false;
    }

    @Override
    public long insert(long maxAmount, TransactionContext transaction) {
        return 0;
    }

    @Override
    public long extract(long maxAmount, TransactionContext transaction) {
        return maxAmount;
    }

    @Override
    public long getAmount() {
        return Long.MAX_VALUE;
    }

    @Override
    public long getStored() {
        return Long.MAX_VALUE;
    }

    @Override
    public long getCapacity() {
        return Long.MAX_VALUE;
    }

    @Override
    public long receive(long amount, Action action) {
        return amount;
    }

    @Override
    public long extract(long amount, Action action) {
        return amount;
    }
}
