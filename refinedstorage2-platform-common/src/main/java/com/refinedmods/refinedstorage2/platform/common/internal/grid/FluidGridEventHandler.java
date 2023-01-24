package com.refinedmods.refinedstorage2.platform.common.internal.grid;

import com.refinedmods.refinedstorage2.api.grid.service.GridExtractMode;
import com.refinedmods.refinedstorage2.platform.api.resource.FluidResource;

public interface FluidGridEventHandler {
    void onExtract(FluidResource fluidResource, GridExtractMode mode, boolean cursor);
}
