package com.refinedmods.refinedstorage2.platform.common.internal.storage.channel;

import com.refinedmods.refinedstorage2.api.grid.view.GridResource;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.resource.list.ResourceList;
import com.refinedmods.refinedstorage2.api.resource.list.ResourceListImpl;
import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.api.resource.list.FuzzyResourceList;
import com.refinedmods.refinedstorage2.platform.api.resource.list.FuzzyResourceListImpl;
import com.refinedmods.refinedstorage2.platform.api.storage.channel.AbstractPlatformStorageChannelType;
import com.refinedmods.refinedstorage2.platform.api.storage.channel.FuzzyStorageChannelImpl;
import com.refinedmods.refinedstorage2.platform.common.Platform;
import com.refinedmods.refinedstorage2.platform.common.internal.grid.view.ItemGridResource;
import com.refinedmods.refinedstorage2.platform.common.screen.widget.AbstractSideButtonWidget;
import com.refinedmods.refinedstorage2.platform.common.util.PacketUtil;

import java.util.Optional;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

class ItemStorageChannelType extends AbstractPlatformStorageChannelType<ItemResource> {
    ItemStorageChannelType() {
        super(
            "ITEM",
            () -> {
                final ResourceList<ItemResource> list = new ResourceListImpl<>();
                final FuzzyResourceList<ItemResource> fuzzyList = new FuzzyResourceListImpl<>(list);
                return new FuzzyStorageChannelImpl<>(fuzzyList);
            },
            createTranslation("misc", "storage_channel_type.item")
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
    public Optional<GridResource> toGridResource(final ResourceAmount<?> resourceAmount) {
        return Platform.INSTANCE.getItemGridResourceFactory().apply(resourceAmount);
    }

    @Override
    public boolean isGridResourceBelonging(final GridResource gridResource) {
        return gridResource instanceof ItemGridResource;
    }

    @Override
    public ResourceLocation getTextureIdentifier() {
        return AbstractSideButtonWidget.DEFAULT_TEXTURE;
    }

    @Override
    public int getXTexture() {
        return 0;
    }

    @Override
    public int getYTexture() {
        return 128;
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
