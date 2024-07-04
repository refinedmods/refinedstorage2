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
import com.refinedmods.refinedstorage.platform.common.grid.view.ItemGridResource;
import com.refinedmods.refinedstorage.platform.common.support.TextureIds;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import static com.refinedmods.refinedstorage.platform.common.util.IdentifierUtil.createTranslation;

class ItemResourceType extends AbstractResourceType {
    ItemResourceType() {
        super(
            "ITEM",
            createTranslation("misc", "resource_type.item"),
            TextureIds.ICONS,
            0,
            128
        );
    }

    @Override
    public Optional<GridResource> toGridResource(final ResourceAmount resourceAmount) {
        return Platform.INSTANCE.getItemGridResourceFactory().apply(resourceAmount);
    }

    @Override
    public boolean isGridResourceBelonging(final GridResource gridResource) {
        return gridResource instanceof ItemGridResource;
    }

    @Override
    public long normalizeAmount(final double amount) {
        return (long) amount;
    }

    @Override
    public double getDisplayAmount(final long amount) {
        return amount;
    }

    @Override
    public long getInterfaceExportLimit() {
        return 64;
    }

    @Override
    public GridOperations createGridOperations(final StorageChannel storageChannel, final Actor actor) {
        return new GridOperationsImpl(
            storageChannel,
            actor,
            resource -> resource instanceof ItemResource itemResource
                ? itemResource.item().getDefaultMaxStackSize()
                : 0,
            1
        );
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public MapCodec<PlatformResourceKey> getMapCodec() {
        return (MapCodec) ResourceCodecs.ITEM_MAP_CODEC;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Codec<PlatformResourceKey> getCodec() {
        return (Codec) ResourceCodecs.ITEM_CODEC;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public StreamCodec<RegistryFriendlyByteBuf, PlatformResourceKey> getStreamCodec() {
        return (StreamCodec) ResourceCodecs.ITEM_STREAM_CODEC;
    }
}
