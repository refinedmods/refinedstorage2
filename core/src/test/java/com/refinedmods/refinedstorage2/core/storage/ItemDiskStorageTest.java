package com.refinedmods.refinedstorage2.core.storage;

import com.refinedmods.refinedstorage2.core.Action;
import com.refinedmods.refinedstorage2.core.RefinedStorage2Test;
import com.refinedmods.refinedstorage2.core.util.ItemStackAssertions;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.util.Optional;

import static com.refinedmods.refinedstorage2.core.util.ItemStackAssertions.assertItemStack;
import static com.refinedmods.refinedstorage2.core.util.ItemStackAssertions.assertItemStackListContents;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@RefinedStorage2Test
class ItemDiskStorageTest {
    private final ItemDiskStorage disk = new ItemDiskStorage(100);

    @Test
    void Test_adding_an_item() {
        // Arrange
        ItemStack stack = new ItemStack(Items.DIAMOND, 32);

        // Act
        Optional<ItemStack> remainder = disk.insert(stack, 64, Action.EXECUTE);

        // Assert
        assertThat(remainder).isEmpty();

        ItemStackAssertions.assertItemStackListContents(disk.getStacks(), new ItemStack(Items.DIAMOND, 64));

        assertThat(disk.getStored()).isEqualTo(64);
    }

    @Test
    void Test_adding_an_item_simulated() {
        // Arrange
        ItemStack stack = new ItemStack(Items.DIAMOND, 32);

        // Act
        Optional<ItemStack> remainder = disk.insert(stack, 64, Action.SIMULATE);

        // Assert
        assertThat(remainder).isEmpty();

        ItemStackAssertions.assertItemStackListContents(disk.getStacks());

        assertThat(disk.getStored()).isZero();
    }

    @Test
    void Test_adding_an_item_but_exceeding_capacity() {
        // Arrange
        ItemStack stack1 = new ItemStack(Items.DIAMOND);
        ItemStack stack2 = new ItemStack(Items.DIRT);

        // Act
        Optional<ItemStack> remainder1 = disk.insert(stack1, 60, Action.EXECUTE);
        Optional<ItemStack> remainder2 = disk.insert(stack2, 45, Action.EXECUTE);

        // Assert
        assertThat(remainder1).isEmpty();
        assertThat(remainder2).isNotEmpty();
        assertThat(remainder2.get()).isNotSameAs(stack1).isNotSameAs(stack2);

        assertItemStack(remainder2.get(), new ItemStack(Items.DIRT, 5));
        ItemStackAssertions.assertItemStackListContents(disk.getStacks(), new ItemStack(Items.DIAMOND, 60), new ItemStack(Items.DIRT, 40));

        assertThat(disk.getStored()).isEqualTo(100);
    }

    @Test
    void Test_adding_an_item_but_exceeding_capacity_simulated() {
        // Arrange
        ItemStack stack1 = new ItemStack(Items.DIAMOND);
        ItemStack stack2 = new ItemStack(Items.DIRT);

        // Act
        Optional<ItemStack> remainder1 = disk.insert(stack1, 60, Action.EXECUTE);
        Optional<ItemStack> remainder2 = disk.insert(stack2, 45, Action.SIMULATE);

        // Assert
        assertThat(remainder1).isEmpty();
        assertThat(remainder2).isNotEmpty();
        assertThat(remainder2.get()).isNotSameAs(stack1).isNotSameAs(stack2);

        assertItemStack(remainder2.get(), new ItemStack(Items.DIRT, 5));
        ItemStackAssertions.assertItemStackListContents(disk.getStacks(), new ItemStack(Items.DIAMOND, 60));

        assertThat(disk.getStored()).isEqualTo(60);
    }

    @Test
    void Test_adding_invalid_item() {
        // Arrange
        ItemStack stack = ItemStack.EMPTY;

        // Act
        Executable action = () -> disk.insert(stack, 1, Action.EXECUTE);

        // Assert
        assertThrows(IllegalArgumentException.class, action);
    }

    @Test
    void Test_adding_invalid_item_count() {
        // Arrange
        ItemStack stack = new ItemStack(Items.DIRT);

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
        ItemStack stack = new ItemStack(Items.DIAMOND);

        // Act
        Optional<ItemStack> result = disk.extract(stack, 1, Action.EXECUTE);

        // Assert
        assertThat(result).isEmpty();

        assertThat(disk.getStored()).isZero();
    }

