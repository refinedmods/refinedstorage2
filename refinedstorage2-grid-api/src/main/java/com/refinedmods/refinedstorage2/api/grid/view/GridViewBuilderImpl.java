package com.refinedmods.refinedstorage2.api.grid.view;

import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.resource.list.ResourceList;
import com.refinedmods.refinedstorage2.api.resource.list.ResourceListImpl;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedResource;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import javax.annotation.Nullable;

public class GridViewBuilderImpl<T> implements GridViewBuilder<T> {
    private final Function<ResourceAmount<T>, AbstractGridResource<T>> gridResourceFactory;
    private final ResourceList<T> backingList = new ResourceListImpl<>();
    private final Map<T, TrackedResource> trackedResources = new HashMap<>();

    public GridViewBuilderImpl(final Function<ResourceAmount<T>, AbstractGridResource<T>> gridResourceFactory) {
        this.gridResourceFactory = gridResourceFactory;
    }

    @Override
    public GridViewBuilder<T> withResource(final T resource,
                                           final long amount,
                                           @Nullable final TrackedResource trackedResource) {
        backingList.add(resource, amount);
        trackedResources.put(resource, trackedResource);
        return this;
    }

    @Override
    public GridView<T> build() {
        return new GridViewImpl<>(
            gridResourceFactory,
            backingList,
            trackedResources
        );
    }
}
