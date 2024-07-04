package com.refinedmods.refinedstorage.platform.common.support.resource;

import com.refinedmods.refinedstorage.api.grid.operations.GridOperations;
import com.refinedmods.refinedstorage.api.grid.operations.GridOperationsImpl;
import com.refinedmods.refinedstorage.api.grid.view.GridResource;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.api.storage.channel.StorageChannel;
import com.refinedmods.refinedstorage.platform.api.support.resource.AbstractResourceType;
import com.refinedmods.refinedstorage.platform.api.support.resource.PlatformResourceKey;
import com.refinedmods.refinedstorage.platform.common.Platform;
import com.refinedmods.refinedstorage.platform.common.grid.view.FluidGridResource;
import com.refinedmods.refinedstorage.platform.common.support.TextureIds;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import static com.refinedmods.refinedstorage.platform.common.util.IdentifierUtil.createTranslation;

class FluidResourceType extends AbstractResourceType {
    FluidResourceType() {
        super(
            "FLUID",
            createTranslation("misc", "resource_type.fluid"),
            TextureIds.ICONS,
            16,
            128
        );
    }

    @Override
    public Optional<GridResource> toGridResource(final ResourceAmount resourceAmount) {
        return Platform.INSTANCE.getFluidGridResourceFactory().apply(resourceAmount);
    }

    @Override
    public boolean isGridResourceBelonging(final GridResource gridResource) {
        return gridResource instanceof FluidGridResource;
    }

    @Override
    public long normalizeAmount(final double amount) {
        return (long) (amount * Platform.INSTANCE.getBucketAmount());
    }

    @Override
    public double getDisplayAmount(final long amount) {
        return amount / (double) Platform.INSTANCE.getBucketAmount();
    }

    @Override
    public long getInterfaceExportLimit() {
        return Platform.INSTANCE.getBucketAmount() * 16;
    }

    @Override
    public GridOperations createGridOperations(final StorageChannel storageChannel, final Actor actor) {
        return new GridOperationsImpl(
            storageChannel,
            actor,
            fluidResource -> Long.MAX_VALUE,
            Platform.INSTANCE.getBucketAmount()
        );
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public MapCodec<PlatformResourceKey> getMapCodec() {
        return (MapCodec) ResourceCodecs.FLUID_MAP_CODEC;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Codec<PlatformResourceKey> getCodec() {
        return (Codec) ResourceCodecs.FLUID_CODEC;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public StreamCodec<RegistryFriendlyByteBuf, PlatformResourceKey> getStreamCodec() {
        return (StreamCodec) ResourceCodecs.FLUID_STREAM_CODEC;
    }
}
