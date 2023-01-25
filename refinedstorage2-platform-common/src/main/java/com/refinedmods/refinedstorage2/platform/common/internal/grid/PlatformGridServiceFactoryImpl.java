package com.refinedmods.refinedstorage2.platform.common.internal.grid;

import com.refinedmods.refinedstorage2.api.grid.service.GridService;
import com.refinedmods.refinedstorage2.api.grid.service.GridServiceFactory;
import com.refinedmods.refinedstorage2.api.storage.Actor;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;
import com.refinedmods.refinedstorage2.platform.api.grid.PlatformGridServiceFactory;
import com.refinedmods.refinedstorage2.platform.api.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.common.Platform;
import com.refinedmods.refinedstorage2.platform.common.internal.storage.channel.StorageChannelTypes;

import java.util.function.Function;

public class PlatformGridServiceFactoryImpl implements PlatformGridServiceFactory {
    private final GridServiceFactory delegate;

    public PlatformGridServiceFactoryImpl(final GridServiceFactory delegate) {
        this.delegate = delegate;
    }

    @Override
    public <T> GridService<T> create(final StorageChannelType<T> storageChannelType,
                                     final Actor actor,
                                     final Function<T, Long> maxAmountProvider,
                                     final long singleAmount) {
        return delegate.create(storageChannelType, actor, maxAmountProvider, singleAmount);
    }

    @Override
    public GridService<ItemResource> createForItem(final Actor actor) {
        return create(
            StorageChannelTypes.ITEM,
            actor,
            itemResource -> (long) itemResource.item().getMaxStackSize(),
            1
        );
    }

    @Override
    public GridService<FluidResource> createForFluid(final Actor actor) {
        return create(
            StorageChannelTypes.FLUID,
            actor,
            fluidResource -> Long.MAX_VALUE,
            Platform.INSTANCE.getBucketAmount()
        );
    }
}
