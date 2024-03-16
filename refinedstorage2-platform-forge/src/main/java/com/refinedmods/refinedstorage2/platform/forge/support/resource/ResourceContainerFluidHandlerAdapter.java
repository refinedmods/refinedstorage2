package com.refinedmods.refinedstorage2.platform.forge.support.resource;

import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.resource.ResourceKey;
import com.refinedmods.refinedstorage2.platform.api.support.resource.ResourceContainer;
import com.refinedmods.refinedstorage2.platform.common.support.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.common.support.resource.ResourceTypes;

import javax.annotation.Nullable;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import static com.refinedmods.refinedstorage2.platform.forge.support.resource.VariantUtil.ofFluidStack;
import static com.refinedmods.refinedstorage2.platform.forge.support.resource.VariantUtil.toAction;
import static com.refinedmods.refinedstorage2.platform.forge.support.resource.VariantUtil.toFluidStack;

public class ResourceContainerFluidHandlerAdapter implements IFluidHandler {
    private final ResourceContainer container;

    public ResourceContainerFluidHandlerAdapter(final ResourceContainer container) {
        this.container = container;
    }

    @Override
    public int getTanks() {
        return container.size();
    }

    @Override
    public FluidStack getFluidInTank(final int tank) {
        final ResourceAmount resourceAmount = container.get(tank);
        if (resourceAmount == null || !(resourceAmount.getResource() instanceof FluidResource fluidResource)) {
            return FluidStack.EMPTY;
        }
        return toFluidStack(fluidResource, resourceAmount.getAmount());
    }

    @Override
    public int getTankCapacity(final int tank) {
        final ResourceKey resource = container.getResource(tank);
        if (resource == null || resource instanceof FluidResource) {
            return (int) ResourceTypes.FLUID.getInterfaceExportLimit();
        }
        return 0;
    }

    @Override
    public boolean isFluidValid(final int tank, final FluidStack stack) {
        return true;
    }

    @Override
    public int fill(final FluidStack resource, final FluidAction action) {
        return (int) container.insert(ofFluidStack(resource), resource.getAmount(), toAction(action));
    }

    @Override
    public FluidStack drain(final FluidStack fluidStack, final FluidAction action) {
        final FluidResource resource = ofFluidStack(fluidStack);
        final long extracted = container.extract(resource, fluidStack.getAmount(), toAction(action));
        if (extracted == 0) {
            return FluidStack.EMPTY;
        }
        return toFluidStack(resource, extracted);
    }

    @Override
    public FluidStack drain(final int maxDrain, final FluidAction action) {
        final FluidResource resource = findExtractableFluidResource();
        if (resource == null) {
            return FluidStack.EMPTY;
        }
        final long extracted = container.extract(resource, maxDrain, toAction(action));
        if (extracted == 0) {
            return FluidStack.EMPTY;
        }
        return toFluidStack(resource, extracted);
    }

    @Nullable
    private FluidResource findExtractableFluidResource() {
        for (int i = 0; i < container.size(); ++i) {
            final ResourceKey resource = container.getResource(i);
            if (!(resource instanceof FluidResource fluidResource)) {
                continue;
            }
            return fluidResource;
        }
        return null;
    }
}
