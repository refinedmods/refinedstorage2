package com.refinedmods.refinedstorage2.platform.common.internal.grid;

import com.refinedmods.refinedstorage2.api.grid.service.GridExtractMode;
import com.refinedmods.refinedstorage2.api.grid.service.GridInsertMode;
import com.refinedmods.refinedstorage2.platform.abstractions.PlatformAbstractions;
import com.refinedmods.refinedstorage2.platform.api.grid.FluidGridEventHandler;
import com.refinedmods.refinedstorage2.platform.api.resource.FluidResource;

public class ClientFluidGridEventHandler implements FluidGridEventHandler {
    @Override
    public void onInsert(GridInsertMode insertMode) {
        PlatformAbstractions.INSTANCE.getClientToServerCommunications().sendGridInsert(insertMode);
    }

    @Override
    public void onTransfer(int slotIndex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void onExtract(FluidResource fluidResource, GridExtractMode mode, boolean cursor) {
        PlatformAbstractions.INSTANCE.getClientToServerCommunications().sendGridFluidExtract(fluidResource, mode, cursor);
    }
}
