package com.refinedmods.refinedstorage2.platform.common.storage.channel;

import com.refinedmods.refinedstorage2.platform.api.storage.channel.PlatformStorageChannelType;

public final class StorageChannelTypes {
    public static final PlatformStorageChannelType ITEM = new ItemStorageChannelType();
    public static final PlatformStorageChannelType FLUID = new FluidStorageChannelType();

    private StorageChannelTypes() {
    }
}
