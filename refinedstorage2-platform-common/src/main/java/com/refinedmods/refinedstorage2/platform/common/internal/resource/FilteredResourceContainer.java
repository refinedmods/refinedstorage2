package com.refinedmods.refinedstorage2.platform.common.internal.resource;

import com.refinedmods.refinedstorage2.platform.api.resource.ResourceInstance;
import com.refinedmods.refinedstorage2.platform.api.storage.channel.PlatformStorageChannelType;

import java.util.function.ToLongFunction;

public class FilteredResourceContainer<T> extends ResourceContainer {
    private final PlatformStorageChannelType<T> allowedType;

    public FilteredResourceContainer(final int size,
                                     final PlatformStorageChannelType<T> allowedType,
                                     final ResourceContainerType type) {
        super(size, type);
        this.allowedType = allowedType;
    }

    public FilteredResourceContainer(final int size,
                                     final PlatformStorageChannelType<T> allowedType,
                                     final ResourceContainerType type,
                                     final ToLongFunction<ResourceInstance<?>> maxAmountProvider) {
        super(size, type, maxAmountProvider);
        this.allowedType = allowedType;
    }

    @Override
    public <O> void set(final int index, final ResourceInstance<O> resourceInstance) {
        if (resourceInstance.getStorageChannelType() != allowedType) {
            return;
        }
        super.set(index, resourceInstance);
    }
}