    @Test
    void Test_extracting_item_partly() {
        // Arrange
        ItemStack stack = new ItemStack(Items.DIAMOND);

        // Act
        disk.insert(stack, 32, Action.EXECUTE);
        Optional<ItemStack> result = disk.extract(stack, 2, Action.EXECUTE);

        // Assert
        assertThat(result).isNotEmpty();
        assertThat(result.get()).isNotSameAs(stack);

        assertItemStack(result.get(), new ItemStack(Items.DIAMOND, 2));
        ItemStackAssertions.assertItemStackListContents(disk.getStacks(), new ItemStack(Items.DIAMOND, 30));

        assertThat(disk.getStored()).isEqualTo(30);
    }

    @Test
    void Test_extracting_item_completely() {
        // Arrange
        ItemStack stack = new ItemStack(Items.DIAMOND);

        // Act
        disk.insert(stack, 32, Action.EXECUTE);
        Optional<ItemStack> result = disk.extract(stack, 32, Action.EXECUTE);

        // Assert
        assertThat(result).isNotEmpty();
        assertThat(result.get()).isNotSameAs(stack);

        assertItemStack(result.get(), new ItemStack(Items.DIAMOND, 32));
        ItemStackAssertions.assertItemStackListContents(disk.getStacks());

        assertThat(disk.getStored()).isZero();
    }

    @Test
    void Test_extracting_item_more_than_is_available() {
        // Arrange
        ItemStack stack = new ItemStack(Items.DIAMOND);

        // Act
        disk.insert(stack, 32, Action.EXECUTE);
        Optional<ItemStack> result = disk.extract(stack, 33, Action.EXECUTE);

        // Assert
        assertThat(result).isNotEmpty();
        assertThat(result.get()).isNotSameAs(stack);

        assertItemStack(result.get(), new ItemStack(Items.DIAMOND, 32));
        ItemStackAssertions.assertItemStackListContents(disk.getStacks());

        assertThat(disk.getStored()).isZero();
    }

    @Test
    void Test_extracting_item_partly_simulated() {
        // Arrange
        ItemStack stack = new ItemStack(Items.DIAMOND);

        // Act
        disk.insert(stack, 32, Action.EXECUTE);
        Optional<ItemStack> result = disk.extract(stack, 2, Action.SIMULATE);

        // Assert
        assertThat(result).isNotEmpty();
        assertThat(result.get()).isNotSameAs(stack);

        assertItemStack(result.get(), new ItemStack(Items.DIAMOND, 2));
        ItemStackAssertions.assertItemStackListContents(disk.getStacks(), new ItemStack(Items.DIAMOND, 32));

        assertThat(disk.getStored()).isEqualTo(32);
    }

    @Test
    void Test_extracting_item_completely_simulated() {
        // Arrange
        ItemStack stack = new ItemStack(Items.DIAMOND);

        // Act
        disk.insert(stack, 32, Action.EXECUTE);
        Optional<ItemStack> result = disk.extract(stack, 32, Action.SIMULATE);

        // Assert
        assertThat(result).isNotEmpty();
        assertThat(result.get()).isNotSameAs(stack);

        assertItemStack(result.get(), new ItemStack(Items.DIAMOND, 32));
        ItemStackAssertions.assertItemStackListContents(disk.getStacks(), new ItemStack(Items.DIAMOND, 32));

        assertThat(disk.getStored()).isEqualTo(32);
    }

    @Test
    void Test_extracting_item_more_than_is_available_simulated() {
        // Arrange
        ItemStack stack = new ItemStack(Items.DIAMOND);

        // Act
        disk.insert(stack, 32, Action.EXECUTE);
        Optional<ItemStack> result = disk.extract(stack, 33, Action.SIMULATE);

        // Assert
        assertThat(result).isNotEmpty();
        assertThat(result.get()).isNotSameAs(stack);

        assertItemStack(result.get(), new ItemStack(Items.DIAMOND, 32));
        ItemStackAssertions.assertItemStackListContents(disk.getStacks(), new ItemStack(Items.DIAMOND, 32));

        assertThat(disk.getStored()).isEqualTo(32);
    }

    @Test
    void Test_extracting_invalid_item() {
        // Arrange
        ItemStack stack = ItemStack.EMPTY;

        // Act
        Executable action = () -> disk.extract(stack, 1, Action.EXECUTE);

        // Assert
        assertThrows(IllegalArgumentException.class, action);
    }

    @Test
    void Test_extracting_invalid_item_count() {
        // Arrange
        ItemStack stack = new ItemStack(Items.DIRT);

        // Act
        Executable action1 = () -> disk.extract(stack, 0, Action.EXECUTE);
        Executable action2 = () -> disk.extract(stack, -1, Action.EXECUTE);

        // Assert
        assertThrows(IllegalArgumentException.class, action1);
        assertThrows(IllegalArgumentException.class, action2);
    }
}
