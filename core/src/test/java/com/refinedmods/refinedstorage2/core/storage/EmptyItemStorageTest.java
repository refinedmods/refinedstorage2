package com.refinedmods.refinedstorage2.core.storage;

import java.util.Collection;
import java.util.Optional;

import com.refinedmods.refinedstorage2.core.RefinedStorage2Test;
import com.refinedmods.refinedstorage2.core.util.Action;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static com.refinedmods.refinedstorage2.core.util.ItemStackAssertions.assertItemStack;
import static org.assertj.core.api.Assertions.assertThat;

@RefinedStorage2Test
class EmptyItemStorageTest {
    private final EmptyItemStorage storage = new EmptyItemStorage();

    @ParameterizedTest
    @EnumSource(Action.class)
    void Test_inserting(Action action) {
        // Act
        Optional<ItemStack> remainder = storage.insert(new ItemStack(Items.DIRT), 100, action);

        // Assert
        assertThat(remainder).isPresent();
        assertItemStack(remainder.get(), new ItemStack(Items.DIRT, 100));
    }

    @Test
    void Test_getting_stacks() {
        // Arrange
        storage.insert(new ItemStack(Items.DIRT), 1, Action.EXECUTE);

        // Act
        Collection<ItemStack> stacks = storage.getStacks();

        // Assert
        assertThat(stacks).isEmpty();
    }

    @Test
    void Test_getting_stored_count() {
        // Arrange
        storage.insert(new ItemStack(Items.DIRT), 1, Action.EXECUTE);

        // Act
        int stored = storage.getStored();

        // Assert
        assertThat(stored).isZero();
    }
}
