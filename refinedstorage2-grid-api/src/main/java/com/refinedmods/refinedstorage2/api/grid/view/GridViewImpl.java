package com.refinedmods.refinedstorage2.api.grid.view;

import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.resource.list.ResourceList;
import com.refinedmods.refinedstorage2.api.resource.list.ResourceListOperationResult;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedResource;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.0")
public class GridViewImpl<T> implements GridView<T> {
    private static final Logger LOGGER = LogManager.getLogger();

    private final ResourceList<T> backingList;
    private final Comparator<GridResource<?>> identitySort;
    private final Function<ResourceAmount<T>, GridResource<T>> gridResourceFactory;
    private final Map<T, TrackedResource> trackedResources = new HashMap<>();
    private final Map<T, GridResource<T>> resourceIndex = new HashMap<>();

    private List<GridResource<T>> viewList = new ArrayList<>();
    private GridSortingType sortingType = GridSortingType.QUANTITY;
    private GridSortingDirection sortingDirection = GridSortingDirection.ASCENDING;
    private Predicate<GridResource<T>> filter = resource -> true;
    @Nullable
    private Runnable listener;
    private boolean preventSorting;

    /**
     * @param gridResourceFactory a factory that transforms a resource amount to a grid resource
     * @param backingList         the backing list
     */
    public GridViewImpl(final Function<ResourceAmount<T>, GridResource<T>> gridResourceFactory,
                        final ResourceList<T> backingList) {
        this.gridResourceFactory = gridResourceFactory;
        this.identitySort = GridSortingType.NAME.getComparator().apply(this);
        this.backingList = backingList;
    }

    @Override
    public void setListener(@Nullable final Runnable listener) {
        this.listener = listener;
    }

    @Override
    public void setSortingType(final GridSortingType sortingType) {
        this.sortingType = sortingType;
    }

    @Override
    public void setFilter(final Predicate<GridResource<T>> filter) {
        this.filter = filter;
    }

    @Override
    public boolean isPreventSorting() {
        return preventSorting;
    }

    @Override
    public void setPreventSorting(final boolean preventSorting) {
        this.preventSorting = preventSorting;
    }

    private Comparator<GridResource<?>> getComparator() {
        // An identity sort is necessary so the order of items is preserved in quantity sorting mode.
        // If two grid resources have the same quantity, their order would not be preserved.
        if (sortingDirection == GridSortingDirection.ASCENDING) {
            return sortingType.getComparator().apply(this).thenComparing(identitySort);
        }

        return sortingType.getComparator().apply(this).thenComparing(identitySort).reversed();
    }

    @Override
    public void setSortingDirection(final GridSortingDirection sortingDirection) {
        this.sortingDirection = sortingDirection;
    }

    @Override
    public void loadResource(final T resource, final long amount, @Nullable final TrackedResource trackedResource) {
        backingList.add(resource, amount);
        trackedResources.put(resource, trackedResource);
    }

    @Override
    public Optional<TrackedResource> getTrackedResource(final T resource) {
        return Optional.ofNullable(trackedResources.get(resource));
    }

    @Override
    public void sort() {
        LOGGER.info("Sorting grid view");

        resourceIndex.clear();
        viewList = backingList
                .getAll()
                .stream()
                .map(gridResourceFactory)
                .sorted(getComparator())
                .filter(filter)
                .collect(Collectors.toList());

        viewList.forEach(resource -> resourceIndex.put(resource.getResourceAmount().getResource(), resource));

        notifyListener();
    }

    @Override
    public void onChange(final T resource, final long amount, @Nullable final TrackedResource trackedResource) {
        final ResourceListOperationResult<T> operationResult = updateBackingList(resource, amount);

        updateTrackedResource(resource, trackedResource);

        final GridResource<T> gridResource = resourceIndex.get(resource);
        if (gridResource != null) {
            if (gridResource.isZeroed()) {
                handleChangeForZeroedResource(resource, operationResult, gridResource);
            } else {
                handleChangeForExistingResource(resource, operationResult, gridResource);
            }
        } else {
            handleChangeForNewResource(resource, operationResult);
        }
    }

    private void updateTrackedResource(final T resource, @Nullable final TrackedResource trackedResource) {
        if (trackedResource == null) {
            trackedResources.remove(resource);
        } else {
            trackedResources.put(resource, trackedResource);
        }
    }

    private ResourceListOperationResult<T> updateBackingList(final T resource, final long amount) {
        if (amount < 0) {
            return backingList.remove(resource, Math.abs(amount)).orElseThrow(RuntimeException::new);
        } else {
            return backingList.add(resource, amount);
        }
    }

    private void handleChangeForNewResource(final T resource, final ResourceListOperationResult<T> operationResult) {
        final GridResource<T> gridResource = gridResourceFactory.apply(operationResult.resourceAmount());
        if (filter.test(gridResource)) {
            resourceIndex.put(resource, gridResource);
            addIntoView(gridResource);
            notifyListener();
        }
    }

    private void handleChangeForExistingResource(final T resource,
                                                 final ResourceListOperationResult<T> operationResult,
                                                 final GridResource<T> gridResource) {
        if (!preventSorting) {
            if (!filter.test(gridResource) || !operationResult.available()) {
                viewList.remove(gridResource);
                resourceIndex.remove(resource);
                notifyListener();
            } else if (operationResult.available()) {
                viewList.remove(gridResource);
                addIntoView(gridResource);
                notifyListener();
            }
        } else if (!operationResult.available()) {
            gridResource.setZeroed(true);
        }
    }

    private void handleChangeForZeroedResource(final T resource,
                                               final ResourceListOperationResult<T> operationResult,
                                               final GridResource<T> oldGridResource) {
        final GridResource<T> newResource = gridResourceFactory.apply(operationResult.resourceAmount());

        resourceIndex.put(resource, newResource);

        final int index = viewList.indexOf(oldGridResource);
        viewList.set(index, newResource);
    }

    private void addIntoView(final GridResource<T> resource) {
        int pos = Collections.binarySearch(viewList, resource, getComparator());
        if (pos < 0) {
            pos = -pos - 1;
        }
        viewList.add(pos, resource);
    }

    private void notifyListener() {
        if (listener != null) {
            listener.run();
        }
    }

    @Override
    public List<GridResource<T>> getAll() {
        return viewList;
    }
}
