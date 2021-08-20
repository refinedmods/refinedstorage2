package com.refinedmods.refinedstorage2.core.util;

import com.refinedmods.refinedstorage2.core.stack.item.ItemStubs;
import com.refinedmods.refinedstorage2.core.stack.item.Rs2ItemStack;
import com.refinedmods.refinedstorage2.test.Rs2Test;

import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import static com.refinedmods.refinedstorage2.core.util.ItemStackAssertions.assertItemStack;
import static com.refinedmods.refinedstorage2.core.util.ItemStackAssertions.assertItemStackListContents;
import static com.refinedmods.refinedstorage2.core.util.ItemStackAssertions.assertOrderedItemStackListContents;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Rs2Test
class ItemStackAssertionsTest {
    @Test
    void Test_equal_item_stack() {
        // Arrange
        Rs2ItemStack a = new Rs2ItemStack(ItemStubs.DIRT, 12, "tag");
        Rs2ItemStack b = new Rs2ItemStack(ItemStubs.DIRT, 12, "tag");

        // Act
        assertItemStack(a, b);
    }

    @Test
    void Test_equal_item_stack_with_no_tag() {
        // Arrange
        Rs2ItemStack a = new Rs2ItemStack(ItemStubs.DIRT, 12);
        Rs2ItemStack b = new Rs2ItemStack(ItemStubs.DIRT, 12);

        // Act
        assertItemStack(a, b);
    }

    @Test
    void Test_equal_item_stack_with_no_count() {
        // Arrange
        Rs2ItemStack a = new Rs2ItemStack(ItemStubs.DIRT);
        Rs2ItemStack b = new Rs2ItemStack(ItemStubs.DIRT);

        // Act
        assertItemStack(a, b);
    }

    @Test
    void Test_not_equal_item_stack_because_of_differing_tag() {
        // Arrange
        Rs2ItemStack a = new Rs2ItemStack(ItemStubs.DIRT, 12, "hello 1");
        Rs2ItemStack b = new Rs2ItemStack(ItemStubs.DIRT, 12, "hello 2");

        // Act
        assertThrows(AssertionFailedError.class, () -> assertItemStack(a, b));
    }

    @Test
    void Test_not_equal_item_stack_because_of_differing_count() {
        // Arrange
        Rs2ItemStack a = new Rs2ItemStack(ItemStubs.DIRT, 1);
        Rs2ItemStack b = new Rs2ItemStack(ItemStubs.DIRT, 2);

        // Act
        assertThrows(AssertionFailedError.class, () -> assertItemStack(a, b));
    }

    @Test
    void Test_not_equal_item_stack_because_of_differing_item() {
        // Arrange
        Rs2ItemStack a = new Rs2ItemStack(ItemStubs.GOLD_BLOCK);
        Rs2ItemStack b = new Rs2ItemStack(ItemStubs.DIRT);

        // Act
        assertThrows(AssertionFailedError.class, () -> assertItemStack(a, b));
    }

    @Test
    void Test_equal_item_stack_lists() {
        // Act
        assertItemStackListContents(Arrays.asList(new Rs2ItemStack(ItemStubs.DIRT, 15), new Rs2ItemStack(ItemStubs.GOLD_BLOCK, 10)), new Rs2ItemStack(ItemStubs.DIRT, 15), new Rs2ItemStack(ItemStubs.GOLD_BLOCK, 10));
    }

    @Test
    void Test_not_equal_item_stack_lists() {
        // Act
        assertThrows(AssertionError.class, () -> assertItemStackListContents(Arrays.asList(new Rs2ItemStack(ItemStubs.DIRT, 15), new Rs2ItemStack(ItemStubs.GOLD_BLOCK, 10)), new Rs2ItemStack(ItemStubs.DIRT, 14), new Rs2ItemStack(ItemStubs.GOLD_BLOCK, 10)));
        assertThrows(AssertionError.class, () -> assertItemStackListContents(Arrays.asList(new Rs2ItemStack(ItemStubs.DIRT, 14), new Rs2ItemStack(ItemStubs.GOLD_BLOCK, 10)), new Rs2ItemStack(ItemStubs.DIRT, 15), new Rs2ItemStack(ItemStubs.GOLD_BLOCK, 10)));
    }

    @Test
    void Test_equal_ordered_item_stack_lists() {
        // Act
        assertOrderedItemStackListContents(Arrays.asList(new Rs2ItemStack(ItemStubs.DIRT, 15), new Rs2ItemStack(ItemStubs.GOLD_BLOCK, 10)), new Rs2ItemStack(ItemStubs.DIRT, 15), new Rs2ItemStack(ItemStubs.GOLD_BLOCK, 10));
    }

    @Test
    void Test_not_equal_ordered_item_stack_lists() {
        // Act
        assertThrows(AssertionError.class, () -> assertOrderedItemStackListContents(Arrays.asList(new Rs2ItemStack(ItemStubs.DIRT, 15), new Rs2ItemStack(ItemStubs.GOLD_BLOCK, 10)), new Rs2ItemStack(ItemStubs.DIRT, 10), new Rs2ItemStack(ItemStubs.GOLD_BLOCK, 15)));
        assertThrows(AssertionError.class, () -> assertOrderedItemStackListContents(Arrays.asList(new Rs2ItemStack(ItemStubs.DIRT, 14), new Rs2ItemStack(ItemStubs.GOLD_BLOCK, 10)), new Rs2ItemStack(ItemStubs.DIRT, 16), new Rs2ItemStack(ItemStubs.GOLD_BLOCK, 10)));
    }
}
