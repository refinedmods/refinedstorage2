package com.refinedmods.refinedstorage2.platform.api.resource;

import com.refinedmods.refinedstorage2.api.core.CoreValidations;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;

import java.util.Optional;
import javax.annotation.Nullable;

import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

public record FluidResource(Fluid fluid, @Nullable CompoundTag tag) implements FuzzyModeNormalizer<FluidResource> {
    private static final String TAG_TAG = "tag";
    private static final String TAG_ID = "id";
    private static final String TAG_AMOUNT = "amount";

    public FluidResource(final Fluid fluid, @Nullable final CompoundTag tag) {
        this.fluid = CoreValidations.validateNotNull(fluid, "Fluid must not be null");
        this.tag = tag;
    }

    @Override
    public FluidResource normalize() {
        return new FluidResource(fluid, null);
    }

    public static CompoundTag toTag(final FluidResource fluidResource) {
        final CompoundTag tag = new CompoundTag();
        if (fluidResource.tag() != null) {
            tag.put(TAG_TAG, fluidResource.tag());
        }
        tag.putString(TAG_ID, Registry.FLUID.getKey(fluidResource.fluid()).toString());
        return tag;
    }

    public static CompoundTag toTagWithAmount(final ResourceAmount<FluidResource> resourceAmount) {
        final CompoundTag tag = toTag(resourceAmount.getResource());
        tag.putLong(TAG_AMOUNT, resourceAmount.getAmount());
        return tag;
    }

    public static Optional<FluidResource> fromTag(final CompoundTag tag) {
        final ResourceLocation id = new ResourceLocation(tag.getString(TAG_ID));
        final Fluid fluid = Registry.FLUID.get(id);
        if (fluid == Fluids.EMPTY) {
            return Optional.empty();
        }
        final CompoundTag itemTag = tag.contains(TAG_TAG) ? tag.getCompound(TAG_TAG) : null;
        return Optional.of(new FluidResource(fluid, itemTag));
    }

    public static Optional<ResourceAmount<FluidResource>> fromTagWithAmount(final CompoundTag tag) {
        return fromTag(tag).map(fluidResource -> new ResourceAmount<>(fluidResource, tag.getLong(TAG_AMOUNT)));
    }
}
