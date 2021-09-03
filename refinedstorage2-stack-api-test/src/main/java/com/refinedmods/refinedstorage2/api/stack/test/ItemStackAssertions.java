package com.refinedmods.refinedstorage2.api.stack.test;

import com.refinedmods.refinedstorage2.api.stack.item.Rs2ItemStack;
import com.refinedmods.refinedstorage2.api.stack.list.StackList;

import java.util.Collection;
import java.util.Objects;
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

    public static void assertOrderedItemStackListContents(Collection<Rs2ItemStack> actual, Rs2ItemStack... expected) {
        Rs2ItemStackWrapper[] wrappers = new Rs2ItemStackWrapper[expected.length];
        for (int i = 0; i < expected.length; ++i) {
            wrappers[i] = new Rs2ItemStackWrapper(expected[i]);
        }

        assertThat(actual.stream().map(Rs2ItemStackWrapper::new).collect(Collectors.toList())).containsExactly(wrappers);
    }

    public static void assertItemStack(Rs2ItemStack actual, Rs2ItemStack expected) {
        assertThat(new Rs2ItemStackWrapper(actual)).isEqualTo(new Rs2ItemStackWrapper(expected));
    }

    private record Rs2ItemStackWrapper(Rs2ItemStack stack) {
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Rs2ItemStackWrapper that = (Rs2ItemStackWrapper) o;
            return Objects.equals(stack.getItem(), that.stack.getItem())
                    && Objects.equals(stack.getTag(), that.stack.getTag())
                    && stack.getAmount() == that.stack.getAmount();
        }

        @Override
        public int hashCode() {
            return Objects.hash(stack.getItem(), stack.getTag(), stack.getAmount());
        }

        @Override
        public String toString() {
            return stack.toString();
        }
    }
}
