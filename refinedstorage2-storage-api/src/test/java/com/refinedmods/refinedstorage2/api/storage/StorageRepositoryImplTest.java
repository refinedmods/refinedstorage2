package com.refinedmods.refinedstorage2.api.storage;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.test.Rs2Test;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Rs2Test
class StorageRepositoryImplTest {
    private final StorageRepositoryImpl sut = new StorageRepositoryImpl();

    @Test
    void Test_whether_getting_non_existent_storage_is_not_present() {
        // Assert
        assertThat(sut.get(UUID.randomUUID())).isEmpty();
    }

    @Test
    void Test_whether_getting_storage_is_present() {
        // Arrange
        UUID id = UUID.randomUUID();
        Storage<String> storage = new LimitedStorageImpl<>(1);

        // Act
        sut.set(id, storage);
        Optional<Storage<String>> foundStorage = sut.get(id);

        Optional<Storage<String>> nonExistent = sut.get(UUID.randomUUID());

        // Assert
        assertThat(foundStorage).isNotEmpty();
        assertThat(foundStorage.get()).isSameAs(storage);

        assertThat(nonExistent).isEmpty();
    }

    @Test
    void Test_getting_info_of_limited_storage() {
        // Arrange
        UUID id = UUID.randomUUID();
        Storage<String> storage = new LimitedStorageImpl<>(10);
        storage.insert("A", 5, Action.EXECUTE, EmptySource.INSTANCE);

        // Act
        sut.set(id, storage);

        StorageInfo info = sut.getInfo(id);

        // Assert
        assertThat(info.capacity()).isEqualTo(10);
        assertThat(info.stored()).isEqualTo(5);
    }

    @Test
    void Test_getting_info_of_storage() {
        // Arrange
        UUID id = UUID.randomUUID();
        Storage<String> storage = new InMemoryStorageImpl<>();
        storage.insert("A", 5, Action.EXECUTE, EmptySource.INSTANCE);

        // Act
        sut.set(id, storage);

        StorageInfo info = sut.getInfo(id);

        // Assert
        assertThat(info.capacity()).isZero();
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
        Optional<Storage<String>> disassembled = sut.disassemble(UUID.randomUUID());

        // Assert
        assertThat(disassembled).isEmpty();
    }

    @Test
    void Test_disassembling_a_non_empty_storage() {
        // Arrange
        UUID id = UUID.randomUUID();
        Storage<String> storage = new LimitedStorageImpl<>(10);
        storage.insert("A", 5, Action.EXECUTE, EmptySource.INSTANCE);
        sut.set(id, storage);

        // Act
        Optional<Storage<String>> disassembled = sut.disassemble(id);
        Optional<Storage<String>> afterDisassembly = sut.get(id);

        // Assert
        assertThat(disassembled).isEmpty();
        assertThat(afterDisassembly).isPresent();
    }

    @Test
    void Test_disassembling_an_empty_storage() {
        // Arrange
        UUID id = UUID.randomUUID();
        Storage<String> storage = new LimitedStorageImpl<>(1);
        sut.set(id, storage);

        // Act
        Optional<Storage<String>> disassembled = sut.disassemble(id);
        Optional<Storage<String>> afterDisassembly = sut.get(id);

        // Assert
        assertThat(disassembled).isNotEmpty();
        assertThat(disassembled.get()).isSameAs(storage);
        assertThat(afterDisassembly).isNotPresent();
    }

    @Test
    void Test_inserting_duplicate_storage_storage_ids_should_fail() {
        // Arrange
        UUID id = UUID.randomUUID();
        sut.set(id, new LimitedStorageImpl<>(1));

        // Act
        Executable action = () -> sut.set(id, new LimitedStorageImpl<>(1));

        // Assert
        assertThrows(IllegalArgumentException.class, action);
    }
}
