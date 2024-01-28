package com.refinedmods.refinedstorage2.platform.forge.storage;

import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;

public final class CapabilityCacheImpl implements CapabilityCache {
    private final BlockCapabilityCache<IItemHandler, Direction> itemCapabilityCache;
    private final BlockCapabilityCache<IFluidHandler, Direction> fluidCapabilityCache;

    public CapabilityCacheImpl(final ServerLevel level, final BlockPos pos, final Direction direction) {
        this.itemCapabilityCache = BlockCapabilityCache.create(Capabilities.ItemHandler.BLOCK, level, pos, direction);
        this.fluidCapabilityCache = BlockCapabilityCache.create(Capabilities.FluidHandler.BLOCK, level, pos, direction);
    }

    @Override
    public Optional<IItemHandler> getItemHandler() {
        return Optional.ofNullable(itemCapabilityCache.getCapability());
    }

    @Override
    public Optional<IFluidHandler> getFluidHandler() {
        return Optional.ofNullable(fluidCapabilityCache.getCapability());
    }
}
