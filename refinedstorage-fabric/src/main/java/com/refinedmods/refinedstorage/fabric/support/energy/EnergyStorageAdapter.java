package com.refinedmods.refinedstorage.fabric.support.energy;

import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.network.energy.EnergyStorage;
import com.refinedmods.refinedstorage.common.support.energy.ItemBlockEnergyStorage;
import com.refinedmods.refinedstorage.common.support.energy.ItemEnergyStorage;

import javax.annotation.Nullable;

import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.world.item.ItemStack;

public class EnergyStorageAdapter extends SnapshotParticipant<Long> implements team.reborn.energy.api.EnergyStorage {
    @Nullable
    private ContainerItemContext ctx;
    private final EnergyStorage energyStorage;

    public EnergyStorageAdapter(final EnergyStorage energyStorage, final ContainerItemContext ctx) {
        this.energyStorage = energyStorage;
        this.ctx = ctx;
    }

    public EnergyStorageAdapter(final EnergyStorage energyStorage) {
        this.energyStorage = energyStorage;
    }

    public EnergyStorage getEnergyStorage() {
        return energyStorage;
    }

    public void exchangeStack(final TransactionContext transaction) {
        if (ctx != null) {
            ItemStack itemStack = null;
            if (energyStorage instanceof ItemBlockEnergyStorage itemBlockEnergyStorage) {
                itemStack = itemBlockEnergyStorage.getStack();
            } else if (energyStorage instanceof ItemEnergyStorage itemEnergyStorage) {
                itemStack = itemEnergyStorage.getStack();
            }
            if (itemStack != null) {
                ctx.exchange(ItemVariant.of(itemStack), 1, transaction);
            }
        }
    }

    @Override
    public long insert(final long maxAmount, final TransactionContext transaction) {
        long inserted = energyStorage.receive(maxAmount, Action.SIMULATE);
        if (inserted > 0) {
            updateSnapshots(transaction);
        }
        inserted = energyStorage.receive(maxAmount, Action.EXECUTE);
        exchangeStack(transaction);
        return inserted;
    }

    @Override
    public long extract(final long maxAmount, final TransactionContext transaction) {
        long extracted = energyStorage.extract(maxAmount, Action.SIMULATE);
        if (extracted > 0) {
            updateSnapshots(transaction);
        }
        extracted = energyStorage.extract(maxAmount, Action.EXECUTE);
        exchangeStack(transaction);
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
    protected void readSnapshot(final Long snapshot) {
        energyStorage.extract(Long.MAX_VALUE, Action.EXECUTE);
        energyStorage.receive(snapshot, Action.EXECUTE);
    }
}
