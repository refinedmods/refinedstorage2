package com.refinedmods.refinedstorage.platform.common.grid.strategy;

import com.refinedmods.refinedstorage.platform.api.grid.GridScrollMode;
import com.refinedmods.refinedstorage.platform.api.grid.strategy.GridScrollingStrategy;
import com.refinedmods.refinedstorage.platform.api.support.resource.PlatformResourceKey;
import com.refinedmods.refinedstorage.platform.common.support.packet.c2s.C2SPackets;

public class ClientGridScrollingStrategy implements GridScrollingStrategy {
    @Override
    public boolean onScroll(final PlatformResourceKey resource, final GridScrollMode scrollMode, final int slotIndex) {
        C2SPackets.sendGridScroll(resource, scrollMode, slotIndex);
        return true;
    }
}
