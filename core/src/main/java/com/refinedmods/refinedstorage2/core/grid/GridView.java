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
    private List<GridStack<ItemStack>> stacks = Collections.emptyList();
    private Comparator<GridStack<ItemStack>> sorter = GridSorter.QUANTITY.getComparator();
    private Predicate<GridStack<ItemStack>> filter = stack -> true;
    private GridSortingDirection sortingDirection = GridSortingDirection.ASCENDING;
    private Runnable listener = () -> {
    };
    private boolean preventSorting;
    private final Function<ItemStack, GridStack<ItemStack>> stackFactory;

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
        this.listener.run();
    }

    public Optional<GridStack<ItemStack>> onChange(ItemStack template, int amount) {
        if (amount < 0) {
            return remove(template, Math.abs(amount));
        } else {
            return add(template, amount);
        }
    }

    private Optional<GridStack<ItemStack>> add(ItemStack template, int amount) {
        StackListResult<ItemStack> result = list.add(template, amount);

        Optional<GridStack<ItemStack>> stack = findStack(result.getStack());
        if (stack.isPresent()) {
            if (!preventSorting) {
                stacks.remove(stack.get());
                addIntoView(stack.get());
                listener.run();
            }
            return stack;
        } else {
            GridStack<ItemStack> newStack = stackFactory.apply(result.getStack());
            if (filter.test(newStack)) {
                addIntoView(newStack);
                listener.run();
                return Optional.of(newStack);
            }
            return Optional.empty();
        }
    }

    private Optional<GridStack<ItemStack>> remove(ItemStack template, int amount) {
        return list.remove(template, amount).flatMap(result -> {
            return findStack(result.getStack()).map(stack -> {
                if (!preventSorting) {
                    stacks.remove(stack);
                    if (result.isAvailable()) {
                        addIntoView(stack);
                    }
                    listener.run();
                } else if (!result.isAvailable()) {
                    stack.setZeroed(true);
                }
                return stack;
            });
        });
    }

    private Optional<GridStack<ItemStack>> findStack(ItemStack stack) {
        return stacks.stream().filter(s -> s.getStack() == stack).findFirst();
    }

    private void addIntoView(GridStack<ItemStack> stack) {
        int pos = Collections.binarySearch(stacks, stack, getSorter());
        if (pos < 0) {
            pos = -pos - 1;
        }

        stacks.add(pos, stack);
    }

    public List<GridStack<ItemStack>> getStacks() {
        return stacks;
    }
}
