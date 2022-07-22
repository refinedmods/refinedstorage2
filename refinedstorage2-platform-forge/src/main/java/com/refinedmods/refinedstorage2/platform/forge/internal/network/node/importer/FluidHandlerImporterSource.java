package com.refinedmods.refinedstorage2.platform.forge.internal.network.node.importer;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.network.node.importer.ImporterSource;
import com.refinedmods.refinedstorage2.api.storage.Actor;
import com.refinedmods.refinedstorage2.platform.api.resource.FluidResource;

import java.util.Collections;
import java.util.Iterator;
import javax.annotation.Nullable;

import com.google.common.collect.AbstractIterator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

import static com.refinedmods.refinedstorage2.platform.forge.util.VariantUtil.ofFluidStack;
import static com.refinedmods.refinedstorage2.platform.forge.util.VariantUtil.toFluidStack;

public class FluidHandlerImporterSource implements ImporterSource<FluidResource> {
    private final Level level;
    private final BlockPos pos;
    private final Direction direction;

    public FluidHandlerImporterSource(final Level level, final BlockPos pos, final Direction direction) {
        this.level = level;
        this.pos = pos;
        this.direction = direction;
    }

    private LazyOptional<IFluidHandler> getFluidHandler() {
        final BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity == null) {
            return LazyOptional.empty();
        }
        return blockEntity.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, direction);
    }

    @Override
    public Iterator<FluidResource> getResources() {
        return getFluidHandler().map(fluidHandler -> (Iterator<FluidResource>) new AbstractIterator<FluidResource>() {
            private int index;

            @Nullable
            @Override
            protected FluidResource computeNext() {
                if (index > fluidHandler.getTanks()) {
                    return endOfData();
                }
                for (; index < fluidHandler.getTanks(); ++index) {
                    final FluidStack slot = fluidHandler.getFluidInTank(index);
                    if (!slot.isEmpty()) {
                        index++;
                        return ofFluidStack(slot);
                    }
                }
                return endOfData();
            }
        }).orElse(Collections.emptyListIterator());
    }

    @Override
    public long extract(final FluidResource resource, final long amount, final Action action, final Actor actor) {
        return getFluidHandler().map(fluidHandler -> {
            final FluidStack stack = toFluidStack(resource, amount);
            return (long) fluidHandler.drain(stack, toFluidAction(action)).getAmount();
        }).orElse(0L);
    }

    @Override
    public long insert(final FluidResource resource, final long amount, final Action action, final Actor actor) {
        return getFluidHandler().map(fluidHandler -> {
            final FluidStack stack = toFluidStack(resource, amount);
            return (long) fluidHandler.fill(stack, toFluidAction(action));
        }).orElse(0L);
    }

    private static IFluidHandler.FluidAction toFluidAction(final Action action) {
        return action == Action.SIMULATE ? IFluidHandler.FluidAction.SIMULATE : IFluidHandler.FluidAction.EXECUTE;
    }
}
