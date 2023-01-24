package com.refinedmods.refinedstorage2.platform.common.internal.grid;

import com.refinedmods.refinedstorage2.api.grid.service.GridExtractMode;
import com.refinedmods.refinedstorage2.platform.api.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.common.Platform;

public class ClientFluidGridEventHandler implements FluidGridEventHandler {
    @Override
    public void onExtract(final FluidResource fluidResource, final GridExtractMode mode, final boolean cursor) {
        Platform.INSTANCE.getClientToServerCommunications().sendGridFluidExtract(fluidResource, mode, cursor);
    }
}
