package com.refinedmods.refinedstorage2.core.grid;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.Comparator;

public enum GridSorter {
    QUANTITY((a, b) -> Integer.compare(a.getCount(), b.getCount())),
    NAME((a, b) -> a.getName().getString().compareTo(b.getName().getString())),
    ID((a, b) -> Integer.compare(Item.getRawId(a.getItem()), Item.getRawId(b.getItem())));

    private final Comparator<ItemStack> comparator;

    GridSorter(Comparator<ItemStack> comparator) {
        this.comparator = comparator;
    }

    public Comparator<ItemStack> getComparator() {
        return comparator;
    }
}
