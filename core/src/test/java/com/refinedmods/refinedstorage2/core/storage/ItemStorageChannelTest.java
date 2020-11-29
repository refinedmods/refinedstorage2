package com.refinedmods.refinedstorage2.core.storage;

import com.refinedmods.refinedstorage2.core.RefinedStorage2Test;
import com.refinedmods.refinedstorage2.core.storage.disk.ItemDiskStorage;
import com.refinedmods.refinedstorage2.core.util.Action;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.*;

import static com.refinedmods.refinedstorage2.core.util.ItemStackAssertions.assertItemStack;
import static com.refinedmods.refinedstorage2.core.util.ItemStackAssertions.assertItemStackListContents;
import static org.assertj.core.api.Assertions.assertThat;

@RefinedStorage2Test
class ItemStorageChannelTest {
    @Test
    void Test_setting_sources_should_clear_and_fill_list() {
        // Arrange
        ItemDiskStorage diskStorage1 = new ItemDiskStorage(10);
        diskStorage1.insert(new ItemStack(Items.DIRT), 10, Action.EXECUTE);

        ItemDiskStorage diskStorage2 = new ItemDiskStorage(10);
        diskStorage2.insert(new ItemStack(Items.GLASS), 5, Action.EXECUTE);

        ItemDiskStorage diskStorage3 = new ItemDiskStorage(10);
        diskStorage3.insert(new ItemStack(Items.DIAMOND), 7, Action.EXECUTE);

        ItemStorageChannel channel = new ItemStorageChannel();

        // Act
        Collection<ItemStack> list1 = new ArrayList<>(channel.getList().getAll());

        channel.setSources(createSources(diskStorage3));
        Collection<ItemStack> list2 = new ArrayList<>(channel.getList().getAll());

        channel.setSources(createSources(diskStorage1, diskStorage2));
        Collection<ItemStack> list3 = new ArrayList<>(channel.getList().getAll());

        // Assert
        assertItemStackListContents(list1.stream());
        assertItemStackListContents(list2.stream(), new ItemStack(Items.DIAMOND, 7));
        assertItemStackListContents(list3.stream(), new ItemStack(Items.DIRT, 10), new ItemStack(Items.GLASS, 5));
    }

