package com.refinedmods.refinedstorage2.platform.common.internal.storage.channel;

import com.refinedmods.refinedstorage2.api.resource.list.ResourceList;
import com.refinedmods.refinedstorage2.api.resource.list.ResourceListImpl;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;
import com.refinedmods.refinedstorage2.platform.api.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.api.resource.list.FuzzyResourceList;
import com.refinedmods.refinedstorage2.platform.api.resource.list.FuzzyResourceListImpl;
import com.refinedmods.refinedstorage2.platform.api.storage.channel.FuzzyStorageChannelImpl;

public final class StorageChannelTypes {
    public static final StorageChannelType<ItemResource> ITEM = new NamedStorageChannelType<>("ITEM", () -> {
        final ResourceList<ItemResource> list = new ResourceListImpl<>();
        final FuzzyResourceList<ItemResource> fuzzyList = new FuzzyResourceListImpl<>(list);
        return new FuzzyStorageChannelImpl<>(fuzzyList);
    });

    public static final StorageChannelType<FluidResource> FLUID = new NamedStorageChannelType<>("FLUID", () -> {
        final ResourceList<FluidResource> list = new ResourceListImpl<>();
        final FuzzyResourceList<FluidResource> fuzzyList = new FuzzyResourceListImpl<>(list);
        return new FuzzyStorageChannelImpl<>(fuzzyList);
    });

    private StorageChannelTypes() {
    }
}
