package com.refinedmods.refinedstorage2.platform.fabric.internal.grid.fluid;

import com.refinedmods.refinedstorage2.api.grid.service.GridExtractMode;
import com.refinedmods.refinedstorage2.api.grid.service.GridInsertMode;
import com.refinedmods.refinedstorage2.platform.fabric.api.resource.FluidResource;

public interface FluidGridEventHandler {
    void onInsert(GridInsertMode insertMode);

    void onExtract(FluidResource fluidResource, GridExtractMode mode, boolean cursor);
}
