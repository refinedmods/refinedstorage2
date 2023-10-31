package com.refinedmods.refinedstorage2.platform.common.grid.strategy;

import com.refinedmods.refinedstorage2.platform.api.grid.GridScrollMode;
import com.refinedmods.refinedstorage2.platform.api.grid.strategy.GridScrollingStrategy;
import com.refinedmods.refinedstorage2.platform.api.storage.channel.PlatformStorageChannelType;
import com.refinedmods.refinedstorage2.platform.common.Platform;

public class ClientGridScrollingStrategy implements GridScrollingStrategy {
    @Override
    public <T> boolean onScroll(final PlatformStorageChannelType<T> storageChannelType,
                                final T resource,
                                final GridScrollMode scrollMode,
                                final int slotIndex) {
        Platform.INSTANCE.getClientToServerCommunications().sendGridScroll(
            storageChannelType,
            resource,
            scrollMode,
            slotIndex
        );
        return true;
    }
}
