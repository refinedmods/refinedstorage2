package com.refinedmods.refinedstorage2.api.grid.view;

import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedResource;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import org.apiguardian.api.API;

/**
 * Represents a grid view.
 * The grid view internally has a backing list and a view list.
 * The backing list is the logical view of the grid without any filtering or sorting applied. It's the source of truth.
 * The view list has filtering and sorting rules applied and is semi in sync with the backing list (depending if the view
 * is in "prevent sorting" mode).
 *
 * @param <T> the resource type
 */
@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.0")
public interface GridView<T> {
    /**
     * Sets a listener that is called when the grid view changes.
     *
     * @param listener the listener
     */
    void setListener(Runnable listener);

    /**
     * @return the sorting type
     */
    GridSortingType getSortingType();

    /**
     * Changing the sorting type still requires a call to {@link #sort()}.
     *
     * @param sortingType the sorting type
     */
    void setSortingType(GridSortingType sortingType);

    /**
     * Changing the filter still requires a call to {@link #sort()}.
     *
     * @param filter the filter
     */
    void setFilter(Predicate<GridResource<T>> filter);

    /**
     * @return whether the view is currently not sorting
     */
    boolean isPreventSorting();

    /**
     * Preventing sorting means that the changes will still arrive at the backing list and view list, but,
     * the view list won't be resorted and if a resource is zeroed, will stay in view until sorting is enabled
     * again.
     * This still requires a call to {@link #sort()} when preventing sorting is disabled again.
     *
     * @param preventSorting whether the view should prevent sorting on changes
     */
    void setPreventSorting(boolean preventSorting);

    /**
     * @return the sorting direction
     */
    GridSortingDirection getSortingDirection();

    /**
     * Changing the sorting direction still requires a call to {@link #sort()}.
     *
     * @param sortingDirection the sorting direction
     */
    void setSortingDirection(GridSortingDirection sortingDirection);

    /**
     * Loads a resource in the backing list. The resource still won't be visible in the view list,
     * call {@link #sort()} for that.
     *
     * @param resource        the resource
     * @param amount          the amount
     * @param trackedResource the tracked resource, can be null
     */
    void loadResource(T resource, long amount, TrackedResource trackedResource);

    /**
     * @param resource the resource
     * @return the tracked resource, if present
     */
    Optional<TrackedResource> getTrackedResource(T resource);

    /**
     * Sorts the view list.
     * Applies sorting and filtering rules.
     */
    void sort();

    /**
     * Applies a change to a resource. Will update the backing list, and will also update the view list (depending
     * if the view is preventing sorting).
     *
     * @param resource        the resource
     * @param amount          the amount, can be negative or positive
     * @param trackedResource the tracked resource, can be null
     */
    void onChange(T resource, long amount, TrackedResource trackedResource);

    /**
     * @return the view list
     */
    List<GridResource<T>> getAll();
}
