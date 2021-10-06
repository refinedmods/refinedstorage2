package com.refinedmods.refinedstorage2.platform.fabric.internal.grid.view;

import com.refinedmods.refinedstorage2.api.grid.view.GridResource;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.platform.fabric.api.resource.FluidResource;

import java.util.Set;

import net.minecraft.util.registry.Registry;

public class FluidGridResource extends GridResource<FluidResource> {
    private final int id;

    public FluidGridResource(ResourceAmount<FluidResource> resourceAmount, String name, String modId, String modName, Set<String> tags) {
        super(resourceAmount, name, modId, modName, tags);
        this.id = Registry.FLUID.getRawId(getResourceAmount().getResource().getFluid());
    }

    @Override
    public int getId() {
        return id;
    }
}
