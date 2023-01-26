package com.refinedmods.refinedstorage2.platform.common.internal.grid.view;

import com.refinedmods.refinedstorage2.api.core.registry.OrderedRegistry;
import com.refinedmods.refinedstorage2.api.grid.view.GridResource;
import com.refinedmods.refinedstorage2.api.grid.view.GridResourceFactory;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.platform.api.storage.channel.PlatformStorageChannelType;

import java.util.Optional;

import net.minecraft.resources.ResourceLocation;

public class CompositeGridResourceFactory implements GridResourceFactory {
    private final OrderedRegistry<ResourceLocation, PlatformStorageChannelType<?>> storageChannelTypeRegistry;

    public CompositeGridResourceFactory(
        final OrderedRegistry<ResourceLocation, PlatformStorageChannelType<?>> storageChannelTypeRegistry
    ) {
        this.storageChannelTypeRegistry = storageChannelTypeRegistry;
    }

    @Override
    public Optional<GridResource> apply(final ResourceAmount<?> resourceAmount) {
        return storageChannelTypeRegistry.getAll()
            .stream()
            .flatMap(type -> type.toGridResource(resourceAmount).stream())
            .findFirst();
    }
}
