package com.refinedmods.refinedstorage2.platform.common.internal.resource.filter.fluid;

import com.refinedmods.refinedstorage2.platform.api.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.api.resource.filter.FilteredResource;
import com.refinedmods.refinedstorage2.platform.api.storage.channel.PlatformStorageChannelType;
import com.refinedmods.refinedstorage2.platform.common.Platform;
import com.refinedmods.refinedstorage2.platform.common.internal.storage.channel.StorageChannelTypes;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;

public record FluidFilteredResource(FluidResource value, long amount) implements FilteredResource<FluidResource> {
    @Override
    public void render(final PoseStack poseStack, final int x, final int y, final int z) {
        Platform.INSTANCE.getFluidRenderer().render(poseStack, x, y, z, value);
    }

    @Override
    public FluidResource getValue() {
        return value;
    }

    @Override
    public FilteredResource<FluidResource> withAmount(final long newAmount) {
        return new FluidFilteredResource(value, newAmount);
    }

    @Override
    public long getAmount() {
        return amount;
    }

    @Override
    public long getMaxAmount() {
        return Long.MAX_VALUE;
    }

    @Override
    public String getDisplayedAmount() {
        return Platform.INSTANCE.getBucketAmountFormatter().formatWithUnits(amount);
    }

    @Override
    public List<Component> getTooltip() {
        return Platform.INSTANCE.getFluidRenderer().getTooltip(value);
    }

    @Override
    public PlatformStorageChannelType<FluidResource> getStorageChannelType() {
        return StorageChannelTypes.FLUID;
    }
}
