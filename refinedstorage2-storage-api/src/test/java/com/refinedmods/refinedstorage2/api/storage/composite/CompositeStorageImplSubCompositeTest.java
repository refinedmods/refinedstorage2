package com.refinedmods.refinedstorage2.api.storage.composite;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.resource.list.ResourceListImpl;
import com.refinedmods.refinedstorage2.api.storage.EmptyActor;
import com.refinedmods.refinedstorage2.api.storage.InMemoryStorageImpl;
import com.refinedmods.refinedstorage2.api.storage.Storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CompositeStorageImplSubCompositeTest {
    private CompositeStorageImpl<String> sut;

    @BeforeEach
    void setUp() {
        sut = new CompositeStorageImpl<>(new ResourceListImpl<>());
    }

    @Test
    void testAddingSourceToSubCompositeShouldNotifyParent() {
        // Arrange
        final CompositeStorage<String> subComposite = new CompositeStorageImpl<>(new ResourceListImpl<>());
        final Storage<String> subStorage = new InMemoryStorageImpl<>();
        subStorage.insert("B", 10, Action.EXECUTE, EmptyActor.INSTANCE);

        sut.addSource(subComposite);
        sut.addSource(subStorage);

        final Storage<String> subCompositeStorage = new InMemoryStorageImpl<>();
        subCompositeStorage.insert("A", 10, Action.EXECUTE, EmptyActor.INSTANCE);

        // Act
        subComposite.addSource(subCompositeStorage);

        // Assert
        assertThat(sut.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount<>("A", 10),
            new ResourceAmount<>("B", 10)
        );

        assertThat(subComposite.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount<>("A", 10)
        );
    }

    @Test
    void testRemovingSourceFromSubCompositeShouldNotifyParent() {
        // Arrange
        final CompositeStorage<String> subComposite = new CompositeStorageImpl<>(new ResourceListImpl<>());
        final Storage<String> subStorage = new InMemoryStorageImpl<>();
        subStorage.insert("B", 10, Action.EXECUTE, EmptyActor.INSTANCE);

        sut.addSource(subComposite);
        sut.addSource(subStorage);

        final Storage<String> subCompositeStorage = new InMemoryStorageImpl<>();
        subCompositeStorage.insert("A", 10, Action.EXECUTE, EmptyActor.INSTANCE);

        subComposite.addSource(subCompositeStorage);

        // Act
        subComposite.removeSource(subCompositeStorage);

        // Assert
        assertThat(sut.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount<>("B", 10)
        );
        assertThat(subComposite.getAll()).isEmpty();
    }
}
