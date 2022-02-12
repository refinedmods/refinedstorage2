package com.refinedmods.refinedstorage2.platform.common.internal.storage.channel;

import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;
import com.refinedmods.refinedstorage2.platform.api.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;

public final class StorageChannelTypes {
    public static final StorageChannelType<ItemResource> ITEM = new SimpleStorageChannelType<>("ITEM");
    public static final StorageChannelType<FluidResource> FLUID = new SimpleStorageChannelType<>("FLUID");

    private StorageChannelTypes() {
    }
}
