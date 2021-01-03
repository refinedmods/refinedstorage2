package com.refinedmods.refinedstorage2.core.grid;

import com.refinedmods.refinedstorage2.core.storage.StorageTracker;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public interface GridView<T> {
    void setListener(Runnable listener);

    void setSorter(Comparator<GridStack<?>> sorter);

    void setFilter(Predicate<GridStack<T>> filter);

    void setPreventSorting(boolean preventSorting);

    boolean isPreventSorting();

    void setSortingDirection(GridSortingDirection sortingDirection);

    void loadStack(T template, int amount, StorageTracker.Entry trackerEntry);

    Optional<StorageTracker.Entry> getTrackerEntry(T template);

    void sort();

    void onChange(T template, int amount, StorageTracker.Entry trackerEntry);

    List<GridStack<T>> getStacks();
}
