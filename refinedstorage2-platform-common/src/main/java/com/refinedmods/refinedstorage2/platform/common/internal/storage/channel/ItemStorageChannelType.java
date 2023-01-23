package com.refinedmods.refinedstorage2.platform.common.internal.storage.channel;

import com.refinedmods.refinedstorage2.api.grid.view.AbstractGridResource;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.resource.list.ResourceList;
import com.refinedmods.refinedstorage2.api.resource.list.ResourceListImpl;
import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.api.resource.list.FuzzyResourceList;
import com.refinedmods.refinedstorage2.platform.api.resource.list.FuzzyResourceListImpl;
import com.refinedmods.refinedstorage2.platform.api.storage.channel.AbstractPlatformStorageChannelType;
import com.refinedmods.refinedstorage2.platform.api.storage.channel.FuzzyStorageChannelImpl;
import com.refinedmods.refinedstorage2.platform.common.Platform;
import com.refinedmods.refinedstorage2.platform.common.util.PacketUtil;

import java.util.Optional;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

class ItemStorageChannelType extends AbstractPlatformStorageChannelType<ItemResource> {
    ItemStorageChannelType() {
        super(
            "ITEM",
            () -> {
                final ResourceList<ItemResource> list = new ResourceListImpl<>();
                final FuzzyResourceList<ItemResource> fuzzyList = new FuzzyResourceListImpl<>(list);
                return new FuzzyStorageChannelImpl<>(fuzzyList);
            }
        );
    }

    @Override
    public void toBuffer(final ItemResource resource, final FriendlyByteBuf buf) {
        PacketUtil.writeItemResource(buf, resource);
    }

    @Override
    public ItemResource fromBuffer(final FriendlyByteBuf buf) {
        return PacketUtil.readItemResource(buf);
    }

    @Override
    public Optional<AbstractGridResource> toGridResource(final ResourceAmount<?> resourceAmount) {
        return Platform.INSTANCE.getItemGridResourceFactory().apply(resourceAmount);
    }

    @Override
    protected CompoundTag toTag(final ItemResource resource) {
        return ItemResource.toTag(resource);
    }

    @Override
    protected Optional<ItemResource> fromTag(final CompoundTag tag) {
        return ItemResource.fromTag(tag);
    }
}