    @Test
    void Test_inserting_without_any_storage_present() {
        // Arrange
        ItemStorageChannel storageChannel = new ItemStorageChannel();

        // Act
        Optional<ItemStack> remainder = storageChannel.insert(new ItemStack(Items.DIRT), 10, Action.EXECUTE);

        // Assert
        assertThat(remainder).isPresent();
        assertItemStack(remainder.get(), new ItemStack(Items.DIRT, 10));
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void Test_single_source_insert_without_remainder(Action action) {
        // Arrange
        ItemDiskStorage diskStorage = new ItemDiskStorage(20);
        ItemStorageChannel storageChannel = new ItemStorageChannel();
        storageChannel.setSources(createSources(diskStorage));

        // Act
        Optional<ItemStack> remainder = storageChannel.insert(new ItemStack(Items.DIRT), 10, action);

        // Assert
        assertThat(remainder).isEmpty();

        if (action == Action.EXECUTE) {
            assertItemStackListContents(diskStorage.getStacks(), new ItemStack(Items.DIRT, 10));
            assertItemStackListContents(storageChannel.getList(), new ItemStack(Items.DIRT, 10));
        } else {
            assertItemStackListContents(diskStorage.getStacks());
            assertItemStackListContents(storageChannel.getList());
        }
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void Test_single_source_insert_with_remainder(Action action) {
        // Arrange
        ItemDiskStorage diskStorage = new ItemDiskStorage(20);
        ItemStorageChannel storageChannel = new ItemStorageChannel();
        storageChannel.setSources(createSources(diskStorage));

        // Act
        Optional<ItemStack> remainder = storageChannel.insert(new ItemStack(Items.DIRT), 30, action);

        // Assert
        assertThat(remainder).isPresent();
        assertItemStack(remainder.get(), new ItemStack(Items.DIRT, 10));

        if (action == Action.EXECUTE) {
            assertItemStackListContents(diskStorage.getStacks(), new ItemStack(Items.DIRT, 20));
            assertItemStackListContents(storageChannel.getList(), new ItemStack(Items.DIRT, 20));
        } else {
            assertItemStackListContents(diskStorage.getStacks());
            assertItemStackListContents(storageChannel.getList());
        }
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void Test_multiple_source_insert_without_remainder(Action action) {
        // Arrange
        ItemDiskStorage diskStorage1 = new ItemDiskStorage(5);
        ItemDiskStorage diskStorage2 = new ItemDiskStorage(10);
        ItemDiskStorage diskStorage3 = new ItemDiskStorage(20);
        ItemStorageChannel storageChannel = new ItemStorageChannel();
        storageChannel.setSources(createSources(diskStorage1, diskStorage2, diskStorage3));

        // Act
        Optional<ItemStack> remainder = storageChannel.insert(new ItemStack(Items.DIRT), 17, action);

        // Assert
        assertThat(remainder).isEmpty();

        if (action == Action.EXECUTE) {
            assertItemStackListContents(diskStorage1.getStacks(), new ItemStack(Items.DIRT, 5));
            assertItemStackListContents(diskStorage2.getStacks(), new ItemStack(Items.DIRT, 10));
            assertItemStackListContents(diskStorage3.getStacks(), new ItemStack(Items.DIRT, 2));
            assertItemStackListContents(storageChannel.getList(), new ItemStack(Items.DIRT, 17));
        } else {
            assertItemStackListContents(diskStorage1.getStacks());
            assertItemStackListContents(diskStorage2.getStacks());
            assertItemStackListContents(diskStorage3.getStacks());
            assertItemStackListContents(storageChannel.getList());
        }
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void Test_multiple_sources_with_remainder(Action action) {
        // Arrange
        ItemDiskStorage diskStorage1 = new ItemDiskStorage(5);
        ItemDiskStorage diskStorage2 = new ItemDiskStorage(10);
        ItemDiskStorage diskStorage3 = new ItemDiskStorage(20);
        ItemStorageChannel storageChannel = new ItemStorageChannel();
        storageChannel.setSources(createSources(diskStorage1, diskStorage2, diskStorage3));

        // Act
        Optional<ItemStack> remainder = storageChannel.insert(new ItemStack(Items.DIRT), 39, action);

        // Assert
        assertThat(remainder).isPresent();
        assertItemStack(remainder.get(), new ItemStack(Items.DIRT, 4));

        if (action == Action.EXECUTE) {
            assertItemStackListContents(diskStorage1.getStacks(), new ItemStack(Items.DIRT, 5));
            assertItemStackListContents(diskStorage2.getStacks(), new ItemStack(Items.DIRT, 10));
            assertItemStackListContents(diskStorage3.getStacks(), new ItemStack(Items.DIRT, 20));
            assertItemStackListContents(storageChannel.getList(), new ItemStack(Items.DIRT, 35));
        } else {
            assertItemStackListContents(diskStorage1.getStacks());
            assertItemStackListContents(diskStorage2.getStacks());
            assertItemStackListContents(diskStorage3.getStacks());
            assertItemStackListContents(storageChannel.getList());
        }
    }

    @Test
    void Test_extracting_without_any_storage_present() {
        // Arrange
        ItemStorageChannel storageChannel = new ItemStorageChannel();

        // Act
        Optional<ItemStack> result = storageChannel.extract(new ItemStack(Items.DIRT), 10, Action.EXECUTE);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void Test_extracting_without_item_present() {
        // Arrange
        ItemDiskStorage diskStorage = new ItemDiskStorage(10);
        diskStorage.insert(new ItemStack(Items.DIRT), 10, Action.EXECUTE);
        ItemStorageChannel storageChannel = new ItemStorageChannel();
        storageChannel.setSources(createSources(diskStorage));

        // Act
        Optional<ItemStack> result = storageChannel.extract(new ItemStack(Items.GLASS), 10, Action.EXECUTE);

        // Assert
        assertThat(result).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void Test_single_source_partial_extract(Action action) {
        // Arrange
        ItemDiskStorage diskStorage = new ItemDiskStorage(10);
        diskStorage.insert(new ItemStack(Items.DIRT), 10, Action.EXECUTE);
        ItemStorageChannel storageChannel = new ItemStorageChannel();
        storageChannel.setSources(createSources(diskStorage));

        // Act
        Optional<ItemStack> result = storageChannel.extract(new ItemStack(Items.DIRT), 3, action);

        // Assert
        assertThat(result).isPresent();
        assertItemStack(result.get(), new ItemStack(Items.DIRT, 3));

        if (action == Action.EXECUTE) {
            assertItemStackListContents(diskStorage.getStacks(), new ItemStack(Items.DIRT, 7));
            assertItemStackListContents(storageChannel.getList(), new ItemStack(Items.DIRT, 7));
        } else {
            assertItemStackListContents(diskStorage.getStacks(), new ItemStack(Items.DIRT, 10));
            assertItemStackListContents(storageChannel.getList(), new ItemStack(Items.DIRT, 10));
        }
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void Test_single_source_full_extract(Action action) {
        // Arrange
        ItemDiskStorage diskStorage = new ItemDiskStorage(10);
        diskStorage.insert(new ItemStack(Items.DIRT), 10, Action.EXECUTE);
        ItemStorageChannel storageChannel = new ItemStorageChannel();
        storageChannel.setSources(createSources(diskStorage));

        // Act
        Optional<ItemStack> result = storageChannel.extract(new ItemStack(Items.DIRT), 10, action);

        // Assert
        assertThat(result).isPresent();
        assertItemStack(result.get(), new ItemStack(Items.DIRT, 10));

        if (action == Action.EXECUTE) {
            assertItemStackListContents(diskStorage.getStacks());
            assertItemStackListContents(storageChannel.getList());
        } else {
            assertItemStackListContents(diskStorage.getStacks(), new ItemStack(Items.DIRT, 10));
            assertItemStackListContents(storageChannel.getList(), new ItemStack(Items.DIRT, 10));
        }
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void Test_single_source_more_than_is_available_extract(Action action) {
        // Arrange
        ItemDiskStorage diskStorage = new ItemDiskStorage(10);
        diskStorage.insert(new ItemStack(Items.DIRT), 4, Action.EXECUTE);
        ItemStorageChannel storageChannel = new ItemStorageChannel();
        storageChannel.setSources(createSources(diskStorage));

        // Act
        Optional<ItemStack> result = storageChannel.extract(new ItemStack(Items.DIRT), 7, action);

        // Assert
        assertThat(result).isPresent();
        assertItemStack(result.get(), new ItemStack(Items.DIRT, 4));

        if (action == Action.EXECUTE) {
            assertItemStackListContents(diskStorage.getStacks());
            assertItemStackListContents(storageChannel.getList());
        } else {
            assertItemStackListContents(diskStorage.getStacks(), new ItemStack(Items.DIRT, 4));
            assertItemStackListContents(storageChannel.getList(), new ItemStack(Items.DIRT, 4));
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

        ItemStorageChannel storageChannel = new ItemStorageChannel();
        storageChannel.setSources(createSources(diskStorage1, diskStorage2));

        // Act
        Optional<ItemStack> result = storageChannel.extract(new ItemStack(Items.DIRT), 12, action);

        // Assert
        assertThat(result).isPresent();
        assertItemStack(result.get(), new ItemStack(Items.DIRT, 12));

        if (action == Action.EXECUTE) {
            assertItemStackListContents(diskStorage1.getStacks());
            assertItemStackListContents(diskStorage2.getStacks(), new ItemStack(Items.DIRT, 1));
            assertItemStackListContents(storageChannel.getList(), new ItemStack(Items.DIRT, 1));
        } else {
            assertItemStackListContents(diskStorage1.getStacks(), new ItemStack(Items.DIRT, 10));
            assertItemStackListContents(diskStorage2.getStacks(), new ItemStack(Items.DIRT, 3));
            assertItemStackListContents(storageChannel.getList(), new ItemStack(Items.DIRT, 13));
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

        ItemStorageChannel storageChannel = new ItemStorageChannel();
        storageChannel.setSources(createSources(diskStorage1, diskStorage2));

        // Act
        Optional<ItemStack> result = storageChannel.extract(new ItemStack(Items.DIRT), 13, action);

        // Assert
        assertThat(result).isPresent();
        assertItemStack(result.get(), new ItemStack(Items.DIRT, 13));

        if (action == Action.EXECUTE) {
            assertItemStackListContents(diskStorage1.getStacks());
            assertItemStackListContents(diskStorage2.getStacks());
            assertItemStackListContents(storageChannel.getList());
        } else {
            assertItemStackListContents(diskStorage1.getStacks(), new ItemStack(Items.DIRT, 10));
            assertItemStackListContents(diskStorage2.getStacks(), new ItemStack(Items.DIRT, 3));
            assertItemStackListContents(storageChannel.getList(), new ItemStack(Items.DIRT, 13));
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

        ItemStorageChannel storageChannel = new ItemStorageChannel();
        storageChannel.setSources(createSources(diskStorage1, diskStorage2));

        // Act
        Optional<ItemStack> result = storageChannel.extract(new ItemStack(Items.DIRT), 30, action);

        // Assert
        assertThat(result).isPresent();
        assertItemStack(result.get(), new ItemStack(Items.DIRT, 13));

        if (action == Action.EXECUTE) {
            assertItemStackListContents(diskStorage1.getStacks());
            assertItemStackListContents(diskStorage2.getStacks());
            assertItemStackListContents(storageChannel.getList());
        } else {
            assertItemStackListContents(diskStorage1.getStacks(), new ItemStack(Items.DIRT, 10));
            assertItemStackListContents(diskStorage2.getStacks(), new ItemStack(Items.DIRT, 3));
            assertItemStackListContents(storageChannel.getList(), new ItemStack(Items.DIRT, 13));
        }
    }

    private List<Storage<ItemStack>> createSources(Storage... storages) {
        return Arrays.asList(storages);
    }
}
