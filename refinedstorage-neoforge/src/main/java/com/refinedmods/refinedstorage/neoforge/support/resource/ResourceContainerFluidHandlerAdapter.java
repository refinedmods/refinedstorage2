package com.refinedmods.refinedstorage.neoforge.support.resource;

import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.common.api.support.resource.ResourceContainer;
import com.refinedmods.refinedstorage.common.support.resource.FluidResource;
import com.refinedmods.refinedstorage.common.support.resource.ResourceTypes;

import javax.annotation.Nullable;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import static com.refinedmods.refinedstorage.neoforge.support.resource.VariantUtil.ofFluidStack;
import static com.refinedmods.refinedstorage.neoforge.support.resource.VariantUtil.toAction;
import static com.refinedmods.refinedstorage.neoforge.support.resource.VariantUtil.toFluidStack;

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
        if (resourceAmount == null || !(resourceAmount.resource() instanceof FluidResource fluidResource)) {
            return FluidStack.EMPTY;
        }
        return toFluidStack(fluidResource, resourceAmount.amount());
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
        if (resource.isEmpty()) {
            return 0;
        }
        return (int) container.insert(ofFluidStack(resource), resource.getAmount(), toAction(action));
    }

    @Override
    public FluidStack drain(final FluidStack fluidStack, final FluidAction action) {
        if (fluidStack.isEmpty()) {
            return FluidStack.EMPTY;
        }
        final FluidResource resource = ofFluidStack(fluidStack);
        final long extracted = container.extract(resource, fluidStack.getAmount(), toAction(action));
        if (extracted == 0) {
            return FluidStack.EMPTY;
        }
        return toFluidStack(resource, extracted);
    }

    @Override
    public FluidStack drain(final int maxDrain, final FluidAction action) {
        if (maxDrain <= 0) {
            return FluidStack.EMPTY;
        }
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
