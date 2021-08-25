package com.refinedmods.refinedstorage2.api.grid.view;

import com.refinedmods.refinedstorage2.api.grid.view.stack.GridStack;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageTracker;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public interface GridView<T> {
    void setListener(Runnable listener);

    GridSortingType getSortingType();

    void setSortingType(GridSortingType sortingType);

    void setFilter(Predicate<GridStack<?>> filter);

    boolean isPreventSorting();

    void setPreventSorting(boolean preventSorting);

    GridSortingDirection getSortingDirection();

    void setSortingDirection(GridSortingDirection sortingDirection);

    void loadStack(T template, long amount, StorageTracker.Entry trackerEntry);

    Optional<StorageTracker.Entry> getTrackerEntry(Object template);

    void sort();

    void onChange(T template, long amount, StorageTracker.Entry trackerEntry);

    List<GridStack<T>> getStacks();
}
