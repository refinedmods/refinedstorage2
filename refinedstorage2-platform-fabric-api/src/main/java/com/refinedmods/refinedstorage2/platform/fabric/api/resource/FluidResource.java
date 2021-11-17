package com.refinedmods.refinedstorage2.platform.fabric.api.resource;

import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;

import java.util.Objects;
import java.util.Optional;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.impl.transfer.fluid.FluidVariantImpl;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

public final class FluidResource {
    private static final String TAG_TAG = "tag";
    private static final String TAG_ID = "id";
    private static final String TAG_AMOUNT = "amount";
    private final Fluid fluid;
    private final CompoundTag tag;

    public FluidResource(Fluid fluid, CompoundTag tag) {
        this.fluid = fluid;
        this.tag = tag;
    }

    public static FluidResource ofFluidVariant(FluidVariant fluidVariant) {
        return new FluidResource(fluidVariant.getFluid(), fluidVariant.getNbt());
    }

    public static CompoundTag toTag(FluidResource fluidResource) {
        CompoundTag tag = new CompoundTag();
        if (fluidResource.getTag() != null) {
            tag.put(TAG_TAG, fluidResource.getTag());
        }
        tag.putString(TAG_ID, Registry.FLUID.getKey(fluidResource.getFluid()).toString());
        return tag;
    }

    public static CompoundTag toTagWithAmount(ResourceAmount<FluidResource> resourceAmount) {
        CompoundTag tag = toTag(resourceAmount.getResource());
        tag.putLong(TAG_AMOUNT, resourceAmount.getAmount());
        return tag;
    }

    public static Optional<FluidResource> fromTag(CompoundTag tag) {
        ResourceLocation id = new ResourceLocation(tag.getString(TAG_ID));
        Fluid fluid = Registry.FLUID.get(id);
        if (fluid == Fluids.EMPTY) {
            return Optional.empty();
        }
        CompoundTag itemTag = tag.contains(TAG_TAG) ? tag.getCompound(TAG_TAG) : null;
        return Optional.of(new FluidResource(fluid, itemTag));
    }

    public static Optional<ResourceAmount<FluidResource>> fromTagWithAmount(CompoundTag tag) {
        return fromTag(tag).map(fluidResource -> new ResourceAmount<>(fluidResource, tag.getLong(TAG_AMOUNT)));
    }

    public Fluid getFluid() {
        return fluid;
    }

    public CompoundTag getTag() {
        return tag;
    }

    public FluidVariant toFluidVariant() {
        return FluidVariantImpl.of(fluid, tag);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FluidResource that = (FluidResource) o;
        return fluid.equals(that.fluid) && Objects.equals(tag, that.tag);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fluid, tag);
    }

    @Override
    public String toString() {
        return "FluidResource{" +
                "fluid=" + fluid +
                ", tag=" + tag +
                '}';
    }
}
