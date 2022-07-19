package com.refinedmods.refinedstorage2.platform.common.internal.grid.view;

import com.refinedmods.refinedstorage2.api.grid.view.AbstractGridResource;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.platform.api.resource.FluidResource;

import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.world.level.material.Fluid;

public abstract class AbstractFluidGridResourceFactory
    implements Function<ResourceAmount<FluidResource>, AbstractGridResource<FluidResource>> {
    @Override
    public AbstractGridResource<FluidResource> apply(final ResourceAmount<FluidResource> resourceAmount) {
        final String name = getName(resourceAmount.getResource());
        final String modId = getModId(resourceAmount.getResource());
        final String modName = getModName(modId);

        final Set<String> tags = getTags(resourceAmount.getResource().fluid());
        final String tooltip = getTooltip(resourceAmount.getResource());

        return new FluidGridResource(resourceAmount, name, modId, modName, tags, tooltip);
    }

    private Set<String> getTags(final Fluid fluid) {
        return Registry.FLUID.getResourceKey(fluid)
            .flatMap(Registry.FLUID::getHolder)
            .stream()
            .flatMap(Holder::tags)
            .map(tagKey -> tagKey.location().getPath())
            .collect(Collectors.toSet());
    }

    private String getModId(final FluidResource fluid) {
        return Registry.FLUID.getKey(fluid.fluid()).getNamespace();
    }

    protected abstract String getModName(String modId);

    protected abstract String getName(FluidResource fluidResource);

    protected abstract String getTooltip(FluidResource resource);
}
