package com.refinedmods.refinedstorage2.platform.common.internal.resource.filter.fluid;

import com.refinedmods.refinedstorage2.platform.api.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.api.resource.filter.FilteredResource;
import com.refinedmods.refinedstorage2.platform.api.storage.channel.PlatformStorageChannelType;
import com.refinedmods.refinedstorage2.platform.common.Platform;
import com.refinedmods.refinedstorage2.platform.common.internal.storage.channel.StorageChannelTypes;

import java.util.List;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public record FluidFilteredResource(FluidResource value, long amount) implements FilteredResource<FluidResource> {
    @Override
    public void render(final GuiGraphics graphics, final int x, final int y) {
        Platform.INSTANCE.getFluidRenderer().render(graphics.pose(), x, y, value);
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
