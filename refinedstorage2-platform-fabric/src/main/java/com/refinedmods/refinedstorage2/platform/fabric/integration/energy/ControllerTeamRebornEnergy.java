package com.refinedmods.refinedstorage2.platform.fabric.integration.energy;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.network.energy.EnergyStorage;
import com.refinedmods.refinedstorage2.platform.abstractions.PlatformAbstractions;

import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import team.reborn.energy.api.base.LimitingEnergyStorage;
import team.reborn.energy.api.base.SimpleEnergyStorage;

public class ControllerTeamRebornEnergy extends SimpleEnergyStorage implements EnergyStorage {
    private final Runnable listener;
    private final LimitingEnergyStorage exposedStorage;

    public ControllerTeamRebornEnergy(Runnable listener) {
        super(PlatformAbstractions.INSTANCE.getConfig().getController().getCapacity(), PlatformAbstractions.INSTANCE.getConfig().getController().getCapacity(), PlatformAbstractions.INSTANCE.getConfig().getController().getCapacity());
        this.listener = listener;
        this.exposedStorage = new LimitingEnergyStorage(
                this,
                maxInsert,
                0
        );
    }

    public LimitingEnergyStorage getExposedStorage() {
        return exposedStorage;
    }

    public void setStoredSilently(long stored) {
        this.amount = stored;
    }

    @Override
    protected void onFinalCommit() {
        super.onFinalCommit();
        listener.run();
    }

    @Override
    public long getStored() {
        return getAmount();
    }

    @Override
    public long receive(long amount, Action action) {
        try (Transaction tx = Transaction.openOuter()) {
            long received = this.insert(amount, tx);
            if (received > 0 && action == Action.EXECUTE) {
                tx.commit();
            }
            return amount - received;
        }
    }

    @Override
    public long extract(long amount, Action action) {
        try (Transaction tx = Transaction.openOuter()) {
            long extracted = this.extract(amount, tx);
            if (extracted > 0 && action == Action.EXECUTE) {
                tx.commit();
            }
            return extracted;
        }
    }
}
