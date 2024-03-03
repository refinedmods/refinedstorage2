package com.refinedmods.refinedstorage2.platform.forge.storage;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.resource.ResourceKey;
import com.refinedmods.refinedstorage2.api.storage.Actor;
import com.refinedmods.refinedstorage2.api.storage.InsertableStorage;
import com.refinedmods.refinedstorage2.platform.api.exporter.AmountOverride;
import com.refinedmods.refinedstorage2.platform.api.support.resource.FluidResource;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import static com.refinedmods.refinedstorage2.platform.forge.support.resource.VariantUtil.toFluidAction;
import static com.refinedmods.refinedstorage2.platform.forge.support.resource.VariantUtil.toFluidStack;

public class FluidHandlerInsertableStorage implements InsertableStorage {
    private final CapabilityCache capabilityCache;
    private final AmountOverride amountOverride;

    public FluidHandlerInsertableStorage(final CapabilityCache capabilityCache,
                                         final AmountOverride amountOverride) {
        this.capabilityCache = capabilityCache;
        this.amountOverride = amountOverride;
    }

    @Override
    public long insert(final ResourceKey resource, final long amount, final Action action, final Actor actor) {
        if (!(resource instanceof FluidResource fluidResource)) {
            return 0;
        }
        return capabilityCache.getFluidHandler().map(fluidHandler -> {
            final long correctedAmount = amountOverride.overrideAmount(
                fluidResource,
                amount,
                () -> ForgeHandlerUtil.getCurrentAmount(fluidHandler, fluidResource)
            );
            if (correctedAmount == 0) {
                return 0L;
            }
            return doInsert(fluidResource, correctedAmount, action, fluidHandler);
        }).orElse(0L);
    }

    private long doInsert(final FluidResource resource,
                          final long amount,
                          final Action action,
                          final IFluidHandler fluidHandler) {
        final FluidStack stack = toFluidStack(resource, amount);
        return fluidHandler.fill(stack, toFluidAction(action));
    }
}
