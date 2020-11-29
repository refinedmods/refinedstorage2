package com.refinedmods.refinedstorage2.core.storage;

import com.refinedmods.refinedstorage2.core.RefinedStorage2Test;
import com.refinedmods.refinedstorage2.core.list.item.ItemStackList;
import com.refinedmods.refinedstorage2.core.storage.disk.ItemDiskStorage;
import com.refinedmods.refinedstorage2.core.util.Action;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static com.refinedmods.refinedstorage2.core.util.ItemStackAssertions.assertItemStack;
import static com.refinedmods.refinedstorage2.core.util.ItemStackAssertions.assertItemStackListContents;
import static org.assertj.core.api.Assertions.assertThat;

@RefinedStorage2Test
class CompositeItemStorageTest {
    @Test
    void Test_setting_sources_should_fill_list() {
        // Arrange
        ItemDiskStorage diskStorage1 = new ItemDiskStorage(10);
        diskStorage1.insert(new ItemStack(Items.DIRT), 10, Action.EXECUTE);

        ItemDiskStorage diskStorage2 = new ItemDiskStorage(10);
        diskStorage2.insert(new ItemStack(Items.GLASS), 5, Action.EXECUTE);

        ItemDiskStorage diskStorage3 = new ItemDiskStorage(10);
        diskStorage3.insert(new ItemStack(Items.DIAMOND), 7, Action.EXECUTE);
        diskStorage3.insert(new ItemStack(Items.DIRT), 3, Action.EXECUTE);

        // Act
        CompositeItemStorage channel = new CompositeItemStorage(Arrays.asList(diskStorage1, diskStorage2, diskStorage3), new ItemStackList());

        // Assert
        assertItemStackListContents(channel.getStacks(), new ItemStack(Items.DIRT, 13), new ItemStack(Items.GLASS, 5), new ItemStack(Items.DIAMOND, 7));
    }

