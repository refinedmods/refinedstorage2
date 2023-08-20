package com.refinedmods.refinedstorage2.platform.forge.integration.energy;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.energy.EnergyStorage;

public class ItemEnergyStorage extends EnergyStorage {
    private static final String TAG_ENERGY = "energy";

    private final ItemStack stack;

    public ItemEnergyStorage(final ItemStack stack, final int capacity) {
        super(capacity, capacity, capacity);
        this.stack = stack;
        if (stack.getTag() != null) {
            this.energy = stack.getTag().getInt(TAG_ENERGY);
        }
    }

    @Override
    public int receiveEnergy(final int maxReceive, final boolean simulate) {
        final int received = super.receiveEnergy(maxReceive, simulate);
        if (received > 0 && !simulate) {
            stack.getOrCreateTag().putInt(TAG_ENERGY, getEnergyStored());
        }
        return received;
    }

    @Override
    public int extractEnergy(final int maxExtract, final boolean simulate) {
        final int extracted = super.extractEnergy(maxExtract, simulate);
        if (extracted > 0 && !simulate) {
            stack.getOrCreateTag().putInt(TAG_ENERGY, getEnergyStored());
        }
        return extracted;
    }
}

