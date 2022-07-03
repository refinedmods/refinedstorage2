package com.refinedmods.refinedstorage2.api.storage;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.storage.limited.LimitedStorageImpl;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StorageRepositoryImplTest {
    private final StorageRepositoryImpl sut = new StorageRepositoryImpl();

    @Test
    void shouldNotRetrieveNonExistentStorage() {
        // Assert
        assertThat(sut.get(UUID.randomUUID())).isEmpty();
    }

    @Test
    void shouldBeAbleToSetAndRetrieveStorage() {
        // Arrange
        final UUID id = UUID.randomUUID();
        final Storage<String> storage = new LimitedStorageImpl<>(1);

        // Act
        sut.set(id, storage);

        // Assert
        final Optional<Storage<String>> existingStorage = sut.get(id);
        final Optional<Storage<String>> nonExistentStorage = sut.get(UUID.randomUUID());

        assertThat(existingStorage).containsSame(storage);
        assertThat(nonExistentStorage).isEmpty();
    }

    @Test
    void shouldNotBeAbleToSetDuplicateStorageId() {
        // Arrange
        final UUID id = UUID.randomUUID();
        sut.set(id, new LimitedStorageImpl<>(1));

        // Act
        final Executable action = () -> sut.set(id, new LimitedStorageImpl<>(1));

        // Assert
        assertThrows(IllegalArgumentException.class, action);
    }

    @Test
    void shouldRetrieveInfoFromLimitedStorage() {
        // Arrange
        final UUID id = UUID.randomUUID();
        final Storage<String> storage = new LimitedStorageImpl<>(10);
        storage.insert("A", 5, Action.EXECUTE, EmptySource.INSTANCE);

        // Act
        sut.set(id, storage);

        // Assert
        final StorageInfo info = sut.getInfo(id);

        assertThat(info.capacity()).isEqualTo(10);
        assertThat(info.stored()).isEqualTo(5);
    }

    @Test
    void shouldRetrieveInfoFromRegularStorage() {
        // Arrange
        final UUID id = UUID.randomUUID();
        final Storage<String> storage = new InMemoryStorageImpl<>();
        storage.insert("A", 5, Action.EXECUTE, EmptySource.INSTANCE);

        // Act
        sut.set(id, storage);

        // Assert
        final StorageInfo info = sut.getInfo(id);

        assertThat(info.capacity()).isZero();
        assertThat(info.stored()).isEqualTo(5);
    }

    @Test
    void shouldRetrieveInfoFromNonExistentStorage() {
        // Act
        final StorageInfo info = sut.getInfo(UUID.randomUUID());

        // Assert
        assertThat(info.capacity()).isZero();
        assertThat(info.stored()).isZero();
    }

    @Test
    void shouldNotDisassembleNonExistentStorage() {
        // Act
        final Optional<Storage<String>> disassembled = sut.disassemble(UUID.randomUUID());

        // Assert
        assertThat(disassembled).isEmpty();
    }

    @Test
    void shouldNotDisassembleNonEmptyStorage() {
        // Arrange
        final UUID id = UUID.randomUUID();
        final Storage<String> storage = new LimitedStorageImpl<>(10);
        storage.insert("A", 5, Action.EXECUTE, EmptySource.INSTANCE);
        sut.set(id, storage);

        // Act
        final Optional<Storage<String>> disassembled = sut.disassemble(id);

        // Assert
        final Optional<Storage<String>> afterDisassembly = sut.get(id);

        assertThat(disassembled).isEmpty();
        assertThat(afterDisassembly).isPresent();
    }

    @Test
    void shouldDisassembleEmptyStorage() {
        // Arrange
        final UUID id = UUID.randomUUID();
        final Storage<String> storage = new LimitedStorageImpl<>(1);
        sut.set(id, storage);

        // Act
        final Optional<Storage<String>> disassembled = sut.disassemble(id);

        // Assert
        final Optional<Storage<String>> afterDisassembly = sut.get(id);

        assertThat(disassembled).containsSame(storage);
        assertThat(afterDisassembly).isNotPresent();
    }
}
