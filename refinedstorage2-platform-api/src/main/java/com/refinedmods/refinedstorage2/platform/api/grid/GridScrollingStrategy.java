package com.refinedmods.refinedstorage2.platform.api.grid;

import com.refinedmods.refinedstorage2.platform.api.storage.channel.PlatformStorageChannelType;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.2.6")
public interface GridScrollingStrategy {
    <T> boolean onScroll(
        PlatformStorageChannelType<T> storageChannelType,
        T resource,
        GridScrollMode scrollMode,
        int slotIndex
    );
}
