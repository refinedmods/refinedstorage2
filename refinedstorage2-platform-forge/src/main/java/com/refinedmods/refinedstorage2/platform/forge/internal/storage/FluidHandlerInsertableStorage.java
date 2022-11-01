package com.refinedmods.refinedstorage2.platform.forge.internal.storage;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.storage.Actor;
import com.refinedmods.refinedstorage2.api.storage.InsertableStorage;
import com.refinedmods.refinedstorage2.platform.api.resource.FluidResource;

import net.minecraftforge.fluids.FluidStack;

import static com.refinedmods.refinedstorage2.platform.forge.util.VariantUtil.toFluidAction;
import static com.refinedmods.refinedstorage2.platform.forge.util.VariantUtil.toFluidStack;

public class FluidHandlerInsertableStorage implements InsertableStorage<FluidResource> {
    private final InteractionCoordinates interactionCoordinates;

    public FluidHandlerInsertableStorage(final InteractionCoordinates interactionCoordinates) {
        this.interactionCoordinates = interactionCoordinates;
    }

    @Override
    public long insert(final FluidResource resource, final long amount, final Action action, final Actor actor) {
        return interactionCoordinates.getFluidHandler().map(fluidHandler -> {
            final FluidStack stack = toFluidStack(resource, amount);
            return (long) fluidHandler.fill(stack, toFluidAction(action));
        }).orElse(0L);
    }
}
