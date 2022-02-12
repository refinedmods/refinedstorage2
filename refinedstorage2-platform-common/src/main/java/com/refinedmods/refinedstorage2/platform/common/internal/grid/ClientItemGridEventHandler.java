package com.refinedmods.refinedstorage2.platform.common.internal.grid;

import com.refinedmods.refinedstorage2.api.grid.service.GridExtractMode;
import com.refinedmods.refinedstorage2.api.grid.service.GridInsertMode;
import com.refinedmods.refinedstorage2.platform.abstractions.Platform;
import com.refinedmods.refinedstorage2.platform.api.grid.GridScrollMode;
import com.refinedmods.refinedstorage2.platform.api.grid.ItemGridEventHandler;
import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;

public class ClientItemGridEventHandler implements ItemGridEventHandler {
    @Override
    public void onInsert(GridInsertMode insertMode) {
        Platform.INSTANCE.getClientToServerCommunications().sendGridInsert(insertMode);
    }

    @Override
    public void onTransfer(int slotIndex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void onExtract(ItemResource itemResource, GridExtractMode mode, boolean cursor) {
        Platform.INSTANCE.getClientToServerCommunications().sendGridItemExtract(itemResource, mode, cursor);
    }

    @Override
    public void onScroll(ItemResource itemResource, GridScrollMode mode, int slot) {
        Platform.INSTANCE.getClientToServerCommunications().sendGridScroll(itemResource, mode, slot);
    }
}
