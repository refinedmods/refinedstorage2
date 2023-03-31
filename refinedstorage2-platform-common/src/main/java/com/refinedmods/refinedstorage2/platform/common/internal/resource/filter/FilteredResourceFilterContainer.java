package com.refinedmods.refinedstorage2.platform.common.internal.resource.filter;

import com.refinedmods.refinedstorage2.platform.api.resource.filter.FilteredResource;
import com.refinedmods.refinedstorage2.platform.api.storage.channel.PlatformStorageChannelType;

public class FilteredResourceFilterContainer<T> extends ResourceFilterContainer {
    private final PlatformStorageChannelType<T> allowedType;

    public FilteredResourceFilterContainer(final int size,
                                           final PlatformStorageChannelType<T> allowedType) {
        this(size, allowedType, -1);
    }


    public FilteredResourceFilterContainer(final int size,
                                           final PlatformStorageChannelType<T> allowedType,
                                           final long maxAmount) {
        super(size, maxAmount);
        this.allowedType = allowedType;
    }

    @Override
    public void set(final int index, final FilteredResource<?> resource) {
        if (resource.getStorageChannelType() != allowedType) {
            return;
        }
        super.set(index, resource);
    }
}
