package com.refinedmods.refinedstorage2.api.storage.composite;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.stack.item.Rs2ItemStack;
import com.refinedmods.refinedstorage2.api.stack.list.StackListImpl;
import com.refinedmods.refinedstorage2.api.stack.test.ItemStubs;
import com.refinedmods.refinedstorage2.api.storage.disk.ItemDiskStorage;
import com.refinedmods.refinedstorage2.test.Rs2Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static com.refinedmods.refinedstorage2.api.stack.test.ItemStackAssertions.assertItemStack;
import static com.refinedmods.refinedstorage2.api.stack.test.ItemStackAssertions.assertItemStackListContents;
import static org.assertj.core.api.Assertions.assertThat;

@Rs2Test
class CompositeStorageTest {
    @Test
    void Test_setting_sources_should_fill_list() {
        // Arrange
        ItemDiskStorage diskStorage1 = new ItemDiskStorage(10);
        diskStorage1.insert(new Rs2ItemStack(ItemStubs.DIRT), 10, Action.EXECUTE);

        ItemDiskStorage diskStorage2 = new ItemDiskStorage(10);
        diskStorage2.insert(new Rs2ItemStack(ItemStubs.GLASS), 5, Action.EXECUTE);

        ItemDiskStorage diskStorage3 = new ItemDiskStorage(10);
        diskStorage3.insert(new Rs2ItemStack(ItemStubs.DIAMOND), 7, Action.EXECUTE);
        diskStorage3.insert(new Rs2ItemStack(ItemStubs.DIRT), 3, Action.EXECUTE);

        // Act
        CompositeStorage channel = new CompositeStorage(Arrays.asList(diskStorage1, diskStorage2, diskStorage3), StackListImpl.createItemStackList());

        // Assert
        assertItemStackListContents(channel.getStacks(), new Rs2ItemStack(ItemStubs.DIRT, 13), new Rs2ItemStack(ItemStubs.GLASS, 5), new Rs2ItemStack(ItemStubs.DIAMOND, 7));
    }

