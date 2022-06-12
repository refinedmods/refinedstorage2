package com.refinedmods.refinedstorage2.platform.apiimpl.grid.view;

import com.refinedmods.refinedstorage2.api.grid.view.GridResource;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.platform.api.resource.FluidResource;

import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.world.level.material.Fluid;

public abstract class FluidGridResourceFactory implements Function<ResourceAmount<FluidResource>, GridResource<FluidResource>> {
    @Override
    public GridResource<FluidResource> apply(ResourceAmount<FluidResource> resourceAmount) {
        String name = getName(resourceAmount.getResource());
        String modId = getModId(resourceAmount.getResource());
        String modName = getModName(modId);

        Set<String> tags = getTags(resourceAmount.getResource().getFluid());
        String tooltip = getTooltip(resourceAmount.getResource());

        return new FluidGridResource(resourceAmount, name, modId, modName, tags, tooltip);
    }

    private Set<String> getTags(Fluid fluid) {
        return Registry.FLUID.getResourceKey(fluid)
                .flatMap(Registry.FLUID::getHolder)
                .stream()
                .flatMap(Holder::tags)
                .map(tagKey -> tagKey.location().getPath())
                .collect(Collectors.toSet());
    }

    private String getModId(FluidResource fluid) {
        return Registry.FLUID.getKey(fluid.getFluid()).getNamespace();
    }

    protected abstract String getModName(String modId);

    protected abstract String getName(FluidResource fluidResource);

    protected abstract String getTooltip(FluidResource resource);
}
