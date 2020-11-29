package com.refinedmods.refinedstorage2.core.storage;

import com.refinedmods.refinedstorage2.core.RefinedStorage2Test;
import com.refinedmods.refinedstorage2.core.storage.disk.ItemDiskStorage;
import com.refinedmods.refinedstorage2.core.util.Action;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.refinedmods.refinedstorage2.core.util.ItemStackAssertions.assertItemStack;
import static com.refinedmods.refinedstorage2.core.util.ItemStackAssertions.assertItemStackListContents;
import static org.assertj.core.api.Assertions.assertThat;

@RefinedStorage2Test
class ItemStorageChannelTest {
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

    private List<Storage<ItemStack>> createSources(Storage... storages) {
        return Arrays.asList(storages);
    }
}
