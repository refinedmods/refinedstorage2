package com.refinedmods.refinedstorage2.core.storage.disk;

import com.refinedmods.refinedstorage2.core.Rs2Test;
import com.refinedmods.refinedstorage2.core.item.ItemStubs;
import com.refinedmods.refinedstorage2.core.item.Rs2ItemStack;
import com.refinedmods.refinedstorage2.core.util.Action;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static com.refinedmods.refinedstorage2.core.util.ItemStackAssertions.assertItemStack;
import static com.refinedmods.refinedstorage2.core.util.ItemStackAssertions.assertItemStackListContents;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Rs2Test
class ItemDiskStorageTest {
    private final ItemDiskStorage disk = new ItemDiskStorage(100);

    @ParameterizedTest
    @EnumSource(Action.class)
    void Test_adding_an_item(Action action) {
        // Arrange
        Rs2ItemStack stack = new Rs2ItemStack(ItemStubs.DIAMOND, 32);

        // Act
        Optional<Rs2ItemStack> remainder = disk.insert(stack, 64, action);

        // Assert
        assertThat(remainder).isEmpty();

        if (action == Action.EXECUTE) {
            assertItemStackListContents(disk.getStacks(), new Rs2ItemStack(ItemStubs.DIAMOND, 64));
            assertThat(disk.getStored()).isEqualTo(64);
        } else {
            assertItemStackListContents(disk.getStacks());
            assertThat(disk.getStored()).isZero();
        }
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void Test_adding_an_item_and_exceeding_capacity(Action action) {
        // Arrange
        Rs2ItemStack stack1 = new Rs2ItemStack(ItemStubs.DIAMOND);
        Rs2ItemStack stack2 = new Rs2ItemStack(ItemStubs.DIRT);

        // Act
        Optional<Rs2ItemStack> remainder1 = disk.insert(stack1, 60, Action.EXECUTE);
        Optional<Rs2ItemStack> remainder2 = disk.insert(stack2, 45, action);

        // Assert
        assertThat(remainder1).isEmpty();

        assertThat(remainder2).isNotEmpty();
        assertThat(remainder2.get()).isNotSameAs(stack1).isNotSameAs(stack2);
        assertItemStack(remainder2.get(), new Rs2ItemStack(ItemStubs.DIRT, 5));

        if (action == Action.EXECUTE) {
            assertItemStackListContents(disk.getStacks(), new Rs2ItemStack(ItemStubs.DIAMOND, 60), new Rs2ItemStack(ItemStubs.DIRT, 40));
            assertThat(disk.getStored()).isEqualTo(100);
        } else {
            assertItemStackListContents(disk.getStacks(), new Rs2ItemStack(ItemStubs.DIAMOND, 60));
            assertThat(disk.getStored()).isEqualTo(60);
        }
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void Test_adding_items_to_an_already_full_disk_and_exceeding_capacity(Action action) {
        // Arrange
        Rs2ItemStack stack = new Rs2ItemStack(ItemStubs.DIAMOND);

        // Act
        Optional<Rs2ItemStack> remainder1 = disk.insert(stack, 100, Action.EXECUTE);
        Optional<Rs2ItemStack> remainder2 = disk.insert(stack, 101, action);

        // Assert
        assertThat(remainder1).isEmpty();

        assertThat(remainder2).isPresent();
        assertItemStack(remainder2.get(), new Rs2ItemStack(ItemStubs.DIAMOND, 101));

        assertItemStackListContents(disk.getStacks(), new Rs2ItemStack(ItemStubs.DIAMOND, 100));
        assertThat(disk.getStored()).isEqualTo(100);
    }

    @Test
    void Test_adding_with_negative_capacity() {
        // Arrange
        ItemDiskStorage diskStorage = new ItemDiskStorage(-1);

        // Act
        Optional<Rs2ItemStack> remainder = diskStorage.insert(new Rs2ItemStack(ItemStubs.DIRT), Integer.MAX_VALUE, Action.EXECUTE);

        // Assert
        assertThat(remainder).isEmpty();
        assertItemStackListContents(diskStorage.getStacks(), new Rs2ItemStack(ItemStubs.DIRT, Integer.MAX_VALUE));
    }

    @Test
    void Test_adding_with_zero_capacity() {
        // Arrange
        ItemDiskStorage diskStorage = new ItemDiskStorage(0);

        // Act
        Optional<Rs2ItemStack> remainder = diskStorage.insert(new Rs2ItemStack(ItemStubs.DIRT), 1, Action.EXECUTE);

        // Assert
        assertThat(remainder).isPresent();
        assertItemStack(remainder.get(), new Rs2ItemStack(ItemStubs.DIRT, 1));
    }

    @Test
    void Test_adding_invalid_item() {
        // Arrange
        Rs2ItemStack stack = Rs2ItemStack.EMPTY;

        // Act
        Executable action = () -> disk.insert(stack, 1, Action.EXECUTE);

        // Assert
        assertThrows(IllegalArgumentException.class, action);
    }

    @Test
    void Test_adding_invalid_item_count() {
        // Arrange
        Rs2ItemStack stack = new Rs2ItemStack(ItemStubs.DIRT);

        // Act
        Executable action1 = () -> disk.insert(stack, 0, Action.EXECUTE);
        Executable action2 = () -> disk.insert(stack, -1, Action.EXECUTE);

        // Assert
        assertThrows(IllegalArgumentException.class, action1);
        assertThrows(IllegalArgumentException.class, action2);
    }

    @Test
    void Test_extracting_non_existent_item() {
        // Arrange
        Rs2ItemStack stack = new Rs2ItemStack(ItemStubs.DIAMOND);

        // Act
        Optional<Rs2ItemStack> result = disk.extract(stack, 1, Action.EXECUTE);

        // Assert
        assertThat(result).isEmpty();

        assertThat(disk.getStored()).isZero();
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void Test_extracting_item_partly(Action action) {
        // Arrange
        Rs2ItemStack stack = new Rs2ItemStack(ItemStubs.DIAMOND);

        // Act
        disk.insert(stack, 32, Action.EXECUTE);
        Optional<Rs2ItemStack> result = disk.extract(stack, 2, action);

        // Assert
        assertThat(result).isNotEmpty();
        assertThat(result.get()).isNotSameAs(stack);

        if (action == Action.EXECUTE) {
            assertItemStack(result.get(), new Rs2ItemStack(ItemStubs.DIAMOND, 2));
            assertItemStackListContents(disk.getStacks(), new Rs2ItemStack(ItemStubs.DIAMOND, 30));
            assertThat(disk.getStored()).isEqualTo(30);
        } else {
            assertItemStack(result.get(), new Rs2ItemStack(ItemStubs.DIAMOND, 2));
            assertItemStackListContents(disk.getStacks(), new Rs2ItemStack(ItemStubs.DIAMOND, 32));
            assertThat(disk.getStored()).isEqualTo(32);
        }
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void Test_extracting_item_completely(Action action) {
        // Arrange
        Rs2ItemStack stack = new Rs2ItemStack(ItemStubs.DIAMOND);

        // Act
        disk.insert(stack, 32, Action.EXECUTE);
        Optional<Rs2ItemStack> result = disk.extract(stack, 32, action);

        // Assert
        assertThat(result).isNotEmpty();
        assertThat(result.get()).isNotSameAs(stack);

        if (action == Action.EXECUTE) {
            assertItemStack(result.get(), new Rs2ItemStack(ItemStubs.DIAMOND, 32));
            assertItemStackListContents(disk.getStacks());
            assertThat(disk.getStored()).isZero();
        } else {
            assertItemStack(result.get(), new Rs2ItemStack(ItemStubs.DIAMOND, 32));
            assertItemStackListContents(disk.getStacks(), new Rs2ItemStack(ItemStubs.DIAMOND, 32));
            assertThat(disk.getStored()).isEqualTo(32);
        }
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void Test_extracting_item_more_than_is_available(Action action) {
        // Arrange
        Rs2ItemStack stack = new Rs2ItemStack(ItemStubs.DIAMOND);

        // Act
        disk.insert(stack, 32, Action.EXECUTE);
        Optional<Rs2ItemStack> result = disk.extract(stack, 33, action);

        // Assert
        assertThat(result).isNotEmpty();
        assertThat(result.get()).isNotSameAs(stack);

        if (action == Action.EXECUTE) {
            assertItemStack(result.get(), new Rs2ItemStack(ItemStubs.DIAMOND, 32));
            assertItemStackListContents(disk.getStacks());
            assertThat(disk.getStored()).isZero();
        } else {
            assertItemStack(result.get(), new Rs2ItemStack(ItemStubs.DIAMOND, 32));
            assertItemStackListContents(disk.getStacks(), new Rs2ItemStack(ItemStubs.DIAMOND, 32));
            assertThat(disk.getStored()).isEqualTo(32);
        }
    }

    @Test
    void Test_extracting_invalid_item() {
        // Arrange
        Rs2ItemStack stack = Rs2ItemStack.EMPTY;

        // Act
        Executable action = () -> disk.extract(stack, 1, Action.EXECUTE);

        // Assert
        assertThrows(IllegalArgumentException.class, action);
    }

    @Test
    void Test_extracting_invalid_item_count() {
        // Arrange
        Rs2ItemStack stack = new Rs2ItemStack(ItemStubs.DIRT);

        // Act
        Executable action1 = () -> disk.extract(stack, 0, Action.EXECUTE);
        Executable action2 = () -> disk.extract(stack, -1, Action.EXECUTE);

        // Assert
        assertThrows(IllegalArgumentException.class, action1);
        assertThrows(IllegalArgumentException.class, action2);
    }
}
