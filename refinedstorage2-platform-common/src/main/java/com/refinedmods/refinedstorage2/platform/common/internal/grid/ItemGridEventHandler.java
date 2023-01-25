package com.refinedmods.refinedstorage2.platform.common.internal.grid;

import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;

public interface ItemGridEventHandler {
    void onScroll(ItemResource itemResource, GridScrollMode mode, int slotIndex);
}
