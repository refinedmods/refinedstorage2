package com.refinedmods.refinedstorage.platform.common.grid.strategy;

import com.refinedmods.refinedstorage.api.grid.operations.GridExtractMode;
import com.refinedmods.refinedstorage.platform.api.grid.strategy.GridExtractionStrategy;
import com.refinedmods.refinedstorage.platform.api.support.resource.PlatformResourceKey;
import com.refinedmods.refinedstorage.platform.common.support.packet.c2s.C2SPackets;

public class ClientGridExtractionStrategy implements GridExtractionStrategy {
    @Override
    public boolean onExtract(final PlatformResourceKey resource,
                             final GridExtractMode extractMode,
                             final boolean cursor) {
        C2SPackets.sendGridExtract(resource, extractMode, cursor);
        return true;
    }
}
