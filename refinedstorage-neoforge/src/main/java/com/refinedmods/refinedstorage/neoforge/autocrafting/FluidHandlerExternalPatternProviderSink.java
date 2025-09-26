package com.refinedmods.refinedstorage.neoforge.autocrafting;

import com.refinedmods.refinedstorage.api.autocrafting.task.ExternalPatternSink;
import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.common.api.autocrafting.PlatformPatternProviderExternalPatternSink;
import com.refinedmods.refinedstorage.common.support.resource.FluidResource;
import com.refinedmods.refinedstorage.neoforge.storage.CapabilityCache;

import java.util.Collection;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.refinedmods.refinedstorage.neoforge.support.resource.VariantUtil.toFluidStack;

class FluidHandlerExternalPatternProviderSink implements PlatformPatternProviderExternalPatternSink {
    private static final Logger LOGGER = LoggerFactory.getLogger(FluidHandlerExternalPatternProviderSink.class);

    private final CapabilityCache capabilityCache;

    FluidHandlerExternalPatternProviderSink(final CapabilityCache capabilityCache) {
        this.capabilityCache = capabilityCache;
    }

    @Override
    public ExternalPatternSink.Result accept(final Collection<ResourceAmount> resources, final Action action) {
        return capabilityCache.getFluidHandler()
            .map(handler -> accept(resources, action, handler))
            .orElse(ExternalPatternSink.Result.SKIPPED);
    }

    private ExternalPatternSink.Result accept(final Collection<ResourceAmount> resources,
                                              final Action action,
                                              final IFluidHandler handler) {
        for (final ResourceAmount resource : resources) {
            if (resource.resource() instanceof FluidResource fluidResource
                && !accept(action, handler, resource.amount(), fluidResource)) {
                return ExternalPatternSink.Result.REJECTED;
            }
        }
        return ExternalPatternSink.Result.ACCEPTED;
    }

    private boolean accept(final Action action,
                           final IFluidHandler handler,
                           final long amount,
                           final FluidResource fluidResource) {
        final FluidStack fluidStack = toFluidStack(fluidResource, amount);
        final int filled = handler.fill(
            fluidStack,
            action == Action.SIMULATE ? IFluidHandler.FluidAction.SIMULATE : IFluidHandler.FluidAction.EXECUTE
        );
        if (filled != fluidStack.getAmount()) {
            if (action == Action.EXECUTE) {
                LOGGER.warn(
                    "{} unexpectedly didn't accept all of {}, the remainder has been voided",
                    handler,
                    fluidStack
                );
            }
            return false;
        }
        return true;
    }

    @Override
    public boolean isEmpty() {
        return capabilityCache.getFluidHandler().map(handler -> {
            for (int i = 0; i < handler.getTanks(); i++) {
                if (!handler.getFluidInTank(i).isEmpty()) {
                    return false;
                }
            }
            return true;
        }).orElse(true);
    }

    @Override
    public boolean applies(final ResourceKey resource) {
        return resource instanceof FluidResource;
    }
}
