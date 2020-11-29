package com.refinedmods.refinedstorage2.core.util;

import com.refinedmods.refinedstorage2.core.list.StackList;
import net.minecraft.item.ItemStack;

import java.util.Collection;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class ItemStackAssertions {
    private ItemStackAssertions() {
    }

    public static void assertItemStackListContents(StackList<ItemStack> actual, ItemStack... expected) {
        assertItemStackListContents(actual.getAll(), expected);
    }

    public static void assertItemStackListContents(Collection<ItemStack> actual, ItemStack... expected) {
        ItemStackWrapper[] wrappers = new ItemStackWrapper[expected.length];
        for (int i = 0; i < expected.length; ++i) {
            wrappers[i] = new ItemStackWrapper(expected[i]);
        }

        assertThat(actual.stream().map(ItemStackWrapper::new).collect(Collectors.toList())).containsExactlyInAnyOrder(wrappers);
    }

    public static void assertItemStack(ItemStack actual, ItemStack expected) {
        assertThat(new ItemStackWrapper(actual)).isEqualTo(new ItemStackWrapper(expected));
    }
}
