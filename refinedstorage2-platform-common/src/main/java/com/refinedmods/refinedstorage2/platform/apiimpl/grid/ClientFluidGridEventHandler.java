package com.refinedmods.refinedstorage2.platform.apiimpl.grid;

import com.refinedmods.refinedstorage2.api.grid.service.GridExtractMode;
import com.refinedmods.refinedstorage2.api.grid.service.GridInsertMode;
import com.refinedmods.refinedstorage2.platform.api.grid.FluidGridEventHandler;
import com.refinedmods.refinedstorage2.platform.api.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.common.Platform;

public class ClientFluidGridEventHandler implements FluidGridEventHandler {
    @Override
    public void onInsert(final GridInsertMode insertMode) {
        Platform.INSTANCE.getClientToServerCommunications().sendGridInsert(insertMode);
    }

    @Override
    public void onTransfer(final int slotIndex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void onExtract(final FluidResource fluidResource, final GridExtractMode mode, final boolean cursor) {
        Platform.INSTANCE.getClientToServerCommunications().sendGridFluidExtract(fluidResource, mode, cursor);
    }
}
