package com.refinedmods.refinedstorage2.platform.common.internal.grid.view;

import com.refinedmods.refinedstorage2.api.grid.view.AbstractGridResource;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.platform.api.resource.FluidResource;

import java.util.Map;
import java.util.Set;

import net.minecraft.core.Registry;

public class FluidGridResource extends AbstractGridResource<FluidResource> {
    private final int id;

    @SuppressWarnings("deprecation") // forge deprecates Registry access
    public FluidGridResource(final ResourceAmount<FluidResource> resourceAmount,
                             final String name,
                             final String modId,
                             final String modName,
                             final Set<String> tags,
                             final String tooltip) {
        super(resourceAmount, name, Map.of(
            GridResourceAttributeKeys.MOD_ID, Set.of(modId),
            GridResourceAttributeKeys.MOD_NAME, Set.of(modName),
            GridResourceAttributeKeys.TAGS, tags,
            GridResourceAttributeKeys.TOOLTIP, Set.of(tooltip)
        ));
        this.id = Registry.FLUID.getId(getResourceAmount().getResource().fluid());
    }

    @Override
    public int getId() {
        return id;
    }
}
