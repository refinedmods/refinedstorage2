package com.refinedmods.refinedstorage2.core.util;

import com.refinedmods.refinedstorage2.core.list.StackList;
import net.minecraft.item.ItemStack;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class ItemStackAssertions {
    private ItemStackAssertions() {
    }

    public static void assertItemStackListContents(StackList<ItemStack> actual, ItemStack... expected) {
        assertItemStackListContents(actual.getAll().stream(), expected);
    }

    public static void assertItemStackListContents(Stream<ItemStack> actual, ItemStack... expected) {
        ItemStackWrapper[] wrappers = new ItemStackWrapper[expected.length];
        for (int i = 0; i < expected.length; ++i) {
            wrappers[i] = new ItemStackWrapper(expected[i]);
        }

        assertThat(actual.map(ItemStackWrapper::new).collect(Collectors.toList())).containsExactlyInAnyOrder(wrappers);
    }

    public static void assertItemStack(ItemStack actual, ItemStack expected) {
        assertThat(new ItemStackWrapper(actual)).isEqualTo(new ItemStackWrapper(expected));
    }
}
