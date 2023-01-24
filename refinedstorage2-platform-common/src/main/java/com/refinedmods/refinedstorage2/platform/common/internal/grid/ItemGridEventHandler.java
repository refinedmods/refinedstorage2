package com.refinedmods.refinedstorage2.platform.common.internal.grid;

import com.refinedmods.refinedstorage2.api.grid.service.GridExtractMode;
import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;

public interface ItemGridEventHandler {
    void onExtract(ItemResource itemResource, GridExtractMode mode, boolean cursor);

    void onScroll(ItemResource itemResource, GridScrollMode mode, int slotIndex);
}
