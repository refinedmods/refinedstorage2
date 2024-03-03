package com.refinedmods.refinedstorage2.platform.common.storage.channel;

import com.refinedmods.refinedstorage2.api.grid.operations.GridOperations;
import com.refinedmods.refinedstorage2.api.grid.operations.GridOperationsImpl;
import com.refinedmods.refinedstorage2.api.grid.view.GridResource;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.resource.ResourceKey;
import com.refinedmods.refinedstorage2.api.resource.list.ResourceList;
import com.refinedmods.refinedstorage2.api.resource.list.ResourceListImpl;
import com.refinedmods.refinedstorage2.api.storage.Actor;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannel;
import com.refinedmods.refinedstorage2.platform.api.storage.channel.AbstractPlatformStorageChannelType;
import com.refinedmods.refinedstorage2.platform.api.storage.channel.FuzzyStorageChannelImpl;
import com.refinedmods.refinedstorage2.platform.api.support.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.api.support.resource.list.FuzzyResourceList;
import com.refinedmods.refinedstorage2.platform.api.support.resource.list.FuzzyResourceListImpl;
import com.refinedmods.refinedstorage2.platform.common.Platform;
import com.refinedmods.refinedstorage2.platform.common.grid.view.ItemGridResource;
import com.refinedmods.refinedstorage2.platform.common.support.TextureIds;
import com.refinedmods.refinedstorage2.platform.common.util.PacketUtil;

import java.util.Optional;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

class ItemStorageChannelType extends AbstractPlatformStorageChannelType {
    ItemStorageChannelType() {
        super(
            "ITEM",
            () -> {
                final ResourceList list = new ResourceListImpl();
                final FuzzyResourceList fuzzyList = new FuzzyResourceListImpl(list);
                return new FuzzyStorageChannelImpl(fuzzyList);
            },
            createTranslation("misc", "storage_channel_type.item"),
            TextureIds.ICONS,
            0,
            128
        );
    }

    @Override
    public void toBuffer(final ResourceKey resource, final FriendlyByteBuf buf) {
        if (!(resource instanceof ItemResource itemResource)) {
            throw new UnsupportedOperationException();
        }
        PacketUtil.writeItemResource(buf, itemResource);
    }

    @Override
    public ItemResource fromBuffer(final FriendlyByteBuf buf) {
        return PacketUtil.readItemResource(buf);
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
    public long getInterfaceExportLimit(final ResourceKey resource) {
        if (!(resource instanceof ItemResource itemResource)) {
            throw new UnsupportedOperationException();
        }
        return itemResource.item().getMaxStackSize();
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
    public CompoundTag toTag(final ResourceKey resource) {
        if (!(resource instanceof ItemResource itemResource)) {
            throw new UnsupportedOperationException();
        }
        return ItemResource.toTag(itemResource);
    }

    @Override
    public Optional<ResourceKey> fromTag(final CompoundTag tag) {
        return ItemResource.fromTag(tag);
    }
}
