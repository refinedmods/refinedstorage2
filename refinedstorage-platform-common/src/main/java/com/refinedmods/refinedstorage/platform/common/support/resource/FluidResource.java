package com.refinedmods.refinedstorage.platform.common.support.resource;

import com.refinedmods.refinedstorage.api.core.CoreValidations;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.platform.api.support.resource.FuzzyModeNormalizer;
import com.refinedmods.refinedstorage.platform.api.support.resource.PlatformResourceKey;
import com.refinedmods.refinedstorage.platform.api.support.resource.ResourceType;

import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.level.material.Fluid;
import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.0")
public record FluidResource(Fluid fluid, DataComponentPatch components)
    implements PlatformResourceKey, FuzzyModeNormalizer {
    public FluidResource(final Fluid fluid) {
        this(fluid, DataComponentPatch.EMPTY);
    }

    public FluidResource(final Fluid fluid, final DataComponentPatch components) {
        this.fluid = CoreValidations.validateNotNull(fluid, "Fluid must not be null");
        this.components = CoreValidations.validateNotNull(components, "Components must not be null");
    }

    @Override
    public ResourceKey normalize() {
        return new FluidResource(fluid);
    }

    @Override
    public long getInterfaceExportLimit() {
        return ResourceTypes.FLUID.getInterfaceExportLimit();
    }

    @Override
    public ResourceType getResourceType() {
        return ResourceTypes.FLUID;
    }
}
