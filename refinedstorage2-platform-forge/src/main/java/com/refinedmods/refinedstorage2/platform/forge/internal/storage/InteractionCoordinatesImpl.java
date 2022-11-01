package com.refinedmods.refinedstorage2.platform.forge.internal.storage;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;

public final class InteractionCoordinatesImpl implements InteractionCoordinates {
    private final Level level;
    private final BlockPos pos;
    private final Direction direction;

    public InteractionCoordinatesImpl(final Level level, final BlockPos pos, final Direction direction) {
        this.level = level;
        this.pos = pos;
        this.direction = direction;
    }

    @Override
    public LazyOptional<IItemHandler> getItemHandler() {
        final BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity == null) {
            return LazyOptional.empty();
        }
        return blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER, direction);
    }

    @Override
    public LazyOptional<IFluidHandler> getFluidHandler() {
        final BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity == null) {
            return LazyOptional.empty();
        }
        return blockEntity.getCapability(ForgeCapabilities.FLUID_HANDLER, direction);
    }
}
