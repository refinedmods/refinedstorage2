package com.refinedmods.refinedstorage2.platform.fabric.internal.grid.item;

import com.refinedmods.refinedstorage2.api.grid.service.GridExtractMode;
import com.refinedmods.refinedstorage2.api.grid.service.GridInsertMode;
import com.refinedmods.refinedstorage2.platform.abstractions.PlatformAbstractions;
import com.refinedmods.refinedstorage2.platform.fabric.api.grid.GridScrollMode;
import com.refinedmods.refinedstorage2.platform.fabric.api.resource.ItemResource;

public class ClientItemGridEventHandler implements ItemGridEventHandler {
    @Override
    public void onInsert(GridInsertMode insertMode) {
        PlatformAbstractions.INSTANCE.getClientToServerCommunications().sendGridInsert(insertMode);
    }

    @Override
    public void onTransfer(int slotIndex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void onExtract(ItemResource itemResource, GridExtractMode mode, boolean cursor) {
        PlatformAbstractions.INSTANCE.getClientToServerCommunications().sendGridItemExtract(itemResource, mode, cursor);
    }

    @Override
    public void onScroll(ItemResource itemResource, GridScrollMode mode, int slot) {
        PlatformAbstractions.INSTANCE.getClientToServerCommunications().sendGridScroll(itemResource, mode, slot);
    }
}
