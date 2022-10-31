package com.refinedmods.refinedstorage2.platform.forge.internal.network.node.importer;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.network.node.importer.ImporterSource;
import com.refinedmods.refinedstorage2.api.storage.Actor;
import com.refinedmods.refinedstorage2.platform.api.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.forge.internal.storage.FluidHandlerInsertableStorage;
import com.refinedmods.refinedstorage2.platform.forge.internal.storage.InteractionCoordinates;

import java.util.Collections;
import java.util.Iterator;
import javax.annotation.Nullable;

import com.google.common.collect.AbstractIterator;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import static com.refinedmods.refinedstorage2.platform.forge.util.VariantUtil.ofFluidStack;
import static com.refinedmods.refinedstorage2.platform.forge.util.VariantUtil.toFluidAction;
import static com.refinedmods.refinedstorage2.platform.forge.util.VariantUtil.toFluidStack;

public class FluidHandlerImporterSource implements ImporterSource<FluidResource> {
    private final InteractionCoordinates interactionCoordinates;
    private final FluidHandlerInsertableStorage insertTarget;

    public FluidHandlerImporterSource(final InteractionCoordinates interactionCoordinates) {
        this.interactionCoordinates = interactionCoordinates;
        this.insertTarget = new FluidHandlerInsertableStorage(interactionCoordinates);
    }

    @Override
    public Iterator<FluidResource> getResources() {
        final LazyOptional<IFluidHandler> fh = interactionCoordinates.getFluidHandler();
        return fh.map(fluidHandler -> (Iterator<FluidResource>) new AbstractIterator<FluidResource>() {
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
        return interactionCoordinates.getFluidHandler().map(fluidHandler -> {
            final FluidStack stack = toFluidStack(resource, amount);
            return (long) fluidHandler.drain(stack, toFluidAction(action)).getAmount();
        }).orElse(0L);
    }

    @Override
    public long insert(final FluidResource resource, final long amount, final Action action, final Actor actor) {
        return insertTarget.insert(resource, amount, action, actor);
    }
}