    @Test
    void Test_inserting_without_any_sources_present() {
        // Arrange
        CompositeStorage storage = new CompositeStorage(Collections.emptyList(), StackListImpl.createItemStackList());

        // Act
        Optional<Rs2ItemStack> remainder = storage.insert(new Rs2ItemStack(ItemStubs.DIRT), 10, Action.EXECUTE);

        // Assert
        assertThat(remainder).isPresent();
        assertItemStack(remainder.get(), new Rs2ItemStack(ItemStubs.DIRT, 10));
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void Test_single_source_insert_without_remainder(Action action) {
        // Arrange
        ItemDiskStorage diskStorage = new ItemDiskStorage(20);

        CompositeStorage storage = new CompositeStorage(Collections.singletonList(diskStorage), StackListImpl.createItemStackList());

        // Act
        Optional<Rs2ItemStack> remainder = storage.insert(new Rs2ItemStack(ItemStubs.DIRT), 10, action);

        // Assert
        assertThat(remainder).isEmpty();

        if (action == Action.EXECUTE) {
            assertItemStackListContents(diskStorage.getStacks(), new Rs2ItemStack(ItemStubs.DIRT, 10));

            assertItemStackListContents(storage.getStacks(), new Rs2ItemStack(ItemStubs.DIRT, 10));
            assertThat(storage.getStored()).isEqualTo(10);
        } else {
            assertItemStackListContents(diskStorage.getStacks());

            assertItemStackListContents(storage.getStacks());
            assertThat(storage.getStored()).isZero();
        }
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void Test_single_source_insert_with_remainder(Action action) {
        // Arrange
        ItemDiskStorage diskStorage = new ItemDiskStorage(20);

        CompositeStorage storage = new CompositeStorage(Collections.singletonList(diskStorage), StackListImpl.createItemStackList());

        // Act
        Optional<Rs2ItemStack> remainder = storage.insert(new Rs2ItemStack(ItemStubs.DIRT), 30, action);

        // Assert
        assertThat(remainder).isPresent();
        assertItemStack(remainder.get(), new Rs2ItemStack(ItemStubs.DIRT, 10));

        if (action == Action.EXECUTE) {
            assertItemStackListContents(diskStorage.getStacks(), new Rs2ItemStack(ItemStubs.DIRT, 20));

            assertItemStackListContents(storage.getStacks(), new Rs2ItemStack(ItemStubs.DIRT, 20));
            assertThat(storage.getStored()).isEqualTo(20);
        } else {
            assertItemStackListContents(diskStorage.getStacks());

            assertItemStackListContents(storage.getStacks());
            assertThat(storage.getStored()).isZero();
        }
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void Test_multiple_source_insert_without_remainder(Action action) {
        // Arrange
        ItemDiskStorage diskStorage1 = new ItemDiskStorage(5);
        ItemDiskStorage diskStorage2 = new ItemDiskStorage(10);
        ItemDiskStorage diskStorage3 = new ItemDiskStorage(20);

        CompositeStorage storage = new CompositeStorage(Arrays.asList(diskStorage1, diskStorage2, diskStorage3), StackListImpl.createItemStackList());

        // Act
        Optional<Rs2ItemStack> remainder = storage.insert(new Rs2ItemStack(ItemStubs.DIRT), 17, action);

        // Assert
        assertThat(remainder).isEmpty();

        if (action == Action.EXECUTE) {
            assertItemStackListContents(diskStorage1.getStacks(), new Rs2ItemStack(ItemStubs.DIRT, 5));
            assertItemStackListContents(diskStorage2.getStacks(), new Rs2ItemStack(ItemStubs.DIRT, 10));
            assertItemStackListContents(diskStorage3.getStacks(), new Rs2ItemStack(ItemStubs.DIRT, 2));

            assertItemStackListContents(storage.getStacks(), new Rs2ItemStack(ItemStubs.DIRT, 17));
            assertThat(storage.getStored()).isEqualTo(17);
        } else {
            assertItemStackListContents(diskStorage1.getStacks());
            assertItemStackListContents(diskStorage2.getStacks());
            assertItemStackListContents(diskStorage3.getStacks());

            assertItemStackListContents(storage.getStacks());
            assertThat(storage.getStored()).isZero();
        }
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void Test_multiple_source_insert_with_remainder(Action action) {
        // Arrange
        ItemDiskStorage diskStorage1 = new ItemDiskStorage(5);
        ItemDiskStorage diskStorage2 = new ItemDiskStorage(10);
        ItemDiskStorage diskStorage3 = new ItemDiskStorage(20);

        CompositeStorage storage = new CompositeStorage(Arrays.asList(diskStorage1, diskStorage2, diskStorage3), StackListImpl.createItemStackList());

        // Act
        Optional<Rs2ItemStack> remainder = storage.insert(new Rs2ItemStack(ItemStubs.DIRT), 39, action);

        // Assert
        assertThat(remainder).isPresent();
        assertItemStack(remainder.get(), new Rs2ItemStack(ItemStubs.DIRT, 4));

        if (action == Action.EXECUTE) {
            assertItemStackListContents(diskStorage1.getStacks(), new Rs2ItemStack(ItemStubs.DIRT, 5));
            assertItemStackListContents(diskStorage2.getStacks(), new Rs2ItemStack(ItemStubs.DIRT, 10));
            assertItemStackListContents(diskStorage3.getStacks(), new Rs2ItemStack(ItemStubs.DIRT, 20));

            assertItemStackListContents(storage.getStacks(), new Rs2ItemStack(ItemStubs.DIRT, 35));
            assertThat(storage.getStored()).isEqualTo(35);
        } else {
            assertItemStackListContents(diskStorage1.getStacks());
            assertItemStackListContents(diskStorage2.getStacks());
            assertItemStackListContents(diskStorage3.getStacks());

            assertItemStackListContents(storage.getStacks());
            assertThat(storage.getStored()).isZero();
        }
    }

    @Test
    void Test_extracting_without_any_sources_present() {
        // Arrange
        CompositeStorage storage = new CompositeStorage(Collections.emptyList(), StackListImpl.createItemStackList());

        // Act
        Optional<Rs2ItemStack> result = storage.extract(new Rs2ItemStack(ItemStubs.DIRT), 10, Action.EXECUTE);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void Test_extracting_without_item_present() {
        // Arrange
        ItemDiskStorage diskStorage = new ItemDiskStorage(10);
        diskStorage.insert(new Rs2ItemStack(ItemStubs.DIRT), 10, Action.EXECUTE);

        CompositeStorage storage = new CompositeStorage(Collections.singletonList(diskStorage), StackListImpl.createItemStackList());

        // Act
        Optional<Rs2ItemStack> result = storage.extract(new Rs2ItemStack(ItemStubs.GLASS), 10, Action.EXECUTE);

        // Assert
        assertThat(result).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void Test_single_source_partial_extract(Action action) {
        // Arrange
        ItemDiskStorage diskStorage = new ItemDiskStorage(10);
        diskStorage.insert(new Rs2ItemStack(ItemStubs.DIRT), 10, Action.EXECUTE);

        CompositeStorage storage = new CompositeStorage(Collections.singletonList(diskStorage), StackListImpl.createItemStackList());

        // Act
        Optional<Rs2ItemStack> result = storage.extract(new Rs2ItemStack(ItemStubs.DIRT), 3, action);

        // Assert
        assertThat(result).isPresent();
        assertItemStack(result.get(), new Rs2ItemStack(ItemStubs.DIRT, 3));

        if (action == Action.EXECUTE) {
            assertItemStackListContents(diskStorage.getStacks(), new Rs2ItemStack(ItemStubs.DIRT, 7));

            assertItemStackListContents(storage.getStacks(), new Rs2ItemStack(ItemStubs.DIRT, 7));
            assertThat(storage.getStored()).isEqualTo(7);
        } else {
            assertItemStackListContents(diskStorage.getStacks(), new Rs2ItemStack(ItemStubs.DIRT, 10));

            assertItemStackListContents(storage.getStacks(), new Rs2ItemStack(ItemStubs.DIRT, 10));
            assertThat(storage.getStored()).isEqualTo(10);
        }
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void Test_single_source_full_extract(Action action) {
        // Arrange
        ItemDiskStorage diskStorage = new ItemDiskStorage(10);
        diskStorage.insert(new Rs2ItemStack(ItemStubs.DIRT), 10, Action.EXECUTE);

        CompositeStorage storage = new CompositeStorage(Arrays.asList(diskStorage), StackListImpl.createItemStackList());

        // Act
        Optional<Rs2ItemStack> result = storage.extract(new Rs2ItemStack(ItemStubs.DIRT), 10, action);

        // Assert
        assertThat(result).isPresent();
        assertItemStack(result.get(), new Rs2ItemStack(ItemStubs.DIRT, 10));

        if (action == Action.EXECUTE) {
            assertItemStackListContents(diskStorage.getStacks());

            assertItemStackListContents(storage.getStacks());
            assertThat(storage.getStored()).isZero();
        } else {
            assertItemStackListContents(diskStorage.getStacks(), new Rs2ItemStack(ItemStubs.DIRT, 10));

            assertItemStackListContents(storage.getStacks(), new Rs2ItemStack(ItemStubs.DIRT, 10));
            assertThat(storage.getStored()).isEqualTo(10);
        }
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void Test_single_source_more_than_is_available_extract(Action action) {
        // Arrange
        ItemDiskStorage diskStorage = new ItemDiskStorage(10);
        diskStorage.insert(new Rs2ItemStack(ItemStubs.DIRT), 4, Action.EXECUTE);

        CompositeStorage storage = new CompositeStorage(Collections.singletonList(diskStorage), StackListImpl.createItemStackList());

        // Act
        Optional<Rs2ItemStack> result = storage.extract(new Rs2ItemStack(ItemStubs.DIRT), 7, action);

        // Assert
        assertThat(result).isPresent();
        assertItemStack(result.get(), new Rs2ItemStack(ItemStubs.DIRT, 4));

        if (action == Action.EXECUTE) {
            assertItemStackListContents(diskStorage.getStacks());

            assertItemStackListContents(storage.getStacks());
            assertThat(storage.getStored()).isZero();
        } else {
            assertItemStackListContents(diskStorage.getStacks(), new Rs2ItemStack(ItemStubs.DIRT, 4));

            assertItemStackListContents(storage.getStacks(), new Rs2ItemStack(ItemStubs.DIRT, 4));
            assertThat(storage.getStored()).isEqualTo(4);
        }
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void Test_multiple_source_partial_extract(Action action) {
        // Arrange
        ItemDiskStorage diskStorage1 = new ItemDiskStorage(10);
        diskStorage1.insert(new Rs2ItemStack(ItemStubs.DIRT), 10, Action.EXECUTE);

        ItemDiskStorage diskStorage2 = new ItemDiskStorage(5);
        diskStorage2.insert(new Rs2ItemStack(ItemStubs.DIRT), 3, Action.EXECUTE);

        CompositeStorage storage = new CompositeStorage(Arrays.asList(diskStorage1, diskStorage2), StackListImpl.createItemStackList());

        // Act
        Optional<Rs2ItemStack> result = storage.extract(new Rs2ItemStack(ItemStubs.DIRT), 12, action);

        // Assert
        assertThat(result).isPresent();
        assertItemStack(result.get(), new Rs2ItemStack(ItemStubs.DIRT, 12));

        if (action == Action.EXECUTE) {
            assertItemStackListContents(diskStorage1.getStacks());
            assertItemStackListContents(diskStorage2.getStacks(), new Rs2ItemStack(ItemStubs.DIRT, 1));

            assertItemStackListContents(storage.getStacks(), new Rs2ItemStack(ItemStubs.DIRT, 1));
            assertThat(storage.getStored()).isEqualTo(1);
        } else {
            assertItemStackListContents(diskStorage1.getStacks(), new Rs2ItemStack(ItemStubs.DIRT, 10));
            assertItemStackListContents(diskStorage2.getStacks(), new Rs2ItemStack(ItemStubs.DIRT, 3));

            assertItemStackListContents(storage.getStacks(), new Rs2ItemStack(ItemStubs.DIRT, 13));
            assertThat(storage.getStored()).isEqualTo(13);
        }
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void Test_multiple_source_full_extract(Action action) {
        // Arrange
        ItemDiskStorage diskStorage1 = new ItemDiskStorage(10);
        diskStorage1.insert(new Rs2ItemStack(ItemStubs.DIRT), 10, Action.EXECUTE);

        ItemDiskStorage diskStorage2 = new ItemDiskStorage(5);
        diskStorage2.insert(new Rs2ItemStack(ItemStubs.DIRT), 3, Action.EXECUTE);

        CompositeStorage storage = new CompositeStorage(Arrays.asList(diskStorage1, diskStorage2), StackListImpl.createItemStackList());

        // Act
        Optional<Rs2ItemStack> result = storage.extract(new Rs2ItemStack(ItemStubs.DIRT), 13, action);

        // Assert
        assertThat(result).isPresent();
        assertItemStack(result.get(), new Rs2ItemStack(ItemStubs.DIRT, 13));

        if (action == Action.EXECUTE) {
            assertItemStackListContents(diskStorage1.getStacks());
            assertItemStackListContents(diskStorage2.getStacks());

            assertItemStackListContents(storage.getStacks());
            assertThat(storage.getStored()).isZero();
        } else {
            assertItemStackListContents(diskStorage1.getStacks(), new Rs2ItemStack(ItemStubs.DIRT, 10));
            assertItemStackListContents(diskStorage2.getStacks(), new Rs2ItemStack(ItemStubs.DIRT, 3));

            assertItemStackListContents(storage.getStacks(), new Rs2ItemStack(ItemStubs.DIRT, 13));
            assertThat(storage.getStored()).isEqualTo(13);
        }
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void Test_multiple_source_more_than_is_available_extract(Action action) {
        // Arrange
        ItemDiskStorage diskStorage1 = new ItemDiskStorage(10);
        diskStorage1.insert(new Rs2ItemStack(ItemStubs.DIRT), 10, Action.EXECUTE);

        ItemDiskStorage diskStorage2 = new ItemDiskStorage(5);
        diskStorage2.insert(new Rs2ItemStack(ItemStubs.DIRT), 3, Action.EXECUTE);

        CompositeStorage storage = new CompositeStorage(Arrays.asList(diskStorage1, diskStorage2), StackListImpl.createItemStackList());

        // Act
        Optional<Rs2ItemStack> result = storage.extract(new Rs2ItemStack(ItemStubs.DIRT), 30, action);

        // Assert
        assertThat(result).isPresent();
        assertItemStack(result.get(), new Rs2ItemStack(ItemStubs.DIRT, 13));

        if (action == Action.EXECUTE) {
            assertItemStackListContents(diskStorage1.getStacks());
            assertItemStackListContents(diskStorage2.getStacks());

            assertItemStackListContents(storage.getStacks());
            assertThat(storage.getStored()).isZero();
        } else {
            assertItemStackListContents(diskStorage1.getStacks(), new Rs2ItemStack(ItemStubs.DIRT, 10));
            assertItemStackListContents(diskStorage2.getStacks(), new Rs2ItemStack(ItemStubs.DIRT, 3));

            assertItemStackListContents(storage.getStacks(), new Rs2ItemStack(ItemStubs.DIRT, 13));
            assertThat(storage.getStored()).isEqualTo(13);
        }
    }

    @Test
    void Test_prioritizing_when_inserting() {
        // Arrange
        PrioritizedStorage<Rs2ItemStack> highestPriority = new PrioritizedStorage<>(10, new ItemDiskStorage(10));
        PrioritizedStorage<Rs2ItemStack> lowestPriority = new PrioritizedStorage<>(5, new ItemDiskStorage(10));

        // Act
        CompositeStorage channel = new CompositeStorage(Arrays.asList(lowestPriority, highestPriority), StackListImpl.createItemStackList());

        channel.insert(new Rs2ItemStack(ItemStubs.DIRT), 11, Action.EXECUTE);

        // Assert
        assertItemStackListContents(highestPriority.getStacks(), new Rs2ItemStack(ItemStubs.DIRT, 10));
        assertItemStackListContents(lowestPriority.getStacks(), new Rs2ItemStack(ItemStubs.DIRT, 1));
    }

    @Test
    void Test_prioritizing_when_extracting() {
        // Arrange
        PrioritizedStorage<Rs2ItemStack> highestPriority = new PrioritizedStorage<>(10, new ItemDiskStorage(10));
        PrioritizedStorage<Rs2ItemStack> lowestPriority = new PrioritizedStorage<>(5, new ItemDiskStorage(10));

        highestPriority.insert(new Rs2ItemStack(ItemStubs.DIRT), 10, Action.EXECUTE);
        lowestPriority.insert(new Rs2ItemStack(ItemStubs.DIRT), 5, Action.EXECUTE);

        // Act
        CompositeStorage channel = new CompositeStorage(Arrays.asList(lowestPriority, highestPriority), StackListImpl.createItemStackList());

        channel.extract(new Rs2ItemStack(ItemStubs.DIRT), 11, Action.EXECUTE);

        // Assert
        assertItemStackListContents(highestPriority.getStacks());
        assertItemStackListContents(lowestPriority.getStacks(), new Rs2ItemStack(ItemStubs.DIRT, 4));
    }
}
