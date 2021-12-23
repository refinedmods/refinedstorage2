package com.refinedmods.refinedstorage2.platform.fabric.integration.energy;

import com.refinedmods.refinedstorage2.api.network.energy.InfiniteEnergyStorage;

import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

public class InfiniteDualEnergyStorage extends InfiniteEnergyStorage implements DualEnergyStorage {
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
}
