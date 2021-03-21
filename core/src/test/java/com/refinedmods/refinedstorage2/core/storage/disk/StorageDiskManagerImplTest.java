package com.refinedmods.refinedstorage2.core.storage.disk;

import com.refinedmods.refinedstorage2.core.RefinedStorage2Test;
import com.refinedmods.refinedstorage2.core.util.Action;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@RefinedStorage2Test
class StorageDiskManagerImplTest {
    private final StorageDiskManagerImpl storageDiskManager = new StorageDiskManagerImpl();

    @Test
    void Test_whether_getting_non_existent_disk_is_not_present() {
        // Assert
        assertThat(storageDiskManager.getDisk(UUID.randomUUID())).isEmpty();
    }

    @Test
    void Test_whether_getting_disk_is_present() {
        // Arrange
        UUID id = UUID.randomUUID();
        StorageDisk<ItemStack> storage = new ItemDiskStorage(1);

        // Act
        storageDiskManager.setDisk(id, storage);
        Optional<StorageDisk<ItemStack>> foundStorage = storageDiskManager.getDisk(id);

        Optional<StorageDisk<ItemStack>> nonExistent = storageDiskManager.getDisk(UUID.randomUUID());

        // Assert
        assertThat(foundStorage).isNotEmpty();
        assertThat(foundStorage.get()).isSameAs(storage);

        assertThat(nonExistent).isEmpty();
    }

    @Test
    void Test_getting_info_of_disk() {
        // Arrange
        UUID id = UUID.randomUUID();
        StorageDisk<ItemStack> storage = new ItemDiskStorage(10);
        storage.insert(new ItemStack(Items.DIRT), 5, Action.EXECUTE);

        // Act
        storageDiskManager.setDisk(id, storage);

        StorageDiskInfo info = storageDiskManager.getInfo(id);

        // Assert
        assertThat(info.getCapacity()).isEqualTo(10);
        assertThat(info.getStored()).isEqualTo(5);
    }

    @Test
    void Test_getting_info_of_non_existent_disk() {
        // Act
        StorageDiskInfo info = storageDiskManager.getInfo(UUID.randomUUID());

        // Assert
        assertThat(info.getCapacity()).isZero();
        assertThat(info.getStored()).isZero();
    }

    @Test
    void Test_disassembling_a_non_existing_disk() {
        // Act
        Optional<StorageDisk<ItemStack>> disassembledDisk = storageDiskManager.disassembleDisk(UUID.randomUUID());

        // Assert
        assertThat(disassembledDisk).isEmpty();
    }

    @Test
    void Test_disassembling_a_non_empty_disk() {
        // Arrange
        UUID id = UUID.randomUUID();
        StorageDisk<ItemStack> storage = new ItemDiskStorage(10);
        storage.insert(new ItemStack(Items.DIRT), 5, Action.EXECUTE);
        storageDiskManager.setDisk(id, storage);

        // Act
        Optional<StorageDisk<ItemStack>> disassembledDisk = storageDiskManager.disassembleDisk(id);
        Optional<StorageDisk<ItemStack>> disk = storageDiskManager.getDisk(id);

        // Assert
        assertThat(disassembledDisk).isEmpty();
        assertThat(disk).isPresent();
    }

    @Test
    void Test_disassembling_an_empty_disk() {
        // Arrange
        UUID id = UUID.randomUUID();
        StorageDisk<ItemStack> storage = new ItemDiskStorage(10);
        storageDiskManager.setDisk(id, storage);

        // Act
        Optional<StorageDisk<ItemStack>> disassembledDisk = storageDiskManager.disassembleDisk(id);
        Optional<StorageDisk<ItemStack>> disk = storageDiskManager.getDisk(id);

        // Assert
        assertThat(disassembledDisk).isNotEmpty();
        assertThat(disassembledDisk.get()).isSameAs(storage);
        assertThat(disk).isNotPresent();
    }

    @Test
    void Test_inserting_duplicate_storage_disk_ids_should_fail() {
        // Arrange
        UUID id = UUID.randomUUID();
        storageDiskManager.setDisk(id, new ItemDiskStorage(10));

        // Act
        Executable action = () -> storageDiskManager.setDisk(id, new ItemDiskStorage(10));

        // Assert
        assertThrows(IllegalArgumentException.class, action);
    }
}