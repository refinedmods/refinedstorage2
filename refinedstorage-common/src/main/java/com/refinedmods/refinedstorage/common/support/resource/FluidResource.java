package com.refinedmods.refinedstorage.common.support.resource;

import com.refinedmods.refinedstorage.api.core.CoreValidations;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.api.support.resource.FuzzyModeNormalizer;
import com.refinedmods.refinedstorage.common.api.support.resource.PlatformResourceKey;
import com.refinedmods.refinedstorage.common.api.support.resource.ResourceTag;
import com.refinedmods.refinedstorage.common.api.support.resource.ResourceType;

import java.util.List;
import java.util.Objects;

import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.material.Fluid;
import org.apiguardian.api.API;

@API(status = API.Status.INTERNAL)
public final class FluidResource
    implements PlatformResourceKey, FuzzyModeNormalizer {
    private final Fluid fluid;
    private final DataComponentPatch components;
    private int hash = 0;

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
    public long getProcessingPatternLimit() {
        return Platform.INSTANCE.getBucketAmount() * 16;
    }

    @Override
    public List<ResourceTag> getTags() {
        return BuiltInRegistries.FLUID.wrapAsHolder(fluid)
            .tags()
            .flatMap(tagKey -> BuiltInRegistries.FLUID.getTag(tagKey).stream())
            .map(tag -> new ResourceTag(
                tag.key(),
                tag.stream().map(holder -> (PlatformResourceKey) new FluidResource(holder.value())).toList()
            )).toList();
    }

    @Override
    public ResourceType getResourceType() {
        return ResourceTypes.FLUID;
    }

    public Fluid fluid() {
        return fluid;
    }

    public DataComponentPatch components() {
        return components;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof FluidResource that)) {
            return false;
        }
        return fluid.equals(that.fluid) && components.equals(that.components);
    }

    @Override
    public int hashCode() {
        if (hash == 0) {
            hash = Objects.hash(fluid, components);
        }
        return hash;
    }

    @Override
    public String toString() {
        return "FluidResource["
            + "fluid=" + fluid + ", "
            + "components=" + components + ']';
    }
}
