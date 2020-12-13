package com.refinedmods.refinedstorage2.core.grid;

import com.refinedmods.refinedstorage2.core.list.StackList;
import com.refinedmods.refinedstorage2.core.list.StackListResult;
import com.refinedmods.refinedstorage2.core.list.item.ItemStackList;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

// TODO - Add tests.
public class GridView {
    private StackList<ItemStack> list = new ItemStackList();
    private List<ItemStack> stacks = new ArrayList<>();

    public void sort() {
        this.stacks = list.getAll().stream().sorted(getSorter()).collect(Collectors.toList());
    }

    private Comparator<ItemStack> getSorter() {
        return (a, b) -> Integer.compare(b.getCount(), a.getCount());
    }

    public void loadStack(ItemStack template, int amount) {
        list.add(template, amount);
    }

    public void onChange(ItemStack template, int amount) {
        if (amount < 0) {
            boolean allRemoved = !list.remove(template, Math.abs(amount)).isPresent();

            if (allRemoved) {

            }
        } else {
            StackListResult<ItemStack> result = list.add(template, amount);

            // TODO - Add test to ItemStackList that assert that the stack from #getAll() is the same as one in StackListResult#getStack
            int pos = Collections.binarySearch(stacks, result.getStack(), getSorter());
            if (pos < 0) {
                pos = -pos - 1;
            }

            stacks.remove(result.getStack());
            stacks.add(pos, result.getStack());
        }
    }

    public List<ItemStack> getStacks() {
        return stacks;
    }
}
