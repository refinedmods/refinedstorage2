package com.refinedmods.refinedstorage2.api.storage.composite;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.resource.list.ResourceListImpl;
import com.refinedmods.refinedstorage2.api.storage.EmptyActor;
import com.refinedmods.refinedstorage2.api.storage.limited.LimitedStorageImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ConsumingStorageCompositeStorageImplTest {
    private CompositeStorageImpl<String> sut;

    @BeforeEach
    void setUp() {
        sut = new CompositeStorageImpl<>(new ResourceListImpl<>());
    }

    @Test
    void shouldLoadResourcesFromConsumingStorageWhenAddingSource() {
        // Arrange
        final ConsumingStorageImpl<String> consumingStorage = new ConsumingStorageImpl<>();
        consumingStorage.insert("A", 10, Action.EXECUTE, EmptyActor.INSTANCE);

        // Act
        sut.addSource(consumingStorage);

        // Assert
        assertThat(sut.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount<>("A", 10)
        );
        assertThat(sut.getStored()).isEqualTo(10);
    }

    @Test
    void shouldRemoveResourcesFromConsumingStorageWhenRemovingSource() {
        // Arrange
        final ConsumingStorageImpl<String> consumingStorage = new ConsumingStorageImpl<>();
        consumingStorage.insert("A", 10, Action.EXECUTE, EmptyActor.INSTANCE);
        sut.addSource(consumingStorage);

        // Act
        sut.removeSource(consumingStorage);

        // Assert
        assertThat(sut.getAll()).isEmpty();
        assertThat(sut.getStored()).isZero();
    }

    @Test
    void shouldInsertResourceEntirelyIntoConsumingStorage() {
        // Arrange
        sut.addSource(new ConsumingStorageImpl<>());

        // Act
        final long inserted = sut.insert("A", 10, Action.EXECUTE, EmptyActor.INSTANCE);

        // Assert
        assertThat(inserted).isEqualTo(10);
        assertThat(sut.getAll()).isEmpty();
        assertThat(sut.getStored()).isEqualTo(10);
    }

    @Test
    void shouldInsertPartlyEntirelyIntoConsumingStorage() {
        // Arrange
        sut.addSource(new PrioritizedStorage<>(10, new LimitedStorageImpl<>(7)));
        sut.addSource(new ConsumingStorageImpl<>());

        // Act
        final long inserted = sut.insert("A", 10, Action.EXECUTE, EmptyActor.INSTANCE);

        // Assert
        assertThat(inserted).isEqualTo(10);
        assertThat(sut.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount<>("A", 7)
        );
        assertThat(sut.getStored()).isEqualTo(10);
    }

    @Test
    void shouldExtractResourceEntirelyFromConsumingStorage() {
        // Arrange
        sut.addSource(new ConsumingStorageImpl<>());
        sut.insert("A", 10, Action.EXECUTE, EmptyActor.INSTANCE);

        // Act
        final long extracted = sut.extract("A", 10, Action.EXECUTE, EmptyActor.INSTANCE);

        // Assert
        assertThat(extracted).isEqualTo(10);
        assertThat(sut.getAll()).isEmpty();
        assertThat(sut.getStored()).isZero();
    }

    @Test
    void shouldExtractResourcePartlyFromConsumingStorage() {
        // Arrange
        sut.addSource(new PrioritizedStorage<>(10, new LimitedStorageImpl<>(7)));
        sut.addSource(new ConsumingStorageImpl<>());
        sut.insert("A", 8, Action.EXECUTE, EmptyActor.INSTANCE);

        // Act
        final long extracted = sut.extract("A", 10, Action.EXECUTE, EmptyActor.INSTANCE);

        // Assert
        assertThat(extracted).isEqualTo(8);
        assertThat(sut.getAll()).isEmpty();
        assertThat(sut.getStored()).isZero();
    }
}
