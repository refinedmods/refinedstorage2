package com.refinedmods.refinedstorage.platform.api.grid.strategy;

import com.refinedmods.refinedstorage.api.grid.operations.GridExtractMode;
import com.refinedmods.refinedstorage.platform.api.support.resource.PlatformResourceKey;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.2.6")
public interface GridExtractionStrategy {
    boolean onExtract(PlatformResourceKey resource, GridExtractMode extractMode, boolean cursor);
}
