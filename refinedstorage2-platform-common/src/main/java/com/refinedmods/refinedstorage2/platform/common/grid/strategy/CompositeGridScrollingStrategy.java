package com.refinedmods.refinedstorage2.platform.common.grid.strategy;

import com.refinedmods.refinedstorage2.platform.api.grid.GridScrollMode;
import com.refinedmods.refinedstorage2.platform.api.grid.strategy.GridScrollingStrategy;
import com.refinedmods.refinedstorage2.platform.api.support.resource.PlatformResourceKey;

import java.util.Collections;
import java.util.List;

public class CompositeGridScrollingStrategy implements GridScrollingStrategy {
    private final List<GridScrollingStrategy> strategies;

    public CompositeGridScrollingStrategy(final List<GridScrollingStrategy> strategies) {
        this.strategies = Collections.unmodifiableList(strategies);
    }

    @Override
    public boolean onScroll(final PlatformResourceKey resource, final GridScrollMode scrollMode, final int slotIndex) {
        for (final GridScrollingStrategy strategy : strategies) {
            if (strategy.onScroll(resource, scrollMode, slotIndex)) {
                return true;
            }
        }
        return false;
    }
}
