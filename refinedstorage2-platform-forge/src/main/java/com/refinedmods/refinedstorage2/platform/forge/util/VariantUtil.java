package com.refinedmods.refinedstorage2.platform.forge.util;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.platform.api.resource.FluidResource;

import java.util.Objects;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class VariantUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(VariantUtil.class);

    private VariantUtil() {
    }

    public static boolean isSame(final FluidResource resource, final FluidStack stack) {
        return resource.fluid() == stack.getFluid() && Objects.equals(resource.tag(), stack.getTag());
    }

    public static FluidResource ofFluidStack(final FluidStack fluidStack) {
        return new FluidResource(fluidStack.getFluid(), fluidStack.getTag());
    }

    public static FluidStack toFluidStack(final FluidResource fluidResource, final long amount) {
        if (amount > Integer.MAX_VALUE) {
            LOGGER.warn("Truncating too large amount for {} to fit into FluidStack {}", fluidResource, amount);
        }
        return new FluidStack(fluidResource.fluid(), (int) amount, fluidResource.tag());
    }

    public static Action toAction(final IFluidHandler.FluidAction action) {
        return action == IFluidHandler.FluidAction.SIMULATE ? Action.SIMULATE : Action.EXECUTE;
    }

    public static IFluidHandler.FluidAction toFluidAction(final Action action) {
        return action == Action.SIMULATE ? IFluidHandler.FluidAction.SIMULATE : IFluidHandler.FluidAction.EXECUTE;
    }
}
