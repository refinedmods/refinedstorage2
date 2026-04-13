package com.refinedmods.refinedstorage.api.resource.repository;

import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.resource.list.MutableResourceList;

import java.util.Comparator;
import java.util.List;

import org.apiguardian.api.API;
import org.jspecify.annotations.Nullable;

/**
 * Represents a view over a {@link com.refinedmods.refinedstorage.api.resource.list.ResourceList}.
 * The {@link com.refinedmods.refinedstorage.api.resource.list.ResourceList} contained in this repository
 * is the backing list.
 * The view list is a sorted and filtered version of the backing list.
 * The resources from the backing list are mapped through a {@link ResourceRepositoryMapper} when inserted into
 * the view list.
 *
 * @param <T> the mapped type
 */
@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.0")
public interface ResourceRepository<T> {
    /**
     * Sets a listener that is called when the view list changes.
     *
     * @param listener the listener, can be null
     */
    void setListener(@Nullable Runnable listener);

    /**
     * After changing the sort, you still need to call {@link #sort()}.
     *
     * @param sort      the sort
     * @param direction the direction
     */
    void setSort(Comparator<T> sort, SortingDirection direction);

    /**
     * Preventing sorting means that the changes will still arrive at the backing list and view list, but,
     * the view list won't be resorted and if a resource is completely removed,
     * it will stay in the view list until sorting is enabled again.
     * When disabling prevent sorting, you still need to call {@link #sort()}.
     *
     * @param preventSorting whether the view should prevent sorting on changes
     * @return whether prevent sorting has changed
     */
    boolean setPreventSorting(boolean preventSorting);

    /**
     * @param filter the filter
     * @return the previous filter
     */
    ResourceRepositoryFilter<T> setFilterAndSort(ResourceRepositoryFilter<T> filter);

    /**
     * @param resource the resource
     * @return the amount in the view, or zero if not present
     */
    long getAmount(ResourceKey resource);

    /**
     * @param resource the resource
     * @return whether the resource is sticky
     */
    boolean isSticky(ResourceKey resource);

    /**
     * Sorts the view list.
     * Applies sorting and filtering rules.
     */
    void sort();

    /**
     * Applies a change to a resource. Will update the backing list, and will also update the view list (depending
     * on if the view is preventing sorting).
     *
     * @param resource the resource
     * @param amount   the amount, can be negative or positive, but not zero
     */
    void update(ResourceKey resource, long amount);

    /**
     * @return the view list
     */
    List<T> getViewList();

    /**
     * @return a copy of the backing list
     */
    MutableResourceList copyBackingList();

    /**
     * Clears the backing list and view list.
     */
    void clear();
}
