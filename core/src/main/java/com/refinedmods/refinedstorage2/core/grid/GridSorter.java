package com.refinedmods.refinedstorage2.core.grid;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.Comparator;

public enum GridSorter {
    QUANTITY((a, b) -> Integer.compare(a.getStack().getCount(), b.getStack().getCount())),
    NAME((a, b) -> a.getName().compareTo(b.getName())),
    ID((a, b) -> Integer.compare(Item.getRawId(a.getStack().getItem()), Item.getRawId(b.getStack().getItem())));

    private final Comparator<GridStack<ItemStack>> comparator;

    GridSorter(Comparator<GridStack<ItemStack>> comparator) {
        this.comparator = comparator;
    }

    public Comparator<GridStack<ItemStack>> getComparator() {
        return comparator;
    }
}
