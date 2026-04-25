package com.refinedmods.refinedstorage.common.support.energy;

import com.refinedmods.refinedstorage.common.api.support.energy.EnergyItemContext;

import net.minecraft.world.item.ItemStack;

public class SimpleEnergyItemContext implements EnergyItemContext {
    private ItemStack stack;

    public SimpleEnergyItemContext(final ItemStack stack) {
        this.stack = stack;
    }

    @Override
    public ItemStack copyStack() {
        return stack.copy();
    }

    @Override
    public void setStack(final ItemStack stack) {
        this.stack = stack;
    }
}
