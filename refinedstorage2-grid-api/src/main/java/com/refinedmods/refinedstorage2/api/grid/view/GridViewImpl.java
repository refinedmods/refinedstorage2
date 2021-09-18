package com.refinedmods.refinedstorage2.api.grid.view;

import com.refinedmods.refinedstorage2.api.grid.view.stack.GridStack;
import com.refinedmods.refinedstorage2.api.stack.ResourceAmount;
import com.refinedmods.refinedstorage2.api.stack.list.StackList;
import com.refinedmods.refinedstorage2.api.stack.list.StackListResult;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageTracker;

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

public class GridViewImpl<T> implements GridView<T> {
    private static final Logger LOGGER = LogManager.getLogger(GridViewImpl.class);

    private final StackList<T> list;
    private final Comparator<GridStack<?>> identitySort;
    private final Function<ResourceAmount<T>, GridStack<T>> stackFactory;
    private final Map<T, StorageTracker.Entry> trackerEntries = new HashMap<>();
    private final Map<T, GridStack<T>> stackIndex = new HashMap<>();

    private List<GridStack<T>> stacks = new ArrayList<>();
    private GridSortingType sortingType;
    private GridSortingDirection sortingDirection = GridSortingDirection.ASCENDING;
    private Predicate<GridStack<T>> filter = stack -> true;
    private Runnable listener;
    private boolean preventSorting;

    public GridViewImpl(Function<ResourceAmount<T>, GridStack<T>> stackFactory, StackList<T> list) {
        this.stackFactory = stackFactory;
        this.identitySort = GridSortingType.NAME.getComparator().apply(this);
        this.list = list;
    }

    @Override
    public void setListener(Runnable listener) {
        this.listener = listener;
    }

    @Override
    public GridSortingType getSortingType() {
        return sortingType;
    }

    @Override
    public void setSortingType(GridSortingType sortingType) {
        this.sortingType = sortingType;
    }

    @Override
    public void setFilter(Predicate<GridStack<T>> filter) {
        this.filter = filter;
    }

    @Override
    public boolean isPreventSorting() {
        return preventSorting;
    }

    @Override
    public void setPreventSorting(boolean preventSorting) {
        this.preventSorting = preventSorting;
    }

    private Comparator<GridStack<?>> getComparator() {
        if (sortingType == null) {
            return sortingDirection == GridSortingDirection.ASCENDING ? identitySort : identitySort.reversed();
        }

        // An identity sort is necessary so the order of items is preserved in quantity sorting mode.
        // If two grid stacks have the same quantity, their order would not be preserved.
        if (sortingDirection == GridSortingDirection.ASCENDING) {
            return sortingType.getComparator().apply(this).thenComparing(identitySort);
        }

        return sortingType.getComparator().apply(this).thenComparing(identitySort).reversed();
    }

    @Override
    public GridSortingDirection getSortingDirection() {
        return sortingDirection;
    }

    @Override
    public void setSortingDirection(GridSortingDirection sortingDirection) {
        this.sortingDirection = sortingDirection;
    }

    @Override
    public void loadResource(T resource, long amount, StorageTracker.Entry trackerEntry) {
        list.add(resource, amount);
        trackerEntries.put(resource, trackerEntry);
    }

    @Override
    public Optional<StorageTracker.Entry> getTrackerEntry(Object template) {
        return Optional.ofNullable(trackerEntries.get((T) template));
    }

    @Override
    public void sort() {
        LOGGER.info("Sorting grid view");

        stackIndex.clear();
        stacks = list
                .getAll()
                .stream()
                .map(stackFactory)
                .sorted(getComparator())
                .filter(filter)
                .collect(Collectors.toList());

        stacks.forEach(stack -> stackIndex.put(stack.getResourceAmount().getResource(), stack));

        notifyListener();
    }

    @Override
    public void onChange(T resource, long amount, StorageTracker.Entry trackerEntry) {
        StackListResult<T> stack;
        if (amount < 0) {
            stack = list.remove(resource, Math.abs(amount)).orElseThrow(RuntimeException::new);
        } else {
            stack = list.add(resource, amount);
        }

        if (trackerEntry == null) {
            trackerEntries.remove(resource);
        } else {
            trackerEntries.put(resource, trackerEntry);
        }

        GridStack<T> gridStack = stackIndex.get(resource);
        if (gridStack != null) {
            if (gridStack.isZeroed()) {
                handleChangeForZeroedStack(resource, stack, gridStack);
            } else {
                handleChangeForExistingStack(resource, stack, gridStack);
            }
        } else {
            handleChangeForNewStack(resource, stack);
        }
    }

    private void handleChangeForNewStack(T resource, StackListResult<T> stack) {
        GridStack<T> gridStack = stackFactory.apply(stack.resourceAmount());
        if (filter.test(gridStack)) {
            stackIndex.put(resource, gridStack);
            addIntoView(gridStack);
            notifyListener();
        }
    }

    private void handleChangeForExistingStack(T resource, StackListResult<T> stack, GridStack<T> gridStack) {
        if (!preventSorting) {
            if (!filter.test(gridStack) || !stack.available()) {
                stacks.remove(gridStack);
                stackIndex.remove(resource);
                notifyListener();
            } else if (stack.available()) {
                stacks.remove(gridStack);
                addIntoView(gridStack);
                notifyListener();
            }
        } else if (!stack.available()) {
            gridStack.setZeroed(true);
        }
    }

    private void handleChangeForZeroedStack(T resource, StackListResult<T> stack, GridStack<T> oldGridStack) {
        GridStack<T> newStack = stackFactory.apply(stack.resourceAmount());

        stackIndex.put(resource, newStack);

        int index = stacks.indexOf(oldGridStack);
        stacks.set(index, newStack);
    }

    private void addIntoView(GridStack<T> stack) {
        int pos = Collections.binarySearch(stacks, stack, getComparator());
        if (pos < 0) {
            pos = -pos - 1;
        }
        stacks.add(pos, stack);
    }

    private void notifyListener() {
        if (listener != null) {
            listener.run();
        }
    }

    @Override
    public List<GridStack<T>> getStacks() {
        return stacks;
    }
}
