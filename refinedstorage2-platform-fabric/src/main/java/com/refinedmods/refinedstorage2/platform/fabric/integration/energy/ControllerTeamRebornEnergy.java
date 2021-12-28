package com.refinedmods.refinedstorage2.platform.fabric.integration.energy;

import com.refinedmods.refinedstorage2.api.network.energy.EnergyStorage;

import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

public class ControllerTeamRebornEnergy extends TeamRebornEnergyAdapter {
    public ControllerTeamRebornEnergy(EnergyStorage energyStorage) {
        super(energyStorage);
    }

    @Override
    public boolean supportsExtraction() {
        return false;
    }

    @Override
    public long extract(long maxAmount, TransactionContext transaction) {
        return 0L;
    }
}
