package com.refinedmods.refinedstorage2.platform.common.internal.storage.channel;

import com.refinedmods.refinedstorage2.platform.api.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.api.storage.channel.PlatformStorageChannelType;

public final class StorageChannelTypes {
    public static final PlatformStorageChannelType<ItemResource> ITEM = new ItemStorageChannelType();
    public static final PlatformStorageChannelType<FluidResource> FLUID = new FluidStorageChannelType();

    private StorageChannelTypes() {
    }
}
