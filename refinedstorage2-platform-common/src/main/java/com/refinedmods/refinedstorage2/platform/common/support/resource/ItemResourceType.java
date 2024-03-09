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
import com.refinedmods.refinedstorage2.platform.common.grid.view.ItemGridResource;
import com.refinedmods.refinedstorage2.platform.common.support.TextureIds;
import com.refinedmods.refinedstorage2.platform.common.util.PacketUtil;

import java.util.Optional;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

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
    public ItemResource fromBuffer(final FriendlyByteBuf buf) {
        return PacketUtil.readItemResource(buf);
    }

    @Override
    public Optional<GridResource> toGridResource(final PlatformResourceKey resource, final long amount) {
        return Platform.INSTANCE.getItemGridResourceFactory().apply(new ResourceAmount(resource, amount));
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
            resource -> resource instanceof ItemResource itemResource ? itemResource.item().getMaxStackSize() : 0,
            1
        );
    }

    @Override
    public Optional<PlatformResourceKey> fromTag(final CompoundTag tag) {
        return ItemResource.fromTag(tag);
    }
}
