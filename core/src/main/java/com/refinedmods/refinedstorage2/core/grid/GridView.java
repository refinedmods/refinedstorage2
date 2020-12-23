package com.refinedmods.refinedstorage2.core.grid;

import com.refinedmods.refinedstorage2.core.list.StackList;
import com.refinedmods.refinedstorage2.core.list.StackListResult;
import com.refinedmods.refinedstorage2.core.list.item.ItemStackList;
import net.minecraft.item.ItemStack;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GridView {
    private final StackList<ItemStack> list = new ItemStackList();
    private List<ItemStack> stacks = Collections.emptyList();
    private Comparator<ItemStack> sorter = GridSorter.QUANTITY.getComparator();
    private GridSortingDirection sortingDirection = GridSortingDirection.ASCENDING;
    private Runnable listener = () -> {
    };

    public void setListener(Runnable listener) {
        this.listener = listener;
    }

    public void setSorter(Comparator<ItemStack> sorter) {
        this.sorter = sorter;
    }

    private Comparator<ItemStack> getSorter() {
        if (sortingDirection == GridSortingDirection.ASCENDING) {
            return sorter;
        }
        return sorter.reversed();
    }

    public void setSortingDirection(GridSortingDirection sortingDirection) {
        this.sortingDirection = sortingDirection;
    }

    public void loadStack(ItemStack template, int amount) {
        list.add(template, amount);
    }

    public void sort() {
        Stream<ItemStack> newStacks = list.getAll().stream();
        if (sorter != null) {
            newStacks = newStacks.sorted(getSorter());
        }
        this.stacks = newStacks.collect(Collectors.toList());
        this.listener.run();
    }

    public void onChange(ItemStack template, int amount) {
        if (amount < 0) {
            remove(template, Math.abs(amount));
        } else {
            add(template, amount);
        }
    }

    private void add(ItemStack template, int amount) {
        StackListResult<ItemStack> result = list.add(template, amount);

        stacks.remove(result.getStack());
        reposition(result.getStack());
        listener.run();
    }

    private void remove(ItemStack template, int amount) {
        Optional<StackListResult<ItemStack>> result = list.remove(template, amount);

        if (result.isPresent()) {
            ItemStack resultingStack = result.get().getStack();

            stacks.remove(resultingStack);
            if (result.get().isAvailable()) {
                reposition(resultingStack);
            }
            listener.run();
        }
    }

    private void reposition(ItemStack stack) {
        int pos = Collections.binarySearch(stacks, stack, getSorter());
        if (pos < 0) {
            pos = -pos - 1;
        }

        stacks.add(pos, stack);
    }

    public List<ItemStack> getStacks() {
        return stacks;
    }
}
