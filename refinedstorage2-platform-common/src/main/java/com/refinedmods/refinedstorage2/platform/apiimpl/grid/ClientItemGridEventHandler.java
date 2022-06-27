package com.refinedmods.refinedstorage2.platform.apiimpl.grid;

import com.refinedmods.refinedstorage2.api.grid.service.GridExtractMode;
import com.refinedmods.refinedstorage2.api.grid.service.GridInsertMode;
import com.refinedmods.refinedstorage2.platform.api.grid.GridScrollMode;
import com.refinedmods.refinedstorage2.platform.api.grid.ItemGridEventHandler;
import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.common.Platform;

public class ClientItemGridEventHandler implements ItemGridEventHandler {
    @Override
    public void onInsert(final GridInsertMode insertMode) {
        Platform.INSTANCE.getClientToServerCommunications().sendGridInsert(insertMode);
    }

    @Override
    public void onTransfer(final int slotIndex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void onExtract(final ItemResource itemResource, final GridExtractMode mode, final boolean cursor) {
        Platform.INSTANCE.getClientToServerCommunications().sendGridItemExtract(itemResource, mode, cursor);
    }

    @Override
    public void onScroll(final ItemResource itemResource, final GridScrollMode mode, final int slotIndex) {
        Platform.INSTANCE.getClientToServerCommunications().sendGridScroll(itemResource, mode, slotIndex);
    }
}
