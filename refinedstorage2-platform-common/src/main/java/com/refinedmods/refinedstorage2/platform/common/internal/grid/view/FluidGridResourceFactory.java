package com.refinedmods.refinedstorage2.platform.common.internal.grid.view;

import com.refinedmods.refinedstorage2.api.grid.view.GridResource;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.platform.api.resource.FluidResource;

import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
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
        return FluidTags
                .getAllTags()
                .getMatchingTags(fluid)
                .stream()
                .map(ResourceLocation::getPath)
                .collect(Collectors.toSet());
    }

    private String getModId(FluidResource fluid) {
        return Registry.FLUID.getKey(fluid.getFluid()).getNamespace();
    }

    protected abstract String getModName(String modId);

    protected abstract String getName(FluidResource fluidResource);

    protected abstract String getTooltip(FluidResource resource);
}
