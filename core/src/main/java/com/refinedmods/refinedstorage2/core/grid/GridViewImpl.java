package com.refinedmods.refinedstorage2.core.grid;

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

import com.refinedmods.refinedstorage2.core.list.StackList;
import com.refinedmods.refinedstorage2.core.list.StackListResult;
import com.refinedmods.refinedstorage2.core.storage.StorageTracker;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GridViewImpl<T, ID> implements GridView<T> {
    private static final Logger LOGGER = LogManager.getLogger(GridViewImpl.class);

    private final StackList<T> list;
    private final Comparator<GridStack<?>> identitySort;
    private final Function<T, GridStack<T>> stackFactory;
    private final Function<T, ID> idFactory;
    private final Map<ID, StorageTracker.Entry> trackerEntries = new HashMap<>();
    private final Map<ID, GridStack<T>> stackIndex = new HashMap<>();

    private List<GridStack<T>> stacks = new ArrayList<>();
    private GridSortingType sortingType;
    private GridSortingDirection sortingDirection = GridSortingDirection.ASCENDING;
    private Predicate<GridStack<?>> filter = stack -> true;
    private Runnable listener;
    private boolean preventSorting;

    public GridViewImpl(Function<T, GridStack<T>> stackFactory, Function<T, ID> idFactory, StackList<T> list) {
        this.stackFactory = stackFactory;
        this.idFactory = idFactory;
        this.identitySort = GridSortingType.NAME.getComparator().apply(this);
        this.list = list;
    }

    @Override
    public void setListener(Runnable listener) {
        this.listener = listener;
    }

    @Override
    public void setSortingType(GridSortingType sortingType) {
        this.sortingType = sortingType;
    }

    @Override
    public GridSortingType getSortingType() {
        return sortingType;
    }

    @Override
    public void setFilter(Predicate<GridStack<?>> filter) {
        this.filter = filter;
    }

    @Override
    public void setPreventSorting(boolean preventSorting) {
        this.preventSorting = preventSorting;
    }

    @Override
    public boolean isPreventSorting() {
        return preventSorting;
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
    public void setSortingDirection(GridSortingDirection sortingDirection) {
        this.sortingDirection = sortingDirection;
    }

    @Override
    public GridSortingDirection getSortingDirection() {
        return sortingDirection;
    }

    @Override
    public void loadStack(T template, long amount, StorageTracker.Entry trackerEntry) {
        list.add(template, amount);
        trackerEntries.put(idFactory.apply(template), trackerEntry);
    }

    @Override
    public Optional<StorageTracker.Entry> getTrackerEntry(Object template) {
        return Optional.ofNullable(trackerEntries.get(idFactory.apply((T) template)));
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
            .peek(stack -> stackIndex.put(idFactory.apply(stack.getStack()), stack))
            .collect(Collectors.toList());

        notifyListener();
    }

    @Override
    public void onChange(T template, long amount, StorageTracker.Entry trackerEntry) {
        StackListResult<T> stack;
        if (amount < 0) {
            stack = list.remove(template, Math.abs(amount)).orElseThrow(RuntimeException::new);
        } else {
            stack = list.add(template, amount);
        }

        ID id = idFactory.apply(stack.getStack());

        if (trackerEntry == null) {
            trackerEntries.remove(id);
        } else {
            trackerEntries.put(id, trackerEntry);
        }

        GridStack<T> gridStack = stackIndex.get(id);
        if (gridStack != null) {
            if (gridStack.isZeroed()) {
                handleChangeForZeroedStack(id, stack, gridStack);
            } else {
                handleChangeForExistingStack(id, stack, gridStack);
            }
        } else {
            handleChangeForNewStack(id, stack);
        }
    }

    private void handleChangeForNewStack(ID id, StackListResult<T> stack) {
        GridStack<T> gridStack = stackFactory.apply(stack.getStack());
        if (filter.test(gridStack)) {
            stackIndex.put(id, gridStack);
            addIntoView(gridStack);
            notifyListener();
        }
    }

    private void handleChangeForExistingStack(ID id, StackListResult<T> stack, GridStack<T> gridStack) {
        if (!preventSorting) {
            if (!filter.test(gridStack) || !stack.isAvailable()) {
                stacks.remove(gridStack);
                stackIndex.remove(id);
                notifyListener();
            } else if (stack.isAvailable()) {
                stacks.remove(gridStack);
                addIntoView(gridStack);
                notifyListener();
            }
        } else if (!stack.isAvailable()) {
            gridStack.setZeroed(true);
        }
    }

    private void handleChangeForZeroedStack(ID id, StackListResult<T> stack, GridStack<T> oldGridStack) {
        GridStack<T> newStack = stackFactory.apply(stack.getStack());

        stackIndex.put(id, newStack);

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
