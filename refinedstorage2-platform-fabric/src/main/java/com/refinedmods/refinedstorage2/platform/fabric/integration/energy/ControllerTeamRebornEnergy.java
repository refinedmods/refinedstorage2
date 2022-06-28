package com.refinedmods.refinedstorage2.platform.fabric.integration.energy;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.network.energy.EnergyStorage;
import com.refinedmods.refinedstorage2.platform.common.Platform;

import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import team.reborn.energy.api.base.LimitingEnergyStorage;
import team.reborn.energy.api.base.SimpleEnergyStorage;

public class ControllerTeamRebornEnergy extends SimpleEnergyStorage implements EnergyStorage {
    private final Runnable listener;
    private final LimitingEnergyStorage exposedStorage;

    public ControllerTeamRebornEnergy(final Runnable listener) {
        super(
                Platform.INSTANCE.getConfig().getController().getEnergyCapacity(),
                Platform.INSTANCE.getConfig().getController().getEnergyCapacity(),
                Platform.INSTANCE.getConfig().getController().getEnergyCapacity()
        );
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

    public void setStoredSilently(final long stored) {
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
    public long receive(final long amount, final Action action) {
        try (final Transaction tx = Transaction.openOuter()) {
            final long received = this.insert(amount, tx);
            if (received > 0 && action == Action.EXECUTE) {
                tx.commit();
            }
            return received;
        }
    }

    @Override
    public long extract(final long amount, final Action action) {
        try (final Transaction tx = Transaction.openOuter()) {
            final long extracted = this.extract(amount, tx);
            if (extracted > 0 && action == Action.EXECUTE) {
                tx.commit();
            }
            return extracted;
        }
    }
}
