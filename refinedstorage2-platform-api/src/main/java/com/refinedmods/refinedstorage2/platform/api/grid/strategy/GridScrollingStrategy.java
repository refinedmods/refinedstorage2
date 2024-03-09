package com.refinedmods.refinedstorage2.platform.api.grid.strategy;

import com.refinedmods.refinedstorage2.platform.api.grid.GridScrollMode;
import com.refinedmods.refinedstorage2.platform.api.support.resource.PlatformResourceKey;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.2.6")
public interface GridScrollingStrategy {
    boolean onScroll(PlatformResourceKey resource, GridScrollMode scrollMode, int slotIndex);
}
