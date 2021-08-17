package com.refinedmods.refinedstorage2.core.util;

import com.refinedmods.refinedstorage2.core.grid.GridStack;
import com.refinedmods.refinedstorage2.core.list.StackList;
import com.refinedmods.refinedstorage2.core.stack.item.Rs2ItemStack;

import java.util.Collection;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class ItemStackAssertions {
    private ItemStackAssertions() {
    }

    public static void assertItemStackListContents(StackList<Rs2ItemStack> actual, Rs2ItemStack... expected) {
        assertItemStackListContents(actual.getAll(), expected);
    }

    public static void assertItemStackListContents(Collection<Rs2ItemStack> actual, Rs2ItemStack... expected) {
        Rs2ItemStackWrapper[] wrappers = new Rs2ItemStackWrapper[expected.length];
        for (int i = 0; i < expected.length; ++i) {
            wrappers[i] = new Rs2ItemStackWrapper(expected[i]);
        }

        assertThat(actual.stream().map(Rs2ItemStackWrapper::new).collect(Collectors.toList())).containsExactlyInAnyOrder(wrappers);
    }

    public static void assertItemGridStackListContents(Collection<GridStack<Rs2ItemStack>> actual, Rs2ItemStack... expected) {
        assertItemStackListContents(actual.stream().map(GridStack::getStack).collect(Collectors.toList()), expected);
    }

    public static void assertOrderedItemStackListContents(Collection<Rs2ItemStack> actual, Rs2ItemStack... expected) {
        Rs2ItemStackWrapper[] wrappers = new Rs2ItemStackWrapper[expected.length];
        for (int i = 0; i < expected.length; ++i) {
            wrappers[i] = new Rs2ItemStackWrapper(expected[i]);
        }

        assertThat(actual.stream().map(Rs2ItemStackWrapper::new).collect(Collectors.toList())).containsExactly(wrappers);
    }

    public static void assertOrderedItemGridStackListContents(Collection<GridStack<Rs2ItemStack>> actual, Rs2ItemStack... expected) {
        assertOrderedItemStackListContents(actual.stream().map(GridStack::getStack).collect(Collectors.toList()), expected);
    }

    public static void assertItemStack(Rs2ItemStack actual, Rs2ItemStack expected) {
        assertThat(new Rs2ItemStackWrapper(actual)).isEqualTo(new Rs2ItemStackWrapper(expected));
    }
}
