package com.refinedmods.refinedstorage2.api.storage;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.storage.disk.StorageDisk;
import com.refinedmods.refinedstorage2.api.storage.disk.StorageDiskImpl;
import com.refinedmods.refinedstorage2.test.Rs2Test;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Rs2Test
class StorageManagerImplTest {
    private final StorageManagerImpl sut = new StorageManagerImpl();

    @Test
    void Test_whether_getting_non_existent_storage_is_not_present() {
        // Assert
        assertThat(sut.get(UUID.randomUUID())).isEmpty();
    }

    @Test
    void Test_whether_getting_storage_is_present() {
        // Arrange
        UUID id = UUID.randomUUID();
        StorageDisk<String> storage = new StorageDiskImpl<>(1);

        // Act
        sut.set(id, storage);
        Optional<StorageDisk<String>> foundStorage = sut.get(id);

        Optional<StorageDisk<String>> nonExistent = sut.get(UUID.randomUUID());

        // Assert
        assertThat(foundStorage).isNotEmpty();
        assertThat(foundStorage.get()).isSameAs(storage);

        assertThat(nonExistent).isEmpty();
    }

    @Test
    void Test_getting_info_of_storage() {
        // Arrange
        UUID id = UUID.randomUUID();
        StorageDisk<String> storage = new StorageDiskImpl<>(10);
        storage.insert("A", 5, Action.EXECUTE);

        // Act
        sut.set(id, storage);

        StorageInfo info = sut.getInfo(id);

        // Assert
        assertThat(info.capacity()).isEqualTo(10);
        assertThat(info.stored()).isEqualTo(5);
    }

    @Test
    void Test_getting_info_of_non_existent_storage() {
        // Act
        StorageInfo info = sut.getInfo(UUID.randomUUID());

        // Assert
        assertThat(info.capacity()).isZero();
        assertThat(info.stored()).isZero();
    }

    @Test
    void Test_disassembling_a_non_existing_storage() {
        // Act
        Optional<StorageDisk<String>> disassembledDisk = sut.disassemble(UUID.randomUUID());

        // Assert
        assertThat(disassembledDisk).isEmpty();
    }

    @Test
    void Test_disassembling_a_non_empty_storage() {
        // Arrange
        UUID id = UUID.randomUUID();
        StorageDisk<String> storage = new StorageDiskImpl<>(10);
        storage.insert("A", 5, Action.EXECUTE);
        sut.set(id, storage);

        // Act
        Optional<StorageDisk<String>> disassembledDisk = sut.disassemble(id);
        Optional<StorageDisk<String>> afterDisassembly = sut.get(id);

        // Assert
        assertThat(disassembledDisk).isEmpty();
        assertThat(afterDisassembly).isPresent();
    }

    @Test
    void Test_disassembling_an_empty_storage() {
        // Arrange
        UUID id = UUID.randomUUID();
        StorageDisk<String> storage = new StorageDiskImpl<>(1);
        sut.set(id, storage);

        // Act
        Optional<StorageDisk<String>> disassembledDisk = sut.disassemble(id);
        Optional<StorageDisk<String>> afterDisassembly = sut.get(id);

        // Assert
        assertThat(disassembledDisk).isNotEmpty();
        assertThat(disassembledDisk.get()).isSameAs(storage);
        assertThat(afterDisassembly).isNotPresent();
    }

    @Test
    void Test_inserting_duplicate_storage_storage_ids_should_fail() {
        // Arrange
        UUID id = UUID.randomUUID();
        sut.set(id, new StorageDiskImpl<>(1));

        // Act
        Executable action = () -> sut.set(id, new StorageDiskImpl<>(1));

        // Assert
        assertThrows(IllegalArgumentException.class, action);
    }
}
