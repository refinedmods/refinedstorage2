package com.refinedmods.refinedstorage2.core.item;

import com.refinedmods.refinedstorage2.core.Rs2Test;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Rs2Test
class Rs2ItemStackTest {
    @Test
    void Test_properties() {
        // Arrange
        Rs2Item item = new ItemStub(10, "Test", 64);

        // Act
        Rs2ItemStack stack = new Rs2ItemStack(item, 10, "tag");

        // Assert
        assertThat(stack.getItem()).isSameAs(item);
        assertThat(stack.getAmount()).isEqualTo(10);
        assertThat(stack.getTag()).isEqualTo("tag");
        assertThat(stack.getMaxCount()).isEqualTo(64);
        assertThat(stack.getName()).isEqualTo("Test");
        assertThat(stack.isEmpty()).isFalse();
    }

    @Test
    void Test_properties_when_empty() {
        // Arrange
        Rs2Item item = new ItemStub(10, "Test", 64);

        // Act
        Rs2ItemStack stack = new Rs2ItemStack(item, 0, "tag");

        // Assert
        assertThat(stack.getItem()).isSameAs(item);
        assertThat(stack.getAmount()).isEqualTo(0);
        assertThat(stack.getTag()).isEqualTo("tag");
        assertThat(stack.getMaxCount()).isEqualTo(64);
        assertThat(stack.getName()).isEqualTo("Test");
        assertThat(stack.isEmpty()).isTrue();
    }

    @Test
    void Test_properties_without_tag() {
        // Arrange
        Rs2Item item = new ItemStub(10, "Test", 64);

        // Act
        Rs2ItemStack stack = new Rs2ItemStack(item, 10);

        // Assert
        assertThat(stack.getItem()).isSameAs(item);
        assertThat(stack.getAmount()).isEqualTo(10);
        assertThat(stack.getTag()).isNull();
        assertThat(stack.getMaxCount()).isEqualTo(64);
        assertThat(stack.getName()).isEqualTo("Test");
        assertThat(stack.isEmpty()).isFalse();
    }

    @Test
    void Test_properties_without_tag_and_count() {
        // Arrange
        Rs2Item item = new ItemStub(10, "Test", 64);

        // Act
        Rs2ItemStack stack = new Rs2ItemStack(item);

        // Assert
        assertThat(stack.getItem()).isSameAs(item);
        assertThat(stack.getAmount()).isEqualTo(1);
        assertThat(stack.getTag()).isNull();
        assertThat(stack.getMaxCount()).isEqualTo(64);
        assertThat(stack.getName()).isEqualTo("Test");
        assertThat(stack.isEmpty()).isFalse();
    }

    @Test
    void Test_copy() {
        // Arrange
        Rs2Item item = new ItemStub(10, "Test", 64);

        // Act
        Rs2ItemStack stack = new Rs2ItemStack(item, 10, "tag");
        Rs2ItemStack copiedStack = stack.copy();

        // Assert
        assertThat(copiedStack.getItem()).isSameAs(item);
        assertThat(copiedStack.getAmount()).isEqualTo(10);
        assertThat(copiedStack.getTag()).isEqualTo("tag");
        assertThat(copiedStack.getMaxCount()).isEqualTo(64);
        assertThat(copiedStack.getName()).isEqualTo("Test");
        assertThat(copiedStack.isEmpty()).isFalse();
        assertThat(copiedStack).isNotSameAs(stack);
    }

    @Test
    void Test_copying_empty() {
        // Arrange
        Rs2ItemStack stack = Rs2ItemStack.EMPTY;

        // Act
        Rs2ItemStack copiedStack = stack.copy();

        // Assert
        assertThat(stack).isSameAs(copiedStack);
    }

    @Test
    void Test_decrement() {
        // Arrange
        Rs2ItemStack stack = new Rs2ItemStack(ItemStubs.COBBLESTONE, 10);

        // Act
        stack.decrement(9);

        // Assert
        assertThat(stack.getAmount()).isEqualTo(1);
        assertThat(stack.isEmpty()).isFalse();
    }

    @Test
    void Test_decrement_should_make_empty() {
        // Arrange
        Rs2ItemStack stack = new Rs2ItemStack(ItemStubs.COBBLESTONE, 10);

        // Act
        stack.decrement(10);

        // Assert
        assertThat(stack.getAmount()).isEqualTo(0);
        assertThat(stack.isEmpty()).isTrue();
    }

    @Test
    void Test_increment() {
        // Arrange
        Rs2ItemStack stack = new Rs2ItemStack(ItemStubs.COBBLESTONE, 10);

        // Act
        stack.increment(3);

        // Assert
        assertThat(stack.getAmount()).isEqualTo(13);
        assertThat(stack.isEmpty()).isFalse();
    }

    @Test
    void Test_increment_should_make_empty() {
        // Arrange
        Rs2ItemStack stack = new Rs2ItemStack(ItemStubs.COBBLESTONE, 10);

        // Act
        stack.increment(-11);

        // Assert
        assertThat(stack.getAmount()).isEqualTo(-1);
        assertThat(stack.isEmpty()).isTrue();
    }

    @Test
    void Test_setting_amount() {
        // Arrange
        Rs2ItemStack stack = new Rs2ItemStack(ItemStubs.COBBLESTONE, 10);

        // Act
        stack.setAmount(1);

        // Assert
        assertThat(stack.getAmount()).isEqualTo(1);
        assertThat(stack.isEmpty()).isFalse();
    }

    @Test
    void Test_setting_amount_should_make_empty() {
        // Arrange
        Rs2ItemStack stack = new Rs2ItemStack(ItemStubs.COBBLESTONE, 10);

        // Act
        stack.setAmount(0);

        // Assert
        assertThat(stack.getAmount()).isEqualTo(0);
        assertThat(stack.isEmpty()).isTrue();
    }
}
