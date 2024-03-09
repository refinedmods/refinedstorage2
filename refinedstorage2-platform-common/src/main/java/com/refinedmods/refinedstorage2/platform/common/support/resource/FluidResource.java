package com.refinedmods.refinedstorage2.platform.common.support.resource;

import com.refinedmods.refinedstorage2.api.core.CoreValidations;
import com.refinedmods.refinedstorage2.api.resource.ResourceKey;
import com.refinedmods.refinedstorage2.platform.api.support.resource.FuzzyModeNormalizer;
import com.refinedmods.refinedstorage2.platform.api.support.resource.PlatformResourceKey;
import com.refinedmods.refinedstorage2.platform.api.support.resource.ResourceType;

import java.util.Optional;
import javax.annotation.Nullable;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.0")
public record FluidResource(Fluid fluid, @Nullable CompoundTag tag)
    implements PlatformResourceKey, FuzzyModeNormalizer {
    private static final String TAG_TAG = "tag";
    private static final String TAG_ID = "id";

    public FluidResource(final Fluid fluid, @Nullable final CompoundTag tag) {
        this.fluid = CoreValidations.validateNotNull(fluid, "Fluid must not be null");
        this.tag = tag;
    }

    @Override
    public ResourceKey normalize() {
        return new FluidResource(fluid, null);
    }

    static Optional<PlatformResourceKey> fromTag(final CompoundTag tag) {
        final ResourceLocation id = new ResourceLocation(tag.getString(TAG_ID));
        final Fluid fluid = BuiltInRegistries.FLUID.get(id);
        if (fluid == Fluids.EMPTY) {
            return Optional.empty();
        }
        final CompoundTag itemTag = tag.contains(TAG_TAG) ? tag.getCompound(TAG_TAG) : null;
        return Optional.of(new FluidResource(fluid, itemTag));
    }

    @Override
    public CompoundTag toTag() {
        final CompoundTag nbt = new CompoundTag();
        if (tag != null) {
            nbt.put(TAG_TAG, tag);
        }
        nbt.putString(TAG_ID, BuiltInRegistries.FLUID.getKey(fluid).toString());
        return nbt;
    }

    @Override
    public void toBuffer(final FriendlyByteBuf buf) {
        buf.writeVarInt(BuiltInRegistries.FLUID.getId(fluid));
        buf.writeNbt(tag);
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
