package com.refinedmods.refinedstorage2.platform.fabric.internal.grid.view;

import com.refinedmods.refinedstorage2.api.grid.view.GridResource;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.platform.fabric.api.resource.FluidResource;

import java.util.Map;
import java.util.Set;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.core.Registry;

public class FluidGridResource extends GridResource<FluidResource> {
    private final int id;
    private final FluidVariant fluidVariant;

    public FluidGridResource(ResourceAmount<FluidResource> resourceAmount, String name, String modId, String modName, Set<String> tags, String tooltip) {
        super(resourceAmount, name, Map.of(
                GridResourceAttributeKeys.MOD_ID, Set.of(modId),
                GridResourceAttributeKeys.MOD_NAME, Set.of(modName),
                GridResourceAttributeKeys.TAGS, tags,
                GridResourceAttributeKeys.TOOLTIP, Set.of(tooltip)
        ));
        this.id = Registry.FLUID.getId(getResourceAmount().getResource().getFluid());
        this.fluidVariant = resourceAmount.getResource().toFluidVariant();
    }

    public FluidVariant getFluidVariant() {
        return fluidVariant;
    }

    @Override
    public int getId() {
        return id;
    }
}
