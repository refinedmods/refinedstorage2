package com.refinedmods.refinedstorage2.platform.forge.internal.grid;

import com.refinedmods.refinedstorage2.api.grid.service.GridExtractMode;
import com.refinedmods.refinedstorage2.api.grid.service.GridInsertMode;
import com.refinedmods.refinedstorage2.platform.api.grid.GridScrollMode;
import com.refinedmods.refinedstorage2.platform.api.grid.ItemGridEventHandler;
import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;

public class ItemGridEventHandlerImpl implements ItemGridEventHandler {
    @Override
    public void onInsert(GridInsertMode insertMode) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void onTransfer(int slotIndex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void onExtract(ItemResource itemResource, GridExtractMode mode, boolean cursor) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void onScroll(ItemResource itemResource, GridScrollMode mode, int slot) {
        throw new UnsupportedOperationException();
    }
}
