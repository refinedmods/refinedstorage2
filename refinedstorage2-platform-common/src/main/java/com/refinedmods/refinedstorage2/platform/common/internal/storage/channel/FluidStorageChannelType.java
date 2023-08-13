package com.refinedmods.refinedstorage2.platform.common.internal.storage.channel;

import com.refinedmods.refinedstorage2.api.grid.view.GridResource;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.resource.list.ResourceList;
import com.refinedmods.refinedstorage2.api.resource.list.ResourceListImpl;
import com.refinedmods.refinedstorage2.platform.api.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.api.resource.list.FuzzyResourceList;
import com.refinedmods.refinedstorage2.platform.api.resource.list.FuzzyResourceListImpl;
import com.refinedmods.refinedstorage2.platform.api.storage.channel.AbstractPlatformStorageChannelType;
import com.refinedmods.refinedstorage2.platform.api.storage.channel.FuzzyStorageChannelImpl;
import com.refinedmods.refinedstorage2.platform.common.Platform;
import com.refinedmods.refinedstorage2.platform.common.internal.grid.view.FluidGridResource;
import com.refinedmods.refinedstorage2.platform.common.screen.TextureIds;
import com.refinedmods.refinedstorage2.platform.common.util.PacketUtil;

import java.util.Optional;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

class FluidStorageChannelType extends AbstractPlatformStorageChannelType<FluidResource> {
    FluidStorageChannelType() {
        super(
            "FLUID",
            () -> {
                final ResourceList<FluidResource> list = new ResourceListImpl<>();
                final FuzzyResourceList<FluidResource> fuzzyList = new FuzzyResourceListImpl<>(list);
                return new FuzzyStorageChannelImpl<>(fuzzyList);
            },
            createTranslation("misc", "storage_channel_type.fluid"),
            TextureIds.ICONS,
            16,
            128
        );
    }

    @Override
    public void toBuffer(final FluidResource resource, final FriendlyByteBuf buf) {
        PacketUtil.writeFluidResource(buf, resource);
    }

    @Override
    public FluidResource fromBuffer(final FriendlyByteBuf buf) {
        return PacketUtil.readFluidResource(buf);
    }

    @Override
    public Optional<GridResource> toGridResource(final ResourceAmount<?> resourceAmount) {
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
    public long getInterfaceExportLimit(final FluidResource resource) {
        return Platform.INSTANCE.getBucketAmount() * 16;
    }

    @Override
    public CompoundTag toTag(final FluidResource resource) {
        return FluidResource.toTag(resource);
    }

    @Override
    public Optional<FluidResource> fromTag(final CompoundTag tag) {
        return FluidResource.fromTag(tag);
    }
}