    @Test
    void Test_inserting_without_any_sources_present() {
        // Arrange
        CompositeItemStorage storage = new CompositeItemStorage(Collections.emptyList(), new ItemStackList());

        // Act
        Optional<ItemStack> remainder = storage.insert(new ItemStack(Items.DIRT), 10, Action.EXECUTE);

        // Assert
        assertThat(remainder).isPresent();
        assertItemStack(remainder.get(), new ItemStack(Items.DIRT, 10));
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void Test_single_source_insert_without_remainder(Action action) {
        // Arrange
        ItemDiskStorage diskStorage = new ItemDiskStorage(20);

        CompositeItemStorage storage = new CompositeItemStorage(Arrays.asList(diskStorage), new ItemStackList());

        // Act
        Optional<ItemStack> remainder = storage.insert(new ItemStack(Items.DIRT), 10, action);

        // Assert
        assertThat(remainder).isEmpty();

        if (action == Action.EXECUTE) {
            assertItemStackListContents(diskStorage.getStacks(), new ItemStack(Items.DIRT, 10));

            assertItemStackListContents(storage.getStacks(), new ItemStack(Items.DIRT, 10));
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

        CompositeItemStorage storage = new CompositeItemStorage(Arrays.asList(diskStorage), new ItemStackList());

        // Act
        Optional<ItemStack> remainder = storage.insert(new ItemStack(Items.DIRT), 30, action);

        // Assert
        assertThat(remainder).isPresent();
        assertItemStack(remainder.get(), new ItemStack(Items.DIRT, 10));

        if (action == Action.EXECUTE) {
            assertItemStackListContents(diskStorage.getStacks(), new ItemStack(Items.DIRT, 20));

            assertItemStackListContents(storage.getStacks(), new ItemStack(Items.DIRT, 20));
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

        CompositeItemStorage storage = new CompositeItemStorage(Arrays.asList(diskStorage1, diskStorage2, diskStorage3), new ItemStackList());

        // Act
        Optional<ItemStack> remainder = storage.insert(new ItemStack(Items.DIRT), 17, action);

        // Assert
        assertThat(remainder).isEmpty();

        if (action == Action.EXECUTE) {
            assertItemStackListContents(diskStorage1.getStacks(), new ItemStack(Items.DIRT, 5));
            assertItemStackListContents(diskStorage2.getStacks(), new ItemStack(Items.DIRT, 10));
            assertItemStackListContents(diskStorage3.getStacks(), new ItemStack(Items.DIRT, 2));

            assertItemStackListContents(storage.getStacks(), new ItemStack(Items.DIRT, 17));
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

        CompositeItemStorage storage = new CompositeItemStorage(Arrays.asList(diskStorage1, diskStorage2, diskStorage3), new ItemStackList());

        // Act
        Optional<ItemStack> remainder = storage.insert(new ItemStack(Items.DIRT), 39, action);

        // Assert
        assertThat(remainder).isPresent();
        assertItemStack(remainder.get(), new ItemStack(Items.DIRT, 4));

        if (action == Action.EXECUTE) {
            assertItemStackListContents(diskStorage1.getStacks(), new ItemStack(Items.DIRT, 5));
            assertItemStackListContents(diskStorage2.getStacks(), new ItemStack(Items.DIRT, 10));
            assertItemStackListContents(diskStorage3.getStacks(), new ItemStack(Items.DIRT, 20));

            assertItemStackListContents(storage.getStacks(), new ItemStack(Items.DIRT, 35));
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
        CompositeItemStorage storage = new CompositeItemStorage(Collections.emptyList(), new ItemStackList());

        // Act
        Optional<ItemStack> result = storage.extract(new ItemStack(Items.DIRT), 10, Action.EXECUTE);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void Test_extracting_without_item_present() {
        // Arrange
        ItemDiskStorage diskStorage = new ItemDiskStorage(10);
        diskStorage.insert(new ItemStack(Items.DIRT), 10, Action.EXECUTE);

        CompositeItemStorage storage = new CompositeItemStorage(Arrays.asList(diskStorage), new ItemStackList());

        // Act
        Optional<ItemStack> result = storage.extract(new ItemStack(Items.GLASS), 10, Action.EXECUTE);

        // Assert
        assertThat(result).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void Test_single_source_partial_extract(Action action) {
        // Arrange
        ItemDiskStorage diskStorage = new ItemDiskStorage(10);
        diskStorage.insert(new ItemStack(Items.DIRT), 10, Action.EXECUTE);

        CompositeItemStorage storage = new CompositeItemStorage(Arrays.asList(diskStorage), new ItemStackList());

        // Act
        Optional<ItemStack> result = storage.extract(new ItemStack(Items.DIRT), 3, action);

        // Assert
        assertThat(result).isPresent();
        assertItemStack(result.get(), new ItemStack(Items.DIRT, 3));

        if (action == Action.EXECUTE) {
            assertItemStackListContents(diskStorage.getStacks(), new ItemStack(Items.DIRT, 7));

            assertItemStackListContents(storage.getStacks(), new ItemStack(Items.DIRT, 7));
            assertThat(storage.getStored()).isEqualTo(7);
        } else {
            assertItemStackListContents(diskStorage.getStacks(), new ItemStack(Items.DIRT, 10));

            assertItemStackListContents(storage.getStacks(), new ItemStack(Items.DIRT, 10));
            assertThat(storage.getStored()).isEqualTo(10);
        }
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void Test_single_source_full_extract(Action action) {
        // Arrange
        ItemDiskStorage diskStorage = new ItemDiskStorage(10);
        diskStorage.insert(new ItemStack(Items.DIRT), 10, Action.EXECUTE);

        CompositeItemStorage storage = new CompositeItemStorage(Arrays.asList(diskStorage), new ItemStackList());

        // Act
        Optional<ItemStack> result = storage.extract(new ItemStack(Items.DIRT), 10, action);

        // Assert
        assertThat(result).isPresent();
        assertItemStack(result.get(), new ItemStack(Items.DIRT, 10));

        if (action == Action.EXECUTE) {
            assertItemStackListContents(diskStorage.getStacks());

            assertItemStackListContents(storage.getStacks());
            assertThat(storage.getStored()).isZero();
        } else {
            assertItemStackListContents(diskStorage.getStacks(), new ItemStack(Items.DIRT, 10));

            assertItemStackListContents(storage.getStacks(), new ItemStack(Items.DIRT, 10));
            assertThat(storage.getStored()).isEqualTo(10);
        }
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void Test_single_source_more_than_is_available_extract(Action action) {
        // Arrange
        ItemDiskStorage diskStorage = new ItemDiskStorage(10);
        diskStorage.insert(new ItemStack(Items.DIRT), 4, Action.EXECUTE);

        CompositeItemStorage storage = new CompositeItemStorage(Arrays.asList(diskStorage), new ItemStackList());

        // Act
        Optional<ItemStack> result = storage.extract(new ItemStack(Items.DIRT), 7, action);

        // Assert
        assertThat(result).isPresent();
        assertItemStack(result.get(), new ItemStack(Items.DIRT, 4));

        if (action == Action.EXECUTE) {
            assertItemStackListContents(diskStorage.getStacks());

            assertItemStackListContents(storage.getStacks());
            assertThat(storage.getStored()).isZero();
        } else {
            assertItemStackListContents(diskStorage.getStacks(), new ItemStack(Items.DIRT, 4));

            assertItemStackListContents(storage.getStacks(), new ItemStack(Items.DIRT, 4));
            assertThat(storage.getStored()).isEqualTo(4);
        }
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void Test_multiple_source_partial_extract(Action action) {
        // Arrange
        ItemDiskStorage diskStorage1 = new ItemDiskStorage(10);
        diskStorage1.insert(new ItemStack(Items.DIRT), 10, Action.EXECUTE);

        ItemDiskStorage diskStorage2 = new ItemDiskStorage(5);
        diskStorage2.insert(new ItemStack(Items.DIRT), 3, Action.EXECUTE);

        CompositeItemStorage storage = new CompositeItemStorage(Arrays.asList(diskStorage1, diskStorage2), new ItemStackList());

        // Act
        Optional<ItemStack> result = storage.extract(new ItemStack(Items.DIRT), 12, action);

        // Assert
        assertThat(result).isPresent();
        assertItemStack(result.get(), new ItemStack(Items.DIRT, 12));

        if (action == Action.EXECUTE) {
            assertItemStackListContents(diskStorage1.getStacks());
            assertItemStackListContents(diskStorage2.getStacks(), new ItemStack(Items.DIRT, 1));

            assertItemStackListContents(storage.getStacks(), new ItemStack(Items.DIRT, 1));
            assertThat(storage.getStored()).isEqualTo(1);
        } else {
            assertItemStackListContents(diskStorage1.getStacks(), new ItemStack(Items.DIRT, 10));
            assertItemStackListContents(diskStorage2.getStacks(), new ItemStack(Items.DIRT, 3));

            assertItemStackListContents(storage.getStacks(), new ItemStack(Items.DIRT, 13));
            assertThat(storage.getStored()).isEqualTo(13);
        }
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void Test_multiple_source_full_extract(Action action) {
        // Arrange
        ItemDiskStorage diskStorage1 = new ItemDiskStorage(10);
        diskStorage1.insert(new ItemStack(Items.DIRT), 10, Action.EXECUTE);

        ItemDiskStorage diskStorage2 = new ItemDiskStorage(5);
        diskStorage2.insert(new ItemStack(Items.DIRT), 3, Action.EXECUTE);

        CompositeItemStorage storage = new CompositeItemStorage(Arrays.asList(diskStorage1, diskStorage2), new ItemStackList());

        // Act
        Optional<ItemStack> result = storage.extract(new ItemStack(Items.DIRT), 13, action);

        // Assert
        assertThat(result).isPresent();
        assertItemStack(result.get(), new ItemStack(Items.DIRT, 13));

        if (action == Action.EXECUTE) {
            assertItemStackListContents(diskStorage1.getStacks());
            assertItemStackListContents(diskStorage2.getStacks());

            assertItemStackListContents(storage.getStacks());
            assertThat(storage.getStored()).isZero();
        } else {
            assertItemStackListContents(diskStorage1.getStacks(), new ItemStack(Items.DIRT, 10));
            assertItemStackListContents(diskStorage2.getStacks(), new ItemStack(Items.DIRT, 3));

            assertItemStackListContents(storage.getStacks(), new ItemStack(Items.DIRT, 13));
            assertThat(storage.getStored()).isEqualTo(13);
        }
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void Test_multiple_source_more_than_is_available_extract(Action action) {
        // Arrange
        ItemDiskStorage diskStorage1 = new ItemDiskStorage(10);
        diskStorage1.insert(new ItemStack(Items.DIRT), 10, Action.EXECUTE);

        ItemDiskStorage diskStorage2 = new ItemDiskStorage(5);
        diskStorage2.insert(new ItemStack(Items.DIRT), 3, Action.EXECUTE);

        CompositeItemStorage storage = new CompositeItemStorage(Arrays.asList(diskStorage1, diskStorage2), new ItemStackList());

        // Act
        Optional<ItemStack> result = storage.extract(new ItemStack(Items.DIRT), 30, action);

        // Assert
        assertThat(result).isPresent();
        assertItemStack(result.get(), new ItemStack(Items.DIRT, 13));

        if (action == Action.EXECUTE) {
            assertItemStackListContents(diskStorage1.getStacks());
            assertItemStackListContents(diskStorage2.getStacks());

            assertItemStackListContents(storage.getStacks());
            assertThat(storage.getStored()).isZero();
        } else {
            assertItemStackListContents(diskStorage1.getStacks(), new ItemStack(Items.DIRT, 10));
            assertItemStackListContents(diskStorage2.getStacks(), new ItemStack(Items.DIRT, 3));

            assertItemStackListContents(storage.getStacks(), new ItemStack(Items.DIRT, 13));
            assertThat(storage.getStored()).isEqualTo(13);
        }
    }
}
