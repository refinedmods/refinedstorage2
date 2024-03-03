package com.refinedmods.refinedstorage2.platform.common.grid.view;

import com.refinedmods.refinedstorage2.api.grid.view.GridResource;
import com.refinedmods.refinedstorage2.api.grid.view.GridResourceFactory;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.platform.api.storage.channel.PlatformStorageChannelType;
import com.refinedmods.refinedstorage2.platform.api.support.registry.PlatformRegistry;

import java.util.Optional;

public class CompositeGridResourceFactory implements GridResourceFactory {
    private final PlatformRegistry<PlatformStorageChannelType> storageChannelTypeRegistry;

    public CompositeGridResourceFactory(
        final PlatformRegistry<PlatformStorageChannelType> storageChannelTypeRegistry
    ) {
        this.storageChannelTypeRegistry = storageChannelTypeRegistry;
    }

    @Override
    public Optional<GridResource> apply(final ResourceAmount resourceAmount) {
        return storageChannelTypeRegistry.getAll()
            .stream()
            .flatMap(type -> type.toGridResource(resourceAmount).stream())
            .findFirst();
    }
}
