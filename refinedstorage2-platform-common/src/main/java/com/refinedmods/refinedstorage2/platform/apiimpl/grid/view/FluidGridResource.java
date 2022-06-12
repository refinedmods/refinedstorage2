package com.refinedmods.refinedstorage2.platform.apiimpl.grid.view;

import com.refinedmods.refinedstorage2.api.grid.view.GridResource;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.platform.api.resource.FluidResource;

import java.util.Map;
import java.util.Set;

import net.minecraft.core.Registry;

public class FluidGridResource extends GridResource<FluidResource> {
    private final int id;

    public FluidGridResource(ResourceAmount<FluidResource> resourceAmount, String name, String modId, String modName, Set<String> tags, String tooltip) {
        super(resourceAmount, name, Map.of(
                GridResourceAttributeKeys.MOD_ID, Set.of(modId),
                GridResourceAttributeKeys.MOD_NAME, Set.of(modName),
                GridResourceAttributeKeys.TAGS, tags,
                GridResourceAttributeKeys.TOOLTIP, Set.of(tooltip)
        ));
        this.id = Registry.FLUID.getId(getResourceAmount().getResource().getFluid());
    }

    @Override
    public int getId() {
        return id;
    }
}
