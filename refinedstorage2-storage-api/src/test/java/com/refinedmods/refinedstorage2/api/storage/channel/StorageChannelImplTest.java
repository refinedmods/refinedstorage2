package com.refinedmods.refinedstorage2.api.storage.channel;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.stack.ResourceAmount;
import com.refinedmods.refinedstorage2.api.stack.list.StackListImpl;
import com.refinedmods.refinedstorage2.api.stack.list.StackListResult;
import com.refinedmods.refinedstorage2.api.stack.list.listenable.StackListListener;
import com.refinedmods.refinedstorage2.api.storage.composite.CompositeStorage;
import com.refinedmods.refinedstorage2.api.storage.composite.PrioritizedStorage;
import com.refinedmods.refinedstorage2.api.storage.disk.StorageDisk;
import com.refinedmods.refinedstorage2.api.storage.disk.StorageDiskImpl;
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
                StackListImpl::new,
                new StorageTracker<>(System::currentTimeMillis),
                new CompositeStorage<>(Collections.emptyList(), new StackListImpl<>())
        );
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void Test_listener_on_insertion(Action action) {
        // Arrange
        channel.addSource(new StorageDiskImpl<>(10));

        StackListListener<String> listener = mock(StackListListener.class);
        channel.addListener(listener);

        ArgumentCaptor<StackListResult<String>> givenStack = ArgumentCaptor.forClass(StackListResult.class);

        // Act
        channel.insert("A", 15, action);

        // Assert
        if (action == Action.EXECUTE) {
            verify(listener, atMost(1)).onChanged(givenStack.capture());

            assertThat(givenStack.getValue().change()).isEqualTo(10);
            assertThat(givenStack.getValue().resourceAmount()).usingRecursiveComparison().isEqualTo(new ResourceAmount<>("A", 10));
        } else {
            verify(listener, never()).onChanged(any());
        }
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void Test_listener_on_extraction(Action action) {
        // Arrange
        StorageDisk<String> diskStorage = new StorageDiskImpl<>(10);
        diskStorage.insert("A", 10, Action.EXECUTE);

        channel.addSource(diskStorage);

        StackListListener<String> listener = mock(StackListListener.class);
        channel.addListener(listener);

        ArgumentCaptor<StackListResult<String>> givenStack = ArgumentCaptor.forClass(StackListResult.class);

        // Act
        channel.extract("A", 5, action);

        // Assert
        if (action == Action.EXECUTE) {
            verify(listener, atMost(1)).onChanged(givenStack.capture());

            assertThat(givenStack.getValue().change()).isEqualTo(-5);
            assertThat(givenStack.getValue().resourceAmount()).usingRecursiveComparison().isEqualTo(new ResourceAmount<>("A", 5));
        } else {
            verify(listener, never()).onChanged(any());
        }
    }

    @Test
    void Test_inserting() {
        // Arrange
        channel.addSource(new StorageDiskImpl<>(10));

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
        StorageDisk<String> diskStorage = new StorageDiskImpl<>(100);
        diskStorage.insert("A", 50, Action.EXECUTE);

        channel.addSource(diskStorage);

        // Act
        channel.extract("A", 49, Action.EXECUTE);

        // Assert
        assertThat(channel.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount<>("A", 1)
        );
    }

    @Test
    void Test_getting_stack() {
        // Arrange
        StorageDisk<String> diskStorage = new StorageDiskImpl<>(100);
        diskStorage.insert("A", 50, Action.EXECUTE);

        channel.addSource(diskStorage);

        // Act
        Optional<ResourceAmount<String>> stack = channel.get("A");

        // Assert
        assertThat(stack).isPresent();
        assertThat(stack.get()).usingRecursiveComparison().isEqualTo(new ResourceAmount<>("A", 50));
    }

    @Test
    void Test_getting_non_existent_stack() {
        // Arrange
        channel.addSource(new StorageDiskImpl<>(100));

        // Act
        Optional<ResourceAmount<String>> stack = channel.get("A");

        // Assert
        assertThat(stack).isEmpty();
    }

    @RepeatedTest(100)
    void Test_sorting_sources() {
        // Arrange
        PrioritizedStorage<String> disk1 = new PrioritizedStorage<>(0, new StorageDiskImpl<>(10));
        PrioritizedStorage<String> disk2 = new PrioritizedStorage<>(0, new StorageDiskImpl<>(10));
        PrioritizedStorage<String> disk3 = new PrioritizedStorage<>(0, new StorageDiskImpl<>(10));

        channel.addSource(disk1);
        channel.addSource(disk2);
        channel.addSource(disk3);

        disk1.setPriority(8);
        disk2.setPriority(15);
        disk3.setPriority(2);

        // Act
        channel.sortSources();

        channel.insert("A", 15, Action.EXECUTE);

        // Assert
        assertThat(disk2.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount<>("A", 10)
        );
        assertThat(disk1.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount<>("A", 5)
        );
        assertThat(disk3.getAll()).isEmpty();
    }
}
