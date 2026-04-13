package com.refinedmods.refinedstorage.fabric.support.energy;

import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.network.energy.EnergyStorage;
import com.refinedmods.refinedstorage.common.support.energy.ItemBlockEnergyStorage;
import com.refinedmods.refinedstorage.common.support.energy.ItemEnergyStorage;

import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public class EnergyStorageAdapter extends SnapshotParticipant<Long> implements team.reborn.energy.api.EnergyStorage {
    private final EnergyStorage energyStorage;
    @Nullable
    private final ContainerItemContext containerItemContext;

    public EnergyStorageAdapter(final EnergyStorage energyStorage,
                                @Nullable final ContainerItemContext containerItemContext) {
        this.energyStorage = energyStorage;
        this.containerItemContext = containerItemContext;
    }

    public EnergyStorageAdapter(final EnergyStorage energyStorage) {
        this(energyStorage, null);
    }

    public EnergyStorage getEnergyStorage() {
        return energyStorage;
    }

    private void tryExchangeStack(final TransactionContext transaction) {
        if (containerItemContext == null) {
            return;
        }
        final ItemStack stack = extractStack();
        if (stack == null) {
            return;
        }
        containerItemContext.exchange(ItemVariant.of(stack), 1, transaction);
    }

    @Nullable
    private ItemStack extractStack() {
        if (energyStorage instanceof ItemBlockEnergyStorage itemBlockEnergyStorage) {
            return itemBlockEnergyStorage.getStack();
        } else if (energyStorage instanceof ItemEnergyStorage itemEnergyStorage) {
            return itemEnergyStorage.getStack();
        }
        return null;
    }

    @Override
    public long insert(final long maxAmount, final TransactionContext transaction) {
        final long insertedSimulated = energyStorage.receive(maxAmount, Action.SIMULATE);
        if (insertedSimulated > 0) {
            updateSnapshots(transaction);
        }
        final long inserted = energyStorage.receive(maxAmount, Action.EXECUTE);
        tryExchangeStack(transaction);
        return inserted;
    }

    @Override
    public long extract(final long maxAmount, final TransactionContext transaction) {
        final long extractedSimulated = energyStorage.extract(maxAmount, Action.SIMULATE);
        if (extractedSimulated > 0) {
            updateSnapshots(transaction);
        }
        final long extracted = energyStorage.extract(maxAmount, Action.EXECUTE);
        tryExchangeStack(transaction);
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
