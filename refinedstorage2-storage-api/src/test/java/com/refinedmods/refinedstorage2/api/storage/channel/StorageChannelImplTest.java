package com.refinedmods.refinedstorage2.api.storage.channel;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.resource.list.ResourceListImpl;
import com.refinedmods.refinedstorage2.api.resource.list.ResourceListOperationResult;
import com.refinedmods.refinedstorage2.api.resource.list.listenable.ResourceListListener;
import com.refinedmods.refinedstorage2.api.storage.bulk.BulkStorage;
import com.refinedmods.refinedstorage2.api.storage.bulk.BulkStorageImpl;
import com.refinedmods.refinedstorage2.api.storage.composite.CompositeStorage;
import com.refinedmods.refinedstorage2.api.storage.composite.PrioritizedStorage;
import com.refinedmods.refinedstorage2.test.Rs2Test;

import java.util.Collections;
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
    private StorageChannel<String> channel;

    @BeforeEach
    void setUp() {
        channel = new StorageChannelImpl<>(
                new CompositeStorage<>(Collections.emptyList(), new ResourceListImpl<>()), ResourceListImpl::new,
                new StorageTracker<>(System::currentTimeMillis)
        );
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void Test_listener_on_insertion(Action action) {
        // Arrange
        channel.addSource(new BulkStorageImpl<>(10));

        ResourceListListener<String> listener = mock(ResourceListListener.class);
        channel.addListener(listener);

        ArgumentCaptor<ResourceListOperationResult<String>> givenOperationResult = ArgumentCaptor.forClass(ResourceListOperationResult.class);

        // Act
        channel.insert("A", 15, action);

        // Assert
        if (action == Action.EXECUTE) {
            verify(listener, atMost(1)).onChanged(givenOperationResult.capture());

            assertThat(givenOperationResult.getValue().change()).isEqualTo(10);
            assertThat(givenOperationResult.getValue().resourceAmount()).usingRecursiveComparison().isEqualTo(
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
        BulkStorage<String> storage = new BulkStorageImpl<>(10);
        storage.insert("A", 10, Action.EXECUTE);

        channel.addSource(storage);

        ResourceListListener<String> listener = mock(ResourceListListener.class);
        channel.addListener(listener);

        ArgumentCaptor<ResourceListOperationResult<String>> givenOperationResult = ArgumentCaptor.forClass(ResourceListOperationResult.class);

        // Act
        channel.extract("A", 5, action);

        // Assert
        if (action == Action.EXECUTE) {
            verify(listener, atMost(1)).onChanged(givenOperationResult.capture());

            assertThat(givenOperationResult.getValue().change()).isEqualTo(-5);
            assertThat(givenOperationResult.getValue().resourceAmount()).usingRecursiveComparison().isEqualTo(
                    new ResourceAmount<>("A", 5)
            );
        } else {
            verify(listener, never()).onChanged(any());
        }
    }

    @Test
    void Test_inserting() {
        // Arrange
        channel.addSource(new BulkStorageImpl<>(10));

        // Act
        channel.insert("A", 5, Action.EXECUTE);
        channel.insert("B", 4, Action.EXECUTE);

        // Assert
        assertThat(channel.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount<>("A", 5),
                new ResourceAmount<>("B", 4)
        );
    }

    @Test
    void Test_extracting() {
        // Arrange
        BulkStorage<String> storage = new BulkStorageImpl<>(100);
        storage.insert("A", 50, Action.EXECUTE);

        channel.addSource(storage);

        // Act
        channel.extract("A", 49, Action.EXECUTE);

        // Assert
        assertThat(channel.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount<>("A", 1)
        );
    }

    @Test
    void Test_getting_resource() {
        // Arrange
        BulkStorage<String> storage = new BulkStorageImpl<>(100);
        storage.insert("A", 50, Action.EXECUTE);

        channel.addSource(storage);

        // Act
        Optional<ResourceAmount<String>> resource = channel.get("A");

        // Assert
        assertThat(resource).isPresent();
        assertThat(resource.get()).usingRecursiveComparison().isEqualTo(new ResourceAmount<>("A", 50));
    }

    @Test
    void Test_getting_non_existent_resource() {
        // Arrange
        channel.addSource(new BulkStorageImpl<>(100));

        // Act
        Optional<ResourceAmount<String>> resource = channel.get("A");

        // Assert
        assertThat(resource).isEmpty();
    }

    @RepeatedTest(100)
    void Test_sorting_sources() {
        // Arrange
        PrioritizedStorage<String> storage1 = new PrioritizedStorage<>(0, new BulkStorageImpl<>(10));
        PrioritizedStorage<String> storage2 = new PrioritizedStorage<>(0, new BulkStorageImpl<>(10));
        PrioritizedStorage<String> storage3 = new PrioritizedStorage<>(0, new BulkStorageImpl<>(10));

        channel.addSource(storage1);
        channel.addSource(storage2);
        channel.addSource(storage3);

        storage1.setPriority(8);
        storage2.setPriority(15);
        storage3.setPriority(2);

        // Act
        channel.sortSources();

        channel.insert("A", 15, Action.EXECUTE);

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
