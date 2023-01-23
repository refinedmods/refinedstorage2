package com.refinedmods.refinedstorage2.api.grid.view;

import com.refinedmods.refinedstorage2.api.core.CoreValidations;
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
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

import org.apiguardian.api.API;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.0")
public class GridViewImpl implements GridView {
    private static final Logger LOGGER = LoggerFactory.getLogger(GridViewImpl.class);

    private final ResourceList<Object> backingList;
    private final Comparator<AbstractGridResource> identitySort;
    private final GridResourceFactory resourceFactory;
    private final Map<Object, TrackedResource> trackedResources = new HashMap<>();

    private List<AbstractGridResource> viewList = new ArrayList<>();
    private final Map<Object, AbstractGridResource> viewListIndex = new HashMap<>();

    private GridSortingType sortingType = GridSortingType.QUANTITY;
    private GridSortingDirection sortingDirection = GridSortingDirection.ASCENDING;
    private Predicate<AbstractGridResource> filter = resource -> true;
    @Nullable
    private Runnable listener;
    private boolean preventSorting;

    /**
     * @param resourceFactory         a factory that transforms a resource amount to a grid resource
     * @param backingList             the backing list
     * @param initialTrackedResources initial tracked resources state
     */
    public GridViewImpl(final GridResourceFactory resourceFactory,
                        final ResourceList<Object> backingList,
                        final Map<Object, TrackedResource> initialTrackedResources) {
        this.resourceFactory = resourceFactory;
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
    public void setFilterAndSort(final Predicate<AbstractGridResource> predicate) {
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
    public <T> Optional<TrackedResource> getTrackedResource(final T resource) {
        return Optional.ofNullable(trackedResources.get(resource));
    }

    @Override
    public void sort() {
        LOGGER.info("Sorting grid view");

        viewListIndex.clear();
        viewList = backingList
            .getAll()
            .stream()
            .flatMap(resourceAmount -> resourceFactory.apply(resourceAmount).stream())
            .filter(filter)
            .sorted(getComparator())
            .collect(Collectors.toList());

        viewList.forEach(resource -> resource.populateInViewListIndex(viewListIndex));

        notifyListener();
    }

    @Override
    public <T> void onChange(final T resource, final long amount, @Nullable final TrackedResource trackedResource) {
        final ResourceListOperationResult<?> operationResult = updateBackingList(resource, amount);

        updateOrRemoveTrackedResource(resource, trackedResource);

        final AbstractGridResource gridResource = viewListIndex.get(resource);
        if (gridResource != null) {
            LOGGER.debug("{} was already found in the view list", resource);
            if (gridResource.isZeroed()) {
                reinsertZeroedResourceIntoViewList(resource, operationResult, gridResource);
            } else {
                handleChangeForExistingResource(resource, operationResult, gridResource);
            }
        } else {
            LOGGER.debug("{} is a new resource, adding it into the view list if filter allows it", resource);
            handleChangeForNewResource(resource, operationResult);
        }
    }

    private <T> ResourceListOperationResult<?> updateBackingList(final T resource, final long amount) {
        if (amount < 0) {
            return backingList.remove(resource, Math.abs(amount)).orElseThrow(RuntimeException::new);
        } else {
            return backingList.add(resource, amount);
        }
    }

    private <T> void updateOrRemoveTrackedResource(final T resource, @Nullable final TrackedResource trackedResource) {
        if (trackedResource == null) {
            trackedResources.remove(resource);
        } else {
            trackedResources.put(resource, trackedResource);
        }
    }

    private <T> void reinsertZeroedResourceIntoViewList(final T resource,
                                                        final ResourceListOperationResult<?> operationResult,
                                                        final AbstractGridResource oldGridResource) {
        LOGGER.debug("{} was zeroed, unzeroing", resource);
        final AbstractGridResource newResource = resourceFactory.apply(operationResult.resourceAmount()).orElseThrow();
        viewListIndex.put(resource, newResource);
        final int index = CoreValidations.validateNotNegative(
            viewList.indexOf(oldGridResource),
            "Cannot reinsert previously zeroed resource, it was not found"
        );
        viewList.set(index, newResource);
    }

    private <T> void handleChangeForExistingResource(final T resource,
                                                     final ResourceListOperationResult<?> operationResult,
                                                     final AbstractGridResource gridResource) {
        final boolean noLongerAvailable = !operationResult.available();
        final boolean canBeSorted = !preventSorting;
        if (canBeSorted) {
            LOGGER.debug("Actually updating {} resource in the view list", resource);
            updateExistingResourceInViewList(resource, gridResource, noLongerAvailable);
        } else if (noLongerAvailable) {
            LOGGER.debug("{} is no longer available, zeroing", resource);
            gridResource.setZeroed(true);
        } else {
            LOGGER.debug("{} can't be sorted, preventing sorting is on", resource);
        }
    }

    private <T> void updateExistingResourceInViewList(final T resource,
                                                      final AbstractGridResource gridResource,
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

    private <T> void handleChangeForNewResource(final T resource,
                                                final ResourceListOperationResult<?> operationResult) {
        final AbstractGridResource gridResource = resourceFactory.apply(operationResult.resourceAmount()).orElseThrow();
        if (filter.test(gridResource)) {
            LOGGER.debug("Filter allowed, actually adding {}", resource);
            viewListIndex.put(resource, gridResource);
            addIntoView(gridResource);
            notifyListener();
        }
    }

    private void addIntoView(final AbstractGridResource resource) {
        // Calculate the position according to sorting rules.
        final int wouldBePosition = Collections.binarySearch(viewList, resource, getComparator());
        // Most of the time, the "would be" position is negative, indicating that the resource wasn't found yet in the
        // list, comparing with sorting rules. The absolute of this position would be the "real" position if sorted.
        if (wouldBePosition < 0) {
            viewList.add(-wouldBePosition - 1, resource);
        } else {
            // If the "would be" position is positive, this means that the resource is already contained in the list,
            // comparing with sorting rules.
            // This doesn't mean that the *exact* resource is already in the list, but that is purely "contained"
            // in the list when comparing with sorting rules.
            // For example: a resource with different identity but the same name (in Minecraft: an enchanted book
            // with different NBT).
            // In that case, just insert it after the "existing" resource.
            viewList.add(wouldBePosition + 1, resource);
        }
    }

    private void notifyListener() {
        if (listener != null) {
            listener.run();
        }
    }

    private Comparator<AbstractGridResource> getComparator() {
        // An identity sort is necessary so the order of items is preserved in quantity sorting mode.
        // If two grid resources have the same quantity, their order would otherwise not be preserved.
        final Comparator<AbstractGridResource> comparator =
            sortingType.getComparator().apply(this).thenComparing(identitySort);
        if (sortingDirection == GridSortingDirection.ASCENDING) {
            return comparator;
        }
        return comparator.reversed();
    }

    @Override
    public List<AbstractGridResource> getAll() {
        return viewList;
    }
}
