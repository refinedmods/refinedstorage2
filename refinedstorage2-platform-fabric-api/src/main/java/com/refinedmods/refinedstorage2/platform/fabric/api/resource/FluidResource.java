package com.refinedmods.refinedstorage2.platform.fabric.api.resource;

import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;

import java.util.Objects;
import java.util.Optional;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.impl.transfer.fluid.FluidVariantImpl;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public final class FluidResource {
    private final Fluid fluid;
    private final NbtCompound tag;

    public static FluidResource ofFluidVariant(FluidVariant fluidVariant) {
        return new FluidResource(fluidVariant.getFluid(), fluidVariant.getNbt());
    }

    public FluidResource(Fluid fluid, NbtCompound tag) {
        this.fluid = fluid;
        this.tag = tag;
    }

    public Fluid getFluid() {
        return fluid;
    }

    public NbtCompound getTag() {
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

    private static final String TAG_TAG = "tag";
    private static final String TAG_ID = "id";
    private static final String TAG_AMOUNT = "amount";

    public static NbtCompound toTag(FluidResource fluidResource) {
        NbtCompound tag = new NbtCompound();
        if (fluidResource.getTag() != null) {
            tag.put(TAG_TAG, fluidResource.getTag());
        }
        tag.putString(TAG_ID, Registry.FLUID.getId(fluidResource.getFluid()).toString());
        return tag;
    }

    public static NbtCompound toTagWithAmount(ResourceAmount<FluidResource> resourceAmount) {
        NbtCompound tag = toTag(resourceAmount.getResource());
        tag.putLong(TAG_AMOUNT, resourceAmount.getAmount());
        return tag;
    }

    public static Optional<FluidResource> fromTag(NbtCompound tag) {
        Identifier id = new Identifier(tag.getString(TAG_ID));
        Fluid fluid = Registry.FLUID.get(id);
        if (fluid == Fluids.EMPTY) {
            return Optional.empty();
        }
        NbtCompound itemTag = tag.contains(TAG_TAG) ? tag.getCompound(TAG_TAG) : null;
        return Optional.of(new FluidResource(fluid, itemTag));
    }

    public static Optional<ResourceAmount<FluidResource>> fromTagWithAmount(NbtCompound tag) {
        return fromTag(tag).map(fluidResource -> new ResourceAmount<>(fluidResource, tag.getLong(TAG_AMOUNT)));
    }
}
