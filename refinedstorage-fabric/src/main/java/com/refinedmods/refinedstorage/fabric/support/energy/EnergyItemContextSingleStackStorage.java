package com.refinedmods.refinedstorage.fabric.support.energy;

import com.refinedmods.refinedstorage.common.api.support.energy.EnergyItemContext;

import net.fabricmc.fabric.api.transfer.v1.item.base.SingleStackStorage;
import net.minecraft.world.item.ItemStack;

class EnergyItemContextSingleStackStorage extends SingleStackStorage {
    private final EnergyItemContext context;

    EnergyItemContextSingleStackStorage(final EnergyItemContext context) {
        this.context = context;
    }

    @Override
    protected ItemStack getStack() {
        return context.copyStack();
    }

    @Override
    protected void setStack(final ItemStack stack) {
        context.setStack(stack);
    }
}
