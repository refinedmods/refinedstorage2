package com.refinedmods.refinedstorage2.platform.forge.internal.storage;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.storage.Actor;
import com.refinedmods.refinedstorage2.api.storage.InsertableStorage;
import com.refinedmods.refinedstorage2.platform.api.network.node.exporter.AmountOverride;
import com.refinedmods.refinedstorage2.platform.api.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.forge.util.VariantUtil;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import static com.refinedmods.refinedstorage2.platform.forge.util.VariantUtil.toFluidAction;
import static com.refinedmods.refinedstorage2.platform.forge.util.VariantUtil.toFluidStack;

public class FluidHandlerInsertableStorage implements InsertableStorage<FluidResource> {
    private final InteractionCoordinates interactionCoordinates;
    private final AmountOverride amountOverride;

    public FluidHandlerInsertableStorage(final InteractionCoordinates interactionCoordinates,
                                         final AmountOverride amountOverride) {
        this.interactionCoordinates = interactionCoordinates;
        this.amountOverride = amountOverride;
    }

    @Override
    public long insert(final FluidResource resource, final long amount, final Action action, final Actor actor) {
        return interactionCoordinates
            .getFluidHandler()
            .map(fluidHandler -> {
                final long correctedAmount = amountOverride.overrideAmount(
                    resource,
                    amount,
                    () -> getCurrentAmount(fluidHandler, resource)
                );
                if (correctedAmount == 0) {
                    return 0L;
                }
                return doInsert(resource, amount, action, fluidHandler);
            })
            .orElse(0L);
    }

    private long getCurrentAmount(final IFluidHandler fluidHandler, final FluidResource fluidResource) {
        long amount = 0;
        for (int i = 0; i < fluidHandler.getTanks(); ++i) {
            final FluidStack tank = fluidHandler.getFluidInTank(i);
            if (VariantUtil.isSame(fluidResource, tank)) {
                amount += tank.getAmount();
            }
        }
        return amount;
    }

    private long doInsert(final FluidResource resource,
                          final long amount,
                          final Action action,
                          final IFluidHandler fluidHandler) {
        final FluidStack stack = toFluidStack(resource, amount);
        return fluidHandler.fill(stack, toFluidAction(action));
    }
}
