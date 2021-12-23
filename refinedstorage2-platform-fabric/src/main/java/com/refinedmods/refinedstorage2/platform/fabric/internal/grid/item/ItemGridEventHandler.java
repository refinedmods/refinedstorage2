package com.refinedmods.refinedstorage2.platform.fabric.internal.grid.item;

import com.refinedmods.refinedstorage2.api.grid.service.GridExtractMode;
import com.refinedmods.refinedstorage2.api.grid.service.GridInsertMode;
import com.refinedmods.refinedstorage2.platform.fabric.api.grid.GridScrollMode;
import com.refinedmods.refinedstorage2.platform.fabric.api.resource.ItemResource;

public interface ItemGridEventHandler {
    void onInsert(GridInsertMode insertMode);

    void onTransfer(int slotIndex);

    void onExtract(ItemResource itemResource, GridExtractMode mode, boolean cursor);

    void onScroll(ItemResource itemResource, GridScrollMode mode, int slot);
}
