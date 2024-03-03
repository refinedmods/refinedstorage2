package com.refinedmods.refinedstorage2.platform.common.grid.strategy;

import com.refinedmods.refinedstorage2.api.grid.operations.GridExtractMode;
import com.refinedmods.refinedstorage2.api.resource.ResourceKey;
import com.refinedmods.refinedstorage2.platform.api.grid.strategy.GridExtractionStrategy;
import com.refinedmods.refinedstorage2.platform.api.storage.channel.PlatformStorageChannelType;

import java.util.Collections;
import java.util.List;

public class CompositeGridExtractionStrategy implements GridExtractionStrategy {
    private final List<GridExtractionStrategy> strategies;

    public CompositeGridExtractionStrategy(final List<GridExtractionStrategy> strategies) {
        this.strategies = Collections.unmodifiableList(strategies);
    }

    @Override
    public boolean onExtract(final PlatformStorageChannelType storageChannelType,
                             final ResourceKey resource,
                             final GridExtractMode extractMode,
                             final boolean cursor) {
        for (final GridExtractionStrategy strategy : strategies) {
            if (strategy.onExtract(storageChannelType, resource, extractMode, cursor)) {
                return true;
            }
        }
        return false;
    }
}
