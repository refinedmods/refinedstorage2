package com.refinedmods.refinedstorage2.core.grid;

import com.refinedmods.refinedstorage2.core.list.StackList;
import com.refinedmods.refinedstorage2.core.list.item.ItemStackList;
import net.minecraft.item.ItemStack;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class GridView {
    private StackList<ItemStack> list = new ItemStackList();
    private List<ItemStack> stacks = Collections.emptyList();

    public void sort() {
        this.stacks = list.getAll().stream().sorted((a, b) -> Integer.compare(b.getCount(), a.getCount())).collect(Collectors.toList());
    }

    public void loadStack(ItemStack template, int amount) {
        list.add(template, amount);
    }

    public void onChange(ItemStack template, int amount) {
        if (amount < 0) {
            list.remove(template, Math.abs(amount));
        } else {
            list.add(template, amount);
        }
    }

    public List<ItemStack> getStacks() {
        return stacks;
    }
}
