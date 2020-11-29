package com.refinedmods.refinedstorage2.core.util;

import com.refinedmods.refinedstorage2.core.RefinedStorage2Test;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import java.util.Arrays;

import static com.refinedmods.refinedstorage2.core.util.ItemStackAssertions.assertItemStack;
import static com.refinedmods.refinedstorage2.core.util.ItemStackAssertions.assertItemStackListContents;
import static org.junit.jupiter.api.Assertions.assertThrows;

@RefinedStorage2Test
class ItemStackAssertionsTest {
    @Test
    void Test_equal_item_stack() {
        // Arrange
        CompoundTag tag = new CompoundTag();
        tag.putInt("a", 123);

        ItemStack a = new ItemStack(Items.DIRT, 12);
        a.setTag(tag.copy());

        ItemStack b = new ItemStack(Items.DIRT, 12);
        b.setTag(tag.copy());

        // Act
        assertItemStack(a, b);
    }

    @Test
    void Test_equal_item_stack_with_no_tag() {
        // Arrange
        ItemStack a = new ItemStack(Items.DIRT, 12);
        ItemStack b = new ItemStack(Items.DIRT, 12);

        // Act
        assertItemStack(a, b);
    }

    @Test
    void Test_equal_item_stack_with_no_count() {
        // Arrange
        ItemStack a = new ItemStack(Items.DIRT);
        ItemStack b = new ItemStack(Items.DIRT);

        // Act
        assertItemStack(a, b);
    }

    @Test
    void Test_not_equal_item_stack_because_of_differing_tag() {
        // Arrange
        CompoundTag tagA = new CompoundTag();
        tagA.putInt("a", 123);
        ItemStack a = new ItemStack(Items.DIRT, 12);
        a.setTag(tagA);

        CompoundTag tagB = new CompoundTag();
        tagB.putInt("a", 124);
        ItemStack b = new ItemStack(Items.DIRT, 12);
        b.setTag(tagB);

        // Act
        assertThrows(AssertionFailedError.class, () -> assertItemStack(a, b));
    }

    @Test
    void Test_not_equal_item_stack_because_of_differing_count() {
        // Arrange
        ItemStack a = new ItemStack(Items.DIRT, 1);
        ItemStack b = new ItemStack(Items.DIRT, 2);

        // Act
        assertThrows(AssertionFailedError.class, () -> assertItemStack(a, b));
    }

    @Test
    void Test_not_equal_item_stack_because_of_differing_item() {
        // Arrange
        ItemStack a = new ItemStack(Items.GOLD_BLOCK);
        ItemStack b = new ItemStack(Items.DIRT);

        // Act
        assertThrows(AssertionFailedError.class, () -> assertItemStack(a, b));
    }

    @Test
    void Test_equal_item_stack_lists() {
        // Act
        assertItemStackListContents(Arrays.asList(new ItemStack(Items.DIRT, 15), new ItemStack(Items.GOLD_BLOCK, 10)), new ItemStack(Items.DIRT, 15), new ItemStack(Items.GOLD_BLOCK, 10));
    }

    @Test
    void Test_not_equal_item_stack_lists() {
        // Act
        assertThrows(AssertionError.class, () -> assertItemStackListContents(Arrays.asList(new ItemStack(Items.DIRT, 15), new ItemStack(Items.GOLD_BLOCK, 10)), new ItemStack(Items.DIRT, 14), new ItemStack(Items.GOLD_BLOCK, 10)));
        assertThrows(AssertionError.class, () -> assertItemStackListContents(Arrays.asList(new ItemStack(Items.DIRT, 14), new ItemStack(Items.GOLD_BLOCK, 10)), new ItemStack(Items.DIRT, 15), new ItemStack(Items.GOLD_BLOCK, 10)));
    }
}
