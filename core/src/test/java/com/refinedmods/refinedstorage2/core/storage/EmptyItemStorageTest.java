package com.refinedmods.refinedstorage2.core.storage;

import com.refinedmods.refinedstorage2.core.Rs2Test;
import com.refinedmods.refinedstorage2.core.item.ItemStubs;
import com.refinedmods.refinedstorage2.core.item.Rs2ItemStack;
import com.refinedmods.refinedstorage2.core.util.Action;

import java.util.Collection;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static com.refinedmods.refinedstorage2.core.util.ItemStackAssertions.assertItemStack;
import static org.assertj.core.api.Assertions.assertThat;

@Rs2Test
class EmptyItemStorageTest {
    private final EmptyItemStorage storage = new EmptyItemStorage();

    @ParameterizedTest
    @EnumSource(Action.class)
    void Test_inserting(Action action) {
        // Act
        Optional<Rs2ItemStack> remainder = storage.insert(new Rs2ItemStack(ItemStubs.DIRT), 100, action);

        // Assert
        assertThat(remainder).isPresent();
        assertItemStack(remainder.get(), new Rs2ItemStack(ItemStubs.DIRT, 100));
    }

    @Test
    void Test_getting_stacks() {
        // Arrange
        storage.insert(new Rs2ItemStack(ItemStubs.DIRT), 1, Action.EXECUTE);

        // Act
        Collection<Rs2ItemStack> stacks = storage.getStacks();

        // Assert
        assertThat(stacks).isEmpty();
    }

    @Test
    void Test_getting_stored_count() {
        // Arrange
        storage.insert(new Rs2ItemStack(ItemStubs.DIRT), 1, Action.EXECUTE);

        // Act
        long stored = storage.getStored();

        // Assert
        assertThat(stored).isZero();
    }
}
