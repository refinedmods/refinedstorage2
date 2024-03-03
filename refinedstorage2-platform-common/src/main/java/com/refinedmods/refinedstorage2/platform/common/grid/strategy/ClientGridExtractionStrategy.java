package com.refinedmods.refinedstorage2.platform.common.grid.strategy;

import com.refinedmods.refinedstorage2.api.grid.operations.GridExtractMode;
import com.refinedmods.refinedstorage2.api.resource.ResourceKey;
import com.refinedmods.refinedstorage2.platform.api.grid.strategy.GridExtractionStrategy;
import com.refinedmods.refinedstorage2.platform.api.storage.channel.PlatformStorageChannelType;
import com.refinedmods.refinedstorage2.platform.common.Platform;

public class ClientGridExtractionStrategy implements GridExtractionStrategy {
    @Override
    public boolean onExtract(final PlatformStorageChannelType storageChannelType,
                             final ResourceKey resource,
                             final GridExtractMode extractMode,
                             final boolean cursor) {
        Platform.INSTANCE.getClientToServerCommunications().sendGridExtract(
            storageChannelType,
            resource,
            extractMode,
            cursor
        );
        return true;
    }
}
