package com.refinedmods.refinedstorage2.api.storage.channel;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.resource.list.ResourceListOperationResult;
import com.refinedmods.refinedstorage2.api.resource.list.listenable.ResourceListListener;
import com.refinedmods.refinedstorage2.api.storage.EmptySource;
import com.refinedmods.refinedstorage2.api.storage.LimitedStorageImpl;
import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.composite.PrioritizedStorage;
import com.refinedmods.refinedstorage2.test.Rs2Test;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@Rs2Test
class StorageChannelImplTest {
    private StorageChannel<String> sut;

    @BeforeEach
    void setUp() {
        sut = new StorageChannelImpl<>();
    }

    @Test
    void Test_adding_source() {
        // Arrange
        Storage<String> storage = new LimitedStorageImpl<>(10);
        storage.insert("A", 8, Action.EXECUTE, EmptySource.INSTANCE);

        // Act
        sut.addSource(storage);

        long inserted = sut.insert("A", 3, Action.EXECUTE, EmptySource.INSTANCE);

        // Assert
        assertThat(sut.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
                new ResourceAmount<>("A", 10)
        );
        assertThat(inserted).isEqualTo(2);
    }

    @Test
    void Test_removing_source() {
        // Arrange
        Storage<String> storage = new LimitedStorageImpl<>(10);
        storage.insert("A", 5, Action.EXECUTE, EmptySource.INSTANCE);

        Storage<String> removedStorage = new LimitedStorageImpl<>(10);
        removedStorage.insert("A", 10, Action.EXECUTE, EmptySource.INSTANCE);

        sut.addSource(storage);
        sut.addSource(removedStorage);

        // Act
        sut.removeSource(removedStorage);

        long extracted = sut.extract("A", 15, Action.SIMULATE, EmptySource.INSTANCE);

        // Assert
        assertThat(sut.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
                new ResourceAmount<>("A", 5)
        );
        assertThat(extracted).isEqualTo(5);
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void Test_listener_on_insertion(Action action) {
        // Arrange
        sut.addSource(new LimitedStorageImpl<>(10));
        sut.insert("A", 2, Action.EXECUTE, EmptySource.INSTANCE);

        ResourceListListener<String> listener = mock(ResourceListListener.class);
        sut.addListener(listener);

        ArgumentCaptor<ResourceListOperationResult<String>> changedResource = ArgumentCaptor.forClass(ResourceListOperationResult.class);

        // Act
        sut.insert("A", 8, action, EmptySource.INSTANCE);

        // Assert
        if (action == Action.EXECUTE) {
            verify(listener, atMost(1)).onChanged(changedResource.capture());

            assertThat(changedResource.getValue().change()).isEqualTo(8);
            assertThat(changedResource.getValue().resourceAmount()).usingRecursiveComparison().isEqualTo(
                    new ResourceAmount<>("A", 10)
            );
        } else {
            verify(listener, never()).onChanged(any());
        }
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void Test_listener_on_extraction(Action action) {
        // Arrange
        Storage<String> storage = new LimitedStorageImpl<>(10);
        storage.insert("A", 10, Action.EXECUTE, EmptySource.INSTANCE);

        sut.addSource(storage);
        sut.extract("A", 2, Action.EXECUTE, EmptySource.INSTANCE);

        ResourceListListener<String> listener = mock(ResourceListListener.class);
        sut.addListener(listener);

        ArgumentCaptor<ResourceListOperationResult<String>> changedResource = ArgumentCaptor.forClass(ResourceListOperationResult.class);

        // Act
        sut.extract("A", 5, action, EmptySource.INSTANCE);

        // Assert
        if (action == Action.EXECUTE) {
            verify(listener, atMost(1)).onChanged(changedResource.capture());

            assertThat(changedResource.getValue().change()).isEqualTo(-5);
            assertThat(changedResource.getValue().resourceAmount()).usingRecursiveComparison().isEqualTo(
                    new ResourceAmount<>("A", 3)
            );
        } else {
            verify(listener, never()).onChanged(any());
        }
    }

    @Test
    void Test_removing_listener() {
        // Arrange
        sut.addSource(new LimitedStorageImpl<>(10));
        sut.insert("A", 2, Action.EXECUTE, EmptySource.INSTANCE);

        ResourceListListener<String> listener = mock(ResourceListListener.class);
        sut.addListener(listener);

        // Act
        sut.removeListener(listener);
        sut.insert("A", 8, Action.EXECUTE, EmptySource.INSTANCE);

        // Assert
        verify(listener, never()).onChanged(any());
    }

    @Test
    void Test_inserting() {
        // Arrange
        sut.addSource(new LimitedStorageImpl<>(10));

        // Act
        long inserted1 = sut.insert("A", 5, Action.EXECUTE, EmptySource.INSTANCE);
        long inserted2 = sut.insert("B", 4, Action.EXECUTE, EmptySource.INSTANCE);

        // Assert
        assertThat(sut.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount<>("A", 5),
                new ResourceAmount<>("B", 4)
        );
        assertThat(inserted1).isEqualTo(5);
        assertThat(inserted2).isEqualTo(4);
        assertThat(sut.getStored()).isEqualTo(9);
    }

    @Test
    void Test_extracting() {
        // Arrange
        Storage<String> storage = new LimitedStorageImpl<>(100);
        storage.insert("A", 50, Action.EXECUTE, EmptySource.INSTANCE);

        sut.addSource(storage);

        // Act
        long extracted = sut.extract("A", 49, Action.EXECUTE, EmptySource.INSTANCE);

        // Assert
        assertThat(sut.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount<>("A", 1)
        );
        assertThat(extracted).isEqualTo(49);
        assertThat(sut.getStored()).isEqualTo(1);
    }

    @Test
    void Test_getting_resource() {
        // Arrange
        Storage<String> storage = new LimitedStorageImpl<>(100);
        storage.insert("A", 50, Action.EXECUTE, EmptySource.INSTANCE);

        sut.addSource(storage);

        // Act
        Optional<ResourceAmount<String>> resource = sut.get("A");

        // Assert
        assertThat(resource).isPresent();
        assertThat(resource.get()).usingRecursiveComparison().isEqualTo(new ResourceAmount<>("A", 50));
    }

    @Test
    void Test_getting_non_existent_resource() {
        // Arrange
        sut.addSource(new LimitedStorageImpl<>(100));

        // Act
        Optional<ResourceAmount<String>> resource = sut.get("A");

        // Assert
        assertThat(resource).isEmpty();
    }

    @RepeatedTest(100)
    void Test_sorting_sources() {
        // Arrange
        PrioritizedStorage<String> storage1 = new PrioritizedStorage<>(0, new LimitedStorageImpl<>(10));
        PrioritizedStorage<String> storage2 = new PrioritizedStorage<>(0, new LimitedStorageImpl<>(10));
        PrioritizedStorage<String> storage3 = new PrioritizedStorage<>(0, new LimitedStorageImpl<>(10));

        sut.addSource(storage1);
        sut.addSource(storage2);
        sut.addSource(storage3);

        storage1.setPriority(8);
        storage2.setPriority(15);
        storage3.setPriority(2);

        // Act
        sut.sortSources();

        sut.insert("A", 15, Action.EXECUTE, EmptySource.INSTANCE);

        // Assert
        assertThat(storage2.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount<>("A", 10)
        );
        assertThat(storage1.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount<>("A", 5)
        );
        assertThat(storage3.getAll()).isEmpty();
    }
}
