package com.refinedmods.refinedstorage2.api.grid.view;

import com.refinedmods.refinedstorage2.api.core.CoreValidations;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.resource.list.ResourceList;
import com.refinedmods.refinedstorage2.api.resource.list.ResourceListOperationResult;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedResource;

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
import javax.annotation.Nullable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.0")
public class GridViewImpl<T> implements GridView<T> {
    private static final Logger LOGGER = LogManager.getLogger();

    private final ResourceList<T> backingList;
    private final Comparator<AbstractGridResource<?>> identitySort;
    private final Function<ResourceAmount<T>, AbstractGridResource<T>> gridResourceFactory;
    private final Map<T, TrackedResource> trackedResources = new HashMap<>();

    private List<AbstractGridResource<T>> viewList = new ArrayList<>();
    private final Map<T, AbstractGridResource<T>> viewListIndex = new HashMap<>();

    private GridSortingType sortingType = GridSortingType.QUANTITY;
    private GridSortingDirection sortingDirection = GridSortingDirection.ASCENDING;
    private Predicate<AbstractGridResource<T>> filter = resource -> true;
    @Nullable
    private Runnable listener;
    private boolean preventSorting;

    /**
     * @param gridResourceFactory     a factory that transforms a resource amount to a grid resource
     * @param backingList             the backing list
     * @param initialTrackedResources initial tracked resources state
     */
    public GridViewImpl(final Function<ResourceAmount<T>, AbstractGridResource<T>> gridResourceFactory,
                        final ResourceList<T> backingList,
                        final Map<T, TrackedResource> initialTrackedResources) {
        this.gridResourceFactory = gridResourceFactory;
        this.identitySort = GridSortingType.NAME.getComparator().apply(this);
        this.backingList = backingList;
        this.trackedResources.putAll(initialTrackedResources);
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
    public void setFilterAndSort(final Predicate<AbstractGridResource<T>> predicate) {
        this.filter = predicate;
        sort();
    }

    @Override
    public boolean setPreventSorting(final boolean changedPreventSorting) {
        final boolean changed = preventSorting != changedPreventSorting;
        this.preventSorting = changedPreventSorting;
        return changed;
    }

    @Override
    public void setSortingDirection(final GridSortingDirection sortingDirection) {
        this.sortingDirection = sortingDirection;
    }

    @Override
    public Optional<TrackedResource> getTrackedResource(final T resource) {
        return Optional.ofNullable(trackedResources.get(resource));
    }

    @Override
    public void sort() {
        LOGGER.info("Sorting grid view");

        viewListIndex.clear();
        viewList = backingList
            .getAll()
            .stream()
            .map(gridResourceFactory)
            .sorted(getComparator())
            .filter(filter)
            .collect(Collectors.toList());

        viewList.forEach(resource -> viewListIndex.put(resource.getResourceAmount().getResource(), resource));

        notifyListener();
    }

    @Override
    public void onChange(final T resource, final long amount, @Nullable final TrackedResource trackedResource) {
        final ResourceListOperationResult<T> operationResult = updateBackingList(resource, amount);

        updateOrRemoveTrackedResource(resource, trackedResource);

        final AbstractGridResource<T> gridResource = viewListIndex.get(resource);
        if (gridResource != null) {
            if (gridResource.isZeroed()) {
                reinsertZeroedResourceIntoViewList(resource, operationResult, gridResource);
            } else {
                handleChangeForExistingResource(resource, operationResult, gridResource);
            }
        } else {
            handleChangeForNewResource(resource, operationResult);
        }
    }

    private ResourceListOperationResult<T> updateBackingList(final T resource, final long amount) {
        if (amount < 0) {
            return backingList.remove(resource, Math.abs(amount)).orElseThrow(RuntimeException::new);
        } else {
            return backingList.add(resource, amount);
        }
    }

    private void updateOrRemoveTrackedResource(final T resource, @Nullable final TrackedResource trackedResource) {
        if (trackedResource == null) {
            trackedResources.remove(resource);
        } else {
            trackedResources.put(resource, trackedResource);
        }
    }

    private void reinsertZeroedResourceIntoViewList(final T resource,
                                                    final ResourceListOperationResult<T> operationResult,
                                                    final AbstractGridResource<T> oldGridResource) {
        final AbstractGridResource<T> newResource = gridResourceFactory.apply(operationResult.resourceAmount());
        viewListIndex.put(resource, newResource);
        final int index = CoreValidations.validateNotNegative(
            viewList.indexOf(oldGridResource),
            "Cannot reinsert previously zeroed resource, it was not found"
        );
        viewList.set(index, newResource);
    }

    private void handleChangeForExistingResource(final T resource,
                                                 final ResourceListOperationResult<T> operationResult,
                                                 final AbstractGridResource<T> gridResource) {
        final boolean noLongerAvailable = !operationResult.available();
        final boolean canBeSorted = !preventSorting;
        if (canBeSorted) {
            updateExistingResourceInViewList(resource, gridResource, noLongerAvailable);
        } else if (noLongerAvailable) {
            gridResource.setZeroed(true);
        }
    }

    private void updateExistingResourceInViewList(final T resource,
                                                  final AbstractGridResource<T> gridResource,
                                                  final boolean noLongerAvailable) {
        viewList.remove(gridResource);
        if (noLongerAvailable) {
            viewListIndex.remove(resource);
            notifyListener();
        } else {
            addIntoView(gridResource);
            notifyListener();
        }
    }

    private void handleChangeForNewResource(final T resource, final ResourceListOperationResult<T> operationResult) {
        final AbstractGridResource<T> gridResource = gridResourceFactory.apply(operationResult.resourceAmount());
        if (filter.test(gridResource)) {
            viewListIndex.put(resource, gridResource);
            addIntoView(gridResource);
            notifyListener();
        }
    }

    private void addIntoView(final AbstractGridResource<T> resource) {
        final int wouldBePosition = Collections.binarySearch(viewList, resource, getComparator());
        CoreValidations.validateNegative(wouldBePosition, "Resource is already present in view list, cannot reinsert");
        viewList.add(-wouldBePosition - 1, resource);
    }

    private void notifyListener() {
        if (listener != null) {
            listener.run();
        }
    }

    private Comparator<AbstractGridResource<?>> getComparator() {
        // An identity sort is necessary so the order of items is preserved in quantity sorting mode.
        // If two grid resources have the same quantity, their order would otherwise not be preserved.
        final Comparator<AbstractGridResource<?>> comparator =
            sortingType.getComparator().apply(this).thenComparing(identitySort);
        if (sortingDirection == GridSortingDirection.ASCENDING) {
            return comparator;
        }
        return comparator.reversed();
    }

    @Override
    public List<AbstractGridResource<T>> getAll() {
        return viewList;
    }
}
