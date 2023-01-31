package com.refinedmods.refinedstorage2.platform.common.internal.resource.filter;

import com.refinedmods.refinedstorage2.platform.api.resource.filter.FilteredResource;
import com.refinedmods.refinedstorage2.platform.api.storage.channel.PlatformStorageChannelType;

public class FilteredResourceFilterContainer extends ResourceFilterContainer {
    private final PlatformStorageChannelType<?> allowedType;

    public FilteredResourceFilterContainer(final int size,
                                           final PlatformStorageChannelType<?> allowedType) {
        this(size, () -> {
        }, allowedType, -1);
    }

    public FilteredResourceFilterContainer(final int size,
                                           final PlatformStorageChannelType<?> allowedType,
                                           final long maxAmount) {
        this(size, () -> {
        }, allowedType, maxAmount);
    }

    public FilteredResourceFilterContainer(final int size,
                                           final Runnable listener,
                                           final PlatformStorageChannelType<?> allowedType) {
        this(size, listener, allowedType, -1);
    }

    public FilteredResourceFilterContainer(final int size,
                                           final Runnable listener,
                                           final PlatformStorageChannelType<?> allowedType,
                                           final long maxAmount) {
        super(size, listener, maxAmount);
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
