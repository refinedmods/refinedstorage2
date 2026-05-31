package com.refinedmods.refinedstorage.common.grid.view.pin;

import com.refinedmods.refinedstorage.common.api.grid.view.GridResource;
import com.refinedmods.refinedstorage.common.api.support.resource.PlatformResourceKey;

import org.jspecify.annotations.Nullable;

public record Pin(GridResource gridResource, boolean manual) {
    public boolean is(final GridResource otherGridResource) {
        return gridResource.is(otherGridResource);
    }

    @Nullable
    public PlatformResourceKey getAutocraftingResource() {
        return gridResource.getAutocraftingResource();
    }
}
