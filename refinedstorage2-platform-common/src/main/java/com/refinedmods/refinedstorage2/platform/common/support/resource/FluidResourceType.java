package com.refinedmods.refinedstorage2.platform.common.support.resource;

import com.refinedmods.refinedstorage2.api.grid.operations.GridOperations;
import com.refinedmods.refinedstorage2.api.grid.operations.GridOperationsImpl;
import com.refinedmods.refinedstorage2.api.grid.view.GridResource;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.storage.Actor;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannel;
import com.refinedmods.refinedstorage2.platform.api.support.resource.AbstractResourceType;
import com.refinedmods.refinedstorage2.platform.api.support.resource.PlatformResourceKey;
import com.refinedmods.refinedstorage2.platform.common.Platform;
import com.refinedmods.refinedstorage2.platform.common.grid.view.FluidGridResource;
import com.refinedmods.refinedstorage2.platform.common.support.TextureIds;
import com.refinedmods.refinedstorage2.platform.common.util.PacketUtil;

import java.util.Optional;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

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
    public PlatformResourceKey fromBuffer(final FriendlyByteBuf buf) {
        return PacketUtil.readFluidResource(buf);
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
    public Optional<PlatformResourceKey> fromTag(final CompoundTag tag) {
        return FluidResource.fromTag(tag);
    }
}
