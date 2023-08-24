package com.refinedmods.refinedstorage2.platform.forge.integration.energy;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;

public class ForgeEnergyCapabilityProvider implements ICapabilityProvider {
    private final LazyOptional<IEnergyStorage> capability;

    public ForgeEnergyCapabilityProvider(final ItemStack stack, final int energyCapacity) {
        this.capability = LazyOptional.of(() -> new ItemEnergyStorage(stack, energyCapacity));
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull final Capability<T> cap, @Nullable final Direction direction) {
        if (cap == ForgeCapabilities.ENERGY) {
            return capability.cast();
        }
        return LazyOptional.empty();
    }
}
