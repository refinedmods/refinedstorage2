package com.refinedmods.refinedstorage2.api.grid.view;

import com.refinedmods.refinedstorage2.api.resource.list.ResourceList;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedResource;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import javax.annotation.Nullable;

import org.apiguardian.api.API;

/**
 * Represents a grid view.
 * The grid view internally has a backing list and a view list.
 * The backing list is the logical view of the grid without any filtering or sorting applied. It's the source of truth.
 * The view list has filtering and sorting rules applied and is in sync with the backing list (depending on the view
 * being in "prevent sorting" mode).
 */
@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.0")
public interface GridView {
    /**
     * Sets a listener that is called when the grid view changes.
     *
     * @param listener the listener, can be null
     */
    void setListener(@Nullable Runnable listener);

    /**
     * Changing the sorting type still requires a call to {@link #sort()}.
     *
     * @param sortingType the sorting type
     */
    void setSortingType(GridSortingType sortingType);

    /**
     * @param predicate the filter
     */
    void setFilterAndSort(Predicate<GridResource> predicate);

    /**
     * Preventing sorting means that the changes will still arrive at the backing list and view list, but,
     * the view list won't be resorted and if a resource is zeroed, will stay in view until sorting is enabled
     * again.
     * This still requires a call to {@link #sort()} when preventing sorting is disabled again.
     *
     * @param changedPreventSorting whether the view should prevent sorting on changes
     * @return whether prevent sorting has changed
     */
    boolean setPreventSorting(boolean changedPreventSorting);

    /**
     * Changing the sorting direction still requires a call to {@link #sort()}.
     *
     * @param sortingDirection the sorting direction
     */
    void setSortingDirection(GridSortingDirection sortingDirection);

    /**
     * @param <T>      the resource type
     * @param resource the resource
     * @return the tracked resource, if present
     */
    <T> Optional<TrackedResource> getTrackedResource(T resource);

    /**
     * Sorts the view list.
     * Applies sorting and filtering rules.
     */
    void sort();

    /**
     * Applies a change to a resource. Will update the backing list, and will also update the view list (depending
     * if the view is preventing sorting).
     *
     * @param <T>             the resource type
     * @param resource        the resource
     * @param amount          the amount, can be negative or positive
     * @param trackedResource the tracked resource, can be null
     */
    <T> void onChange(T resource, long amount, @Nullable TrackedResource trackedResource);

    /**
     * @return the view list
     */
    List<GridResource> getViewList();

    /**
     * @return a copy of the backing list
     */
    ResourceList<Object> copyBackingList();
}
