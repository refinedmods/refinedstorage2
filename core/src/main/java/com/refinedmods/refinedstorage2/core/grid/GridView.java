package com.refinedmods.refinedstorage2.core.grid;

import com.refinedmods.refinedstorage2.core.list.StackList;
import com.refinedmods.refinedstorage2.core.list.StackListResult;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class GridView<T> {
    private final StackList<T> list;
    private final Comparator<GridStack<?>> identitySort;
    private final Function<T, GridStack<T>> stackFactory;
    private final Map<T, GridStack<T>> stackIndex = new HashMap<>();

    private List<GridStack<T>> stacks = new ArrayList<>();
    private Comparator<GridStack<?>> sorter;
    private GridSortingDirection sortingDirection = GridSortingDirection.ASCENDING;
    private Predicate<GridStack<T>> filter = stack -> true;
    private Runnable listener;
    private boolean preventSorting;

    public GridView(Function<T, GridStack<T>> stackFactory, Comparator<GridStack<?>> identitySort, StackList<T> list) {
        this.stackFactory = stackFactory;
        this.identitySort = identitySort;
        this.list = list;
    }

    public void setListener(Runnable listener) {
        this.listener = listener;
    }

    public void setSorter(Comparator<GridStack<?>> sorter) {
        this.sorter = sorter;
    }

    public void setFilter(Predicate<GridStack<T>> filter) {
        this.filter = filter;
    }

    public void setPreventSorting(boolean preventSorting) {
        this.preventSorting = preventSorting;
    }

    public boolean isPreventSorting() {
        return preventSorting;
    }

    private Comparator<GridStack<?>> getSorter() {
        if (sorter == null) {
            return sortingDirection == GridSortingDirection.ASCENDING ? identitySort : identitySort.reversed();
        }

        // An identity sort is necessary so the order of items is preserved in quantity sorting mode.
        // If two grid stacks have the same quantity, their order would not be preserved.
        if (sortingDirection == GridSortingDirection.ASCENDING) {
            return sorter.thenComparing(identitySort);
        }

        return sorter.thenComparing(identitySort).reversed();
    }

    public void setSortingDirection(GridSortingDirection sortingDirection) {
        this.sortingDirection = sortingDirection;
    }

    public void loadStack(T template, int amount) {
        list.add(template, amount);
    }

    public void sort() {
        stackIndex.clear();
        stacks = list
            .getAll()
            .stream()
            .map(stackFactory)
            .sorted(getSorter())
            .filter(filter)
            .map(stack -> {
                stackIndex.put(stack.getStack(), stack);
                return stack;
            })
            .collect(Collectors.toList());

        notifyListener();
    }

    public void onChange(T template, int amount) {
        StackListResult<T> stack;
        if (amount < 0) {
            stack = list.remove(template, Math.abs(amount)).orElseThrow(RuntimeException::new);
        } else {
            stack = list.add(template, amount);
        }

        GridStack<T> gridStack = stackIndex.get(stack.getStack());
        if (gridStack != null) {
            handleChangeForExistingStack(stack, gridStack);
        } else {
            handleChangeForNewStack(stack);
        }
    }

    private void handleChangeForNewStack(StackListResult<T> stack) {
        GridStack<T> gridStack = stackFactory.apply(stack.getStack());
        if (filter.test(gridStack)) {
            stackIndex.put(gridStack.getStack(), gridStack);
            addIntoView(gridStack);
            notifyListener();
        }
    }

    private void handleChangeForExistingStack(StackListResult<T> stack, GridStack<T> gridStack) {
        if (!preventSorting) {
            if (!filter.test(gridStack) || !stack.isAvailable()) {
                stacks.remove(gridStack);
                stackIndex.remove(gridStack.getStack());
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

    private void addIntoView(GridStack<T> stack) {
        int pos = Collections.binarySearch(stacks, stack, getSorter());
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

    public List<GridStack<T>> getStacks() {
        return stacks;
    }
}
