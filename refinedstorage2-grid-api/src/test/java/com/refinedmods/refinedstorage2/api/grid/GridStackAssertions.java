package com.refinedmods.refinedstorage2.api.grid;

import com.refinedmods.refinedstorage2.api.grid.view.stack.GridStack;
import com.refinedmods.refinedstorage2.api.stack.item.Rs2ItemStack;

import java.util.Collection;
import java.util.stream.Collectors;

import static com.refinedmods.refinedstorage2.api.stack.test.ItemStackAssertions.assertItemStackListContents;
import static com.refinedmods.refinedstorage2.api.stack.test.ItemStackAssertions.assertOrderedItemStackListContents;

public final class GridStackAssertions {
    private GridStackAssertions() {
    }

    public static void assertItemGridStackListContents(Collection<GridStack<Rs2ItemStack>> actual, Rs2ItemStack... expected) {
        assertItemStackListContents(actual.stream().map(GridStack::getStack).collect(Collectors.toList()), expected);
    }

    public static void assertOrderedItemGridStackListContents(Collection<GridStack<Rs2ItemStack>> actual, Rs2ItemStack... expected) {
        assertOrderedItemStackListContents(actual.stream().map(GridStack::getStack).collect(Collectors.toList()), expected);
    }
}
