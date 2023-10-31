package com.refinedmods.refinedstorage2.platform.common.grid;

import com.refinedmods.refinedstorage2.api.grid.view.GridResource;
import com.refinedmods.refinedstorage2.api.grid.view.GridSortingType;
import com.refinedmods.refinedstorage2.api.grid.view.GridView;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedResource;
import com.refinedmods.refinedstorage2.platform.api.grid.view.PlatformGridResource;

import java.util.Comparator;
import java.util.function.Function;

public enum GridSortingTypes implements GridSortingType {
    QUANTITY(view -> Comparator.comparingLong(GridResource::getAmount)),
    NAME(view -> Comparator.comparing(GridResource::getName)),
    ID(view -> (a, b) -> {
        if (a instanceof PlatformGridResource aa && b instanceof PlatformGridResource bb) {
            return Integer.compare(aa.getRegistryId(), bb.getRegistryId());
        }
        return 0;
    }),
    LAST_MODIFIED(view -> (a, b) -> {
        final long lastModifiedA = a.getTrackedResource(view).map(TrackedResource::getTime).orElse(0L);
        final long lastModifiedB = b.getTrackedResource(view).map(TrackedResource::getTime).orElse(0L);
        return Long.compare(lastModifiedA, lastModifiedB);
    });

    private final Function<GridView, Comparator<GridResource>> comparator;

    GridSortingTypes(final Function<GridView, Comparator<GridResource>> comparator) {
        this.comparator = comparator;
    }

    @Override
    public Comparator<GridResource> apply(final GridView view) {
        return comparator.apply(view);
    }
}
