package com.refinedmods.refinedstorage2.api.storage.disk;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.storage.StorageInfo;
import com.refinedmods.refinedstorage2.test.Rs2Test;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Rs2Test
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
        StorageDisk<String> storage = new StorageDiskImpl<>(1);

        // Act
        storageDiskManager.setDisk(id, storage);
        Optional<StorageDisk<String>> foundStorage = storageDiskManager.getDisk(id);

        Optional<StorageDisk<String>> nonExistent = storageDiskManager.getDisk(UUID.randomUUID());

        // Assert
        assertThat(foundStorage).isNotEmpty();
        assertThat(foundStorage.get()).isSameAs(storage);

        assertThat(nonExistent).isEmpty();
    }

    @Test
    void Test_getting_info_of_disk() {
        // Arrange
        UUID id = UUID.randomUUID();
        StorageDisk<String> storage = new StorageDiskImpl<>(10);
        storage.insert("A", 5, Action.EXECUTE);

        // Act
        storageDiskManager.setDisk(id, storage);

        StorageInfo info = storageDiskManager.getInfo(id);

        // Assert
        assertThat(info.capacity()).isEqualTo(10);
        assertThat(info.stored()).isEqualTo(5);
    }

    @Test
    void Test_getting_info_of_non_existent_disk() {
        // Act
        StorageInfo info = storageDiskManager.getInfo(UUID.randomUUID());

        // Assert
        assertThat(info.capacity()).isZero();
        assertThat(info.stored()).isZero();
    }

    @Test
    void Test_disassembling_a_non_existing_disk() {
        // Act
        Optional<StorageDisk<String>> disassembledDisk = storageDiskManager.disassembleDisk(UUID.randomUUID());

        // Assert
        assertThat(disassembledDisk).isEmpty();
    }

    @Test
    void Test_disassembling_a_non_empty_disk() {
        // Arrange
        UUID id = UUID.randomUUID();
        StorageDisk<String> storage = new StorageDiskImpl<>(10);
        storage.insert("A", 5, Action.EXECUTE);
        storageDiskManager.setDisk(id, storage);

        // Act
        Optional<StorageDisk<String>> disassembledDisk = storageDiskManager.disassembleDisk(id);
        Optional<StorageDisk<String>> disk = storageDiskManager.getDisk(id);

        // Assert
        assertThat(disassembledDisk).isEmpty();
        assertThat(disk).isPresent();
    }

    @Test
    void Test_disassembling_an_empty_disk() {
        // Arrange
        UUID id = UUID.randomUUID();
        StorageDisk<String> storage = new StorageDiskImpl<>(1);
        storageDiskManager.setDisk(id, storage);

        // Act
        Optional<StorageDisk<String>> disassembledDisk = storageDiskManager.disassembleDisk(id);
        Optional<StorageDisk<String>> disk = storageDiskManager.getDisk(id);

        // Assert
        assertThat(disassembledDisk).isNotEmpty();
        assertThat(disassembledDisk.get()).isSameAs(storage);
        assertThat(disk).isNotPresent();
    }

    @Test
    void Test_inserting_duplicate_storage_disk_ids_should_fail() {
        // Arrange
        UUID id = UUID.randomUUID();
        storageDiskManager.setDisk(id, new StorageDiskImpl<>(1));

        // Act
        Executable action = () -> storageDiskManager.setDisk(id, new StorageDiskImpl<>(1));

        // Assert
        assertThrows(IllegalArgumentException.class, action);
    }
}
