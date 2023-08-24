package com.refinedmods.refinedstorage2.platform.common.internal.grid;

import com.refinedmods.refinedstorage2.api.grid.operations.GridExtractMode;
import com.refinedmods.refinedstorage2.platform.api.grid.GridExtractionStrategy;
import com.refinedmods.refinedstorage2.platform.api.storage.channel.PlatformStorageChannelType;
import com.refinedmods.refinedstorage2.platform.common.Platform;

public class ClientGridExtractionStrategy implements GridExtractionStrategy {
    @Override
    public <T> boolean onExtract(final PlatformStorageChannelType<T> storageChannelType,
                                 final T resource,
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
