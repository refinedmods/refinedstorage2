package com.refinedmods.refinedstorage2.api.grid.view;

import com.refinedmods.refinedstorage2.api.core.CoreValidations;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.resource.ResourceKey;
import com.refinedmods.refinedstorage2.api.resource.list.ResourceList;
import com.refinedmods.refinedstorage2.api.resource.list.ResourceListImpl;
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
import javax.annotation.Nullable;

import org.apiguardian.api.API;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.0")
public class GridViewImpl implements GridView {
    private static final Logger LOGGER = LoggerFactory.getLogger(GridViewImpl.class);

    private final ResourceList backingList;
    private final Comparator<GridResource> identitySort;
    private final GridResourceFactory resourceFactory;
    private final Map<ResourceKey, TrackedResource> trackedResources = new HashMap<>();

    private List<GridResource> viewList = new ArrayList<>();
    private final Map<ResourceKey, GridResource> viewListIndex = new HashMap<>();

    private GridSortingType sortingType;
    private GridSortingDirection sortingDirection = GridSortingDirection.ASCENDING;
    private Predicate<GridResource> filter = resource -> true;
    @Nullable
    private Runnable listener;
    private boolean preventSorting;

    /**
     * @param resourceFactory         a factory that transforms a resource amount to a grid resource
     * @param backingList             the backing list
     * @param initialTrackedResources initial tracked resources state
     * @param identitySortingType     a sorting type required to keep a consistent sorting order with quantity sorting
     * @param defaultSortingType      the default sorting type
     */
    public GridViewImpl(final GridResourceFactory resourceFactory,
                        final ResourceList backingList,
                        final Map<ResourceKey, TrackedResource> initialTrackedResources,
                        final GridSortingType identitySortingType,
                        final GridSortingType defaultSortingType) {
        this.resourceFactory = resourceFactory;
        this.identitySort = identitySortingType.apply(this);
        this.sortingType = defaultSortingType;
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
    public Predicate<GridResource> setFilterAndSort(final Predicate<GridResource> predicate) {
        final Predicate<GridResource> previousPredicate = filter;
        this.filter = predicate;
        sort();
        return previousPredicate;
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
    public Optional<TrackedResource> getTrackedResource(final ResourceKey resource) {
        return Optional.ofNullable(trackedResources.get(resource));
    }

    @Override
    public void sort() {
        LOGGER.info("Sorting grid view");

        viewListIndex.clear();

        final List<GridResource> newViewList = new ArrayList<>();
        for (final ResourceAmount backingListItem : backingList.getAll()) {
            resourceFactory.apply(backingListItem).ifPresent(gridResource -> {
                if (filter.test(gridResource)) {
                    newViewList.add(gridResource);
                    viewListIndex.put(backingListItem.getResource(), gridResource);
                }
            });
        }
        newViewList.sort(getComparator());
        viewList = newViewList;

        notifyListener();
    }

    @Override
    public void onChange(final ResourceKey resource,
                         final long amount,
                         @Nullable final TrackedResource trackedResource) {
        final ResourceListOperationResult operationResult = updateBackingList(resource, amount);

        updateOrRemoveTrackedResource(resource, trackedResource);

        final GridResource gridResource = viewListIndex.get(resource);
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

    private ResourceListOperationResult updateBackingList(final ResourceKey resource, final long amount) {
        if (amount < 0) {
            return backingList.remove(resource, Math.abs(amount)).orElseThrow(RuntimeException::new);
        } else {
            return backingList.add(resource, amount);
        }
    }

    private void updateOrRemoveTrackedResource(final ResourceKey resource,
                                               @Nullable final TrackedResource trackedResource) {
        if (trackedResource == null) {
            trackedResources.remove(resource);
        } else {
            trackedResources.put(resource, trackedResource);
        }
    }

    private void reinsertZeroedResourceIntoViewList(final ResourceKey resource,
                                                    final ResourceListOperationResult operationResult,
                                                    final GridResource oldGridResource) {
        LOGGER.debug("{} was zeroed, unzeroing", resource);
        final GridResource newResource = resourceFactory.apply(operationResult.resourceAmount()).orElseThrow();
        viewListIndex.put(resource, newResource);
        final int index = CoreValidations.validateNotNegative(
            viewList.indexOf(oldGridResource),
            "Cannot reinsert previously zeroed resource, it was not found"
        );
        viewList.set(index, newResource);
    }

    private void handleChangeForExistingResource(final ResourceKey resource,
                                                 final ResourceListOperationResult operationResult,
                                                 final GridResource gridResource) {
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

    private void updateExistingResourceInViewList(final ResourceKey resource,
                                                  final GridResource gridResource,
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

    private void handleChangeForNewResource(final ResourceKey resource,
                                            final ResourceListOperationResult operationResult) {
        final GridResource gridResource = resourceFactory.apply(operationResult.resourceAmount()).orElseThrow();
        if (filter.test(gridResource)) {
            LOGGER.debug("Filter allowed, actually adding {}", resource);
            viewListIndex.put(resource, gridResource);
            addIntoView(gridResource);
            notifyListener();
        }
    }

    private void addIntoView(final GridResource resource) {
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

    private Comparator<GridResource> getComparator() {
        // An identity sort is necessary so the order of items is preserved in quantity sorting mode.
        // If two grid resources have the same quantity, their order would otherwise not be preserved.
        final Comparator<GridResource> comparator = sortingType.apply(this).thenComparing(identitySort);
        if (sortingDirection == GridSortingDirection.ASCENDING) {
            return comparator;
        }
        return comparator.reversed();
    }

    @Override
    public List<GridResource> getViewList() {
        return viewList;
    }

    @Override
    public ResourceList copyBackingList() {
        final ResourceList copy = new ResourceListImpl();
        backingList.getAll().forEach(copy::add);
        return copy;
    }

    @Override
    public void clear() {
        backingList.clear();
        viewListIndex.clear();
        trackedResources.clear();
        viewList.clear();
    }
}
