package com.refinedmods.refinedstorage2.api.grid.service;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.stack.ResourceAmount;
import com.refinedmods.refinedstorage2.api.stack.list.StackListImpl;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannel;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelImpl;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageTracker;
import com.refinedmods.refinedstorage2.api.storage.composite.CompositeStorage;
import com.refinedmods.refinedstorage2.api.storage.disk.StorageDiskImpl;
import com.refinedmods.refinedstorage2.test.Rs2Test;

import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Rs2Test
class GridServiceImplTest {
    private StorageChannel<String> storageChannel;
    private GridServiceImpl<String> sut;

    @BeforeEach
    void setUp() {
        storageChannel = new StorageChannelImpl<>(
                StackListImpl::new,
                new StorageTracker<>(() -> 0L),
                new CompositeStorage<>(Collections.emptyList(), new StackListImpl<>())
        );
        sut = new GridServiceImpl<>(storageChannel, () -> "Test source");
    }

    @Nested
    class InsertEntireResource {
        @Test
        void Test_inserting_entire_resource() {
            // Arrange
            storageChannel.addSource(new StorageDiskImpl<>(100));

            // Act
            Optional<ResourceAmount<String>> remainder = sut.insert(new ResourceAmount<>("A", 100), GridInsertMode.ENTIRE_RESOURCE);

            // Assert
            assertThat(remainder).isEmpty();
            assertThat(storageChannel.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                    new ResourceAmount<>("A", 100)
            );
            assertThat(storageChannel.getTracker().getEntry("A")).isPresent();
        }

        @Test
        void Test_inserting_entire_resource_with_remainder() {
            // Arrange
            storageChannel.addSource(new StorageDiskImpl<>(100));

            // Act
            Optional<ResourceAmount<String>> remainder = sut.insert(new ResourceAmount<>("A", 101), GridInsertMode.ENTIRE_RESOURCE);

            // Assert
            assertThat(remainder).isPresent();
            assertThat(remainder.get()).usingRecursiveComparison().isEqualTo(new ResourceAmount<>("A", 1));
            assertThat(storageChannel.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                    new ResourceAmount<>("A", 100)
            );
            assertThat(storageChannel.getTracker().getEntry("A")).isPresent();
        }

        @Test
        void Test_inserting_entire_resource_with_no_space_in_storage() {
            // Arrange
            storageChannel.addSource(new StorageDiskImpl<>(100));
            storageChannel.insert("A", 100, Action.EXECUTE);

            // Act
            Optional<ResourceAmount<String>> remainder = sut.insert(new ResourceAmount<>("A", 1), GridInsertMode.ENTIRE_RESOURCE);

            // Assert
            assertThat(remainder).isPresent();
            assertThat(remainder.get()).usingRecursiveComparison().isEqualTo(new ResourceAmount<>("A", 1));
            assertThat(storageChannel.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                    new ResourceAmount<>("A", 100)
            );
            assertThat(storageChannel.getTracker().getEntry("A")).isNotPresent();
        }
    }

    @Nested
    class InsertSingleResource {
        @Test
        void Test_inserting_single_resource_of_large_amount() {
            // Arrange
            storageChannel.addSource(new StorageDiskImpl<>(100));

            // Act
            Optional<ResourceAmount<String>> remainder = sut.insert(new ResourceAmount<>("A", 100), GridInsertMode.SINGLE_RESOURCE);

            // Assert
            assertThat(remainder).isPresent();
            assertThat(remainder.get()).usingRecursiveComparison().isEqualTo(new ResourceAmount<>("A", 99));
            assertThat(storageChannel.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                    new ResourceAmount<>("A", 1)
            );
            assertThat(storageChannel.getTracker().getEntry("A")).isPresent();
        }

        @Test
        void Test_inserting_single_resource_of_single_amount() {
            // Arrange
            storageChannel.addSource(new StorageDiskImpl<>(100));

            // Act
            Optional<ResourceAmount<String>> remainder = sut.insert(new ResourceAmount<>("A", 1), GridInsertMode.SINGLE_RESOURCE);

            // Assert
            assertThat(remainder).isEmpty();
            assertThat(storageChannel.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                    new ResourceAmount<>("A", 1)
            );
            assertThat(storageChannel.getTracker().getEntry("A")).isPresent();
        }

        @Test
        void Test_inserting_single_resource_of_large_amount_with_no_space_in_storage() {
            // Arrange
            storageChannel.addSource(new StorageDiskImpl<>(100));
            storageChannel.insert("A", 100, Action.EXECUTE);

            // Act
            Optional<ResourceAmount<String>> remainder = sut.insert(new ResourceAmount<>("A", 100), GridInsertMode.SINGLE_RESOURCE);

            // Assert
            assertThat(remainder).isPresent();
            assertThat(remainder.get()).usingRecursiveComparison().isEqualTo(new ResourceAmount<>("A", 100));
            assertThat(storageChannel.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                    new ResourceAmount<>("A", 100)
            );
            assertThat(storageChannel.getTracker().getEntry("A")).isNotPresent();
        }

        @Test
        void Test_inserting_single_resource_of_single_amount_with_no_space_in_storage() {
            // Arrange
            storageChannel.addSource(new StorageDiskImpl<>(100));
            storageChannel.insert("A", 100, Action.EXECUTE);

            // Act
            Optional<ResourceAmount<String>> remainder = sut.insert(new ResourceAmount<>("A", 1), GridInsertMode.SINGLE_RESOURCE);

            // Assert
            assertThat(remainder).isPresent();
            assertThat(remainder.get()).usingRecursiveComparison().isEqualTo(new ResourceAmount<>("A", 1));
            assertThat(storageChannel.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                    new ResourceAmount<>("A", 100)
            );
            assertThat(storageChannel.getTracker().getEntry("A")).isNotPresent();
        }
    }
}
