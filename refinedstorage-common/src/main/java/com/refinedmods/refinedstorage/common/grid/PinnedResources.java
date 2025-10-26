package com.refinedmods.refinedstorage.common.grid;

import com.refinedmods.refinedstorage.common.api.grid.view.GridResource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class PinnedResources {
    private final List<GridResource> resources = new ArrayList<>();
    private final List<GridResource> resourcesView = Collections.unmodifiableList(resources);

    void add(final int index, final GridResource gridResource) {
        if (contains(gridResource)) {
            return;
        }
        resources.add(index, gridResource);
    }

    GridResource remove(final int index) {
        return resources.remove(index);
    }

    List<GridResource> getAll() {
        return resourcesView;
    }

    boolean contains(final GridResource gridResource) {
        for (final GridResource existingResource : resources) {
            if (existingResource.is(gridResource)) {
                return true;
            }
        }
        return false;
    }
}
