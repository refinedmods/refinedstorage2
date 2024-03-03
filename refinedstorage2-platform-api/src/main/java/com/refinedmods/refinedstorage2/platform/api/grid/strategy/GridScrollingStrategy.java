package com.refinedmods.refinedstorage2.platform.api.grid.strategy;

import com.refinedmods.refinedstorage2.api.resource.ResourceKey;
import com.refinedmods.refinedstorage2.platform.api.grid.GridScrollMode;
import com.refinedmods.refinedstorage2.platform.api.storage.channel.PlatformStorageChannelType;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.2.6")
public interface GridScrollingStrategy {
    boolean onScroll(
        PlatformStorageChannelType storageChannelType,
        ResourceKey resource,
        GridScrollMode scrollMode,
        int slotIndex
    );
}
