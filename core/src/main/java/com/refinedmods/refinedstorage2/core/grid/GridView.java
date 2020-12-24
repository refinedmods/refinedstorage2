package com.refinedmods.refinedstorage2.core.grid;

import com.refinedmods.refinedstorage2.core.list.StackList;
import com.refinedmods.refinedstorage2.core.list.StackListResult;
import com.refinedmods.refinedstorage2.core.list.item.ItemStackList;
import net.minecraft.item.ItemStack;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class GridView {
    private final StackList<ItemStack> list = new ItemStackList();
    private final Function<ItemStack, GridStack<ItemStack>> stackFactory;

    private List<GridStack<ItemStack>> stacks = Collections.emptyList();
    private Comparator<GridStack<ItemStack>> sorter = GridSorter.QUANTITY.getComparator();
    private GridSortingDirection sortingDirection = GridSortingDirection.ASCENDING;
    private Predicate<GridStack<ItemStack>> filter = stack -> true;
    private Runnable listener;
    private boolean preventSorting;

    public GridView(Function<ItemStack, GridStack<ItemStack>> stackFactory) {
        this.stackFactory = stackFactory;
    }

    public void setListener(Runnable listener) {
        this.listener = listener;
    }

    public void setSorter(Comparator<GridStack<ItemStack>> sorter) {
        this.sorter = sorter;
    }

    public void setFilter(Predicate<GridStack<ItemStack>> filter) {
        this.filter = filter;
    }

    public void setPreventSorting(boolean preventSorting) {
        this.preventSorting = preventSorting;
    }

    public boolean isPreventSorting() {
        return preventSorting;
    }

    private Comparator<GridStack<ItemStack>> getSorter() {
        // An identity sort is necessary so the order of items is preserved in quantity sorting mode.
        // If two grid stacks have the same quantity, their order would not be preserved.
        Comparator<GridStack<ItemStack>> identity = GridSorter.NAME.getComparator();
        if (sortingDirection == GridSortingDirection.ASCENDING) {
            return sorter.thenComparing(identity);
        }

        return sorter.thenComparing(identity).reversed();
    }

    public void setSortingDirection(GridSortingDirection sortingDirection) {
        this.sortingDirection = sortingDirection;
    }

    public void loadStack(ItemStack template, int amount) {
        list.add(template, amount);
    }

    public void sort() {
        this.stacks = list
            .getAll()
            .stream()
            .map(stackFactory)
            .sorted(getSorter())
            .filter(filter)
            .collect(Collectors.toList());

        notifyListener();
    }

    public void onChange(ItemStack template, int amount) {
        StackListResult<ItemStack> stack;
        if (amount < 0) {
            stack = list.remove(template, Math.abs(amount)).orElseThrow(RuntimeException::new);
        } else {
            stack = list.add(template, amount);
        }

        Optional<GridStack<ItemStack>> gridStack = findGridStack(stack.getStack());
        if (gridStack.isPresent()) {
            handleChangeForExistingStack(stack, gridStack.get());
        } else {
            handleChangeForNewStack(stack);
        }
    }

    private void handleChangeForNewStack(StackListResult<ItemStack> stack) {
        GridStack<ItemStack> gridStack = stackFactory.apply(stack.getStack());
        if (filter.test(gridStack)) {
            addIntoView(gridStack);
            notifyListener();
        }
    }

    private void handleChangeForExistingStack(StackListResult<ItemStack> stack, GridStack<ItemStack> gridStack) {
        if (!preventSorting) {
            if (!filter.test(gridStack) || !stack.isAvailable()) {
                stacks.remove(gridStack);
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

    private Optional<GridStack<ItemStack>> findGridStack(ItemStack stack) {
        return stacks.stream().filter(s -> s.getStack() == stack).findFirst();
    }

    private void addIntoView(GridStack<ItemStack> stack) {
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

    public List<GridStack<ItemStack>> getStacks() {
        return stacks;
    }
}
