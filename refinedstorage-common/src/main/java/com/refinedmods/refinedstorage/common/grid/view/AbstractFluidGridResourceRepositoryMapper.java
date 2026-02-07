package com.refinedmods.refinedstorage.common.grid.view;

import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.resource.repository.ResourceRepositoryMapper;
import com.refinedmods.refinedstorage.common.api.grid.GridResourceAttributeKeys;
import com.refinedmods.refinedstorage.common.api.grid.view.GridResource;
import com.refinedmods.refinedstorage.common.api.grid.view.GridResourceAttributeKey;
import com.refinedmods.refinedstorage.common.support.resource.FluidResource;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.google.common.base.Suppliers;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.material.Fluid;

public abstract class AbstractFluidGridResourceRepositoryMapper implements ResourceRepositoryMapper<GridResource> {
    @Override
    public GridResource apply(final ResourceKey resource) {
        final FluidResource fluidResource = (FluidResource) resource;
        final String name = getName(fluidResource);
        final String modId = getModId(fluidResource);
        final String modName = getModName(modId);
        final Map<GridResourceAttributeKey, Supplier<Set<String>>> attributes = Map.of(
            GridResourceAttributeKeys.MOD_ID, Suppliers.ofInstance(Set.of(modId)),
            GridResourceAttributeKeys.MOD_NAME, Suppliers.ofInstance(Set.of(modName)),
            GridResourceAttributeKeys.TAGS, Suppliers.memoize(() -> getTags(fluidResource.fluid())),
            GridResourceAttributeKeys.TOOLTIP, Suppliers.memoize(() -> Set.of(getTooltip(fluidResource)))
        );
        return new FluidGridResource(
            fluidResource,
            name,
            k -> attributes.getOrDefault(k, Collections::emptySet).get()
        );
    }

    private Set<String> getTags(final Fluid fluid) {
        return BuiltInRegistries.FLUID.getResourceKey(fluid)
            .flatMap(BuiltInRegistries.FLUID::get)
            .stream()
            .flatMap(Holder::tags)
            .map(tagKey -> tagKey.location().getPath())
            .collect(Collectors.toSet());
    }

    private String getModId(final FluidResource fluid) {
        return BuiltInRegistries.FLUID.getKey(fluid.fluid()).getNamespace();
    }

    protected abstract String getModName(String modId);

    protected abstract String getName(FluidResource fluidResource);

    protected abstract String getTooltip(FluidResource resource);
}
