package com.refinedmods.refinedstorage2.core.grid;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import com.refinedmods.refinedstorage2.core.storage.StorageTracker;

public interface GridView<T> {
    void setListener(Runnable listener);

    void setSortingType(GridSortingType sortingType);

    GridSortingType getSortingType();

    void setFilter(Predicate<GridStack<?>> filter);

    void setPreventSorting(boolean preventSorting);

    boolean isPreventSorting();

    void setSortingDirection(GridSortingDirection sortingDirection);

    GridSortingDirection getSortingDirection();

    void loadStack(T template, long amount, StorageTracker.Entry trackerEntry);

    Optional<StorageTracker.Entry> getTrackerEntry(Object template);

    void sort();

    void onChange(T template, long amount, StorageTracker.Entry trackerEntry);

    List<GridStack<T>> getStacks();
}
