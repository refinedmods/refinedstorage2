package com.refinedmods.refinedstorage2.platform.common.storage.channel;

import com.refinedmods.refinedstorage2.platform.api.storage.channel.PlatformStorageChannelType;
import com.refinedmods.refinedstorage2.platform.api.support.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.api.support.resource.ItemResource;

public final class StorageChannelTypes {
    public static final PlatformStorageChannelType<ItemResource> ITEM = new ItemStorageChannelType();
    public static final PlatformStorageChannelType<FluidResource> FLUID = new FluidStorageChannelType();

    private StorageChannelTypes() {
    }
}
