package com.refinedmods.refinedstorage.common.grid.view.pin;

import com.refinedmods.refinedstorage.common.api.grid.view.GridResource;

public record Pin(GridResource gridResource, boolean manual) {
    public boolean is(final GridResource otherGridResource) {
        return gridResource.is(otherGridResource);
    }
}
