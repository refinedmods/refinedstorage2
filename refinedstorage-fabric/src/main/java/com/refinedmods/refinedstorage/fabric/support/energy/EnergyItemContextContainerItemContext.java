package com.refinedmods.refinedstorage.fabric.support.energy;

import com.refinedmods.refinedstorage.common.api.support.energy.EnergyItemContext;

import java.util.List;

import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

public class EnergyItemContextContainerItemContext implements ContainerItemContext {
    private final EnergyItemContextSingleStackStorage mainSlot;

    public EnergyItemContextContainerItemContext(final EnergyItemContext context) {
        this.mainSlot = new EnergyItemContextSingleStackStorage(context);
    }

    @Override
    public SingleSlotStorage<ItemVariant> getMainSlot() {
        return mainSlot;
    }

    @Override
    public long insertOverflow(final ItemVariant itemVariant, final long maxAmount,
                               final TransactionContext transactionContext) {
        return 0;
    }

    @Override
    public List<SingleSlotStorage<ItemVariant>> getAdditionalSlots() {
        return List.of();
    }
}
