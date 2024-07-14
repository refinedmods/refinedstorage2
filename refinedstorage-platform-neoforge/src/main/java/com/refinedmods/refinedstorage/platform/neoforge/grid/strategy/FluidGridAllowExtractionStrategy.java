package com.refinedmods.refinedstorage.platform.neoforge.grid.strategy;

import com.refinedmods.refinedstorage.platform.api.grid.strategy.GridAllowExtractionStrategy;
import com.refinedmods.refinedstorage.platform.api.grid.view.PlatformGridResource;
import com.refinedmods.refinedstorage.platform.common.support.resource.FluidResource;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;

import static com.refinedmods.refinedstorage.platform.neoforge.support.resource.VariantUtil.toFluidStack;

public class FluidGridAllowExtractionStrategy implements GridAllowExtractionStrategy {
    @Override
    public boolean allowExtraction(final PlatformGridResource resource, final ItemStack carriedStack) {
        if (resource.getUnderlyingResource() instanceof FluidResource fluidResource) {
            final IFluidHandlerItem cap = carriedStack.getCapability(Capabilities.FluidHandler.ITEM);

            return carriedStack.getCount() == 1
                && cap != null
                && cap.fill(toFluidStack(fluidResource, resource.getAmount()), IFluidHandler.FluidAction.SIMULATE) > 0;
        }
        return false;
    }
}
