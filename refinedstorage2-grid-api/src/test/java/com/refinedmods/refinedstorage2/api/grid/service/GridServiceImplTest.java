package com.refinedmods.refinedstorage2.api.grid.service;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.resource.list.ResourceListImpl;
import com.refinedmods.refinedstorage2.api.storage.CappedStorage;
import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannel;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelImpl;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageTracker;
import com.refinedmods.refinedstorage2.api.storage.composite.CompositeStorage;
import com.refinedmods.refinedstorage2.test.Rs2Test;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

@Rs2Test
class GridServiceImplTest {
    private static final long MAX_COUNT = 15;

    private StorageChannel<String> storageChannel;
    private GridServiceImpl<String> sut;

    @BeforeEach
    void setUp() {
        storageChannel = new StorageChannelImpl<>(
                new CompositeStorage<>(Collections.emptyList(), new ResourceListImpl<>()), ResourceListImpl::new,
                new StorageTracker<>(() -> 0L)
        );
        sut = new GridServiceImpl<>(storageChannel, () -> "Test source", r -> MAX_COUNT);
    }

    @Nested
    class Insert {
        @ParameterizedTest
        @EnumSource(GridInsertMode.class)
        void Test_inserting(GridInsertMode insertMode) {
            // Arrange
            Storage<String> source = new CappedStorage<>(100);
            source.insert("A", MAX_COUNT * 3, Action.EXECUTE);

            storageChannel.addSource(new CappedStorage<>(100));

            // Act
            sut.insert("A", insertMode, source);

            // Assert
            long expectedAmount = switch (insertMode) {
                case ENTIRE_RESOURCE -> MAX_COUNT;
                case SINGLE_RESOURCE -> 1;
            };

            assertThat(storageChannel.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                    new ResourceAmount<>("A", expectedAmount)
            );
            assertThat(source.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                    new ResourceAmount<>("A", (MAX_COUNT * 3) - expectedAmount)
            );
            assertThat(storageChannel.getTracker().getEntry("A")).isPresent();
        }

        @ParameterizedTest
        @EnumSource(GridInsertMode.class)
        void Test_inserting_with_non_existent_resource(GridInsertMode insertMode) {
            // Arrange
            Storage<String> source = new CappedStorage<>(100);

            storageChannel.addSource(new CappedStorage<>(100));

            // Act
            sut.insert("A", insertMode, source);

            // Assert
            assertThat(storageChannel.getAll()).isEmpty();
            assertThat(source.getAll()).isEmpty();
            assertThat(storageChannel.getTracker().getEntry("A")).isEmpty();
        }

        @ParameterizedTest
        @EnumSource(GridInsertMode.class)
        void Test_inserting_with_no_space_in_storage(GridInsertMode insertMode) {
            // Arrange
            Storage<String> source = new CappedStorage<>(100);
            source.insert("A", 100, Action.EXECUTE);

            storageChannel.addSource(new CappedStorage<>(100));
            storageChannel.insert("A", 100, Action.EXECUTE);

            // Act
            sut.insert("A", insertMode, source);

            // Assert
            assertThat(storageChannel.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                    new ResourceAmount<>("A", 100)
            );
            assertThat(source.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                    new ResourceAmount<>("A", 100)
            );
            assertThat(storageChannel.getTracker().getEntry("A")).isEmpty();
        }
    }

    @Nested
    class InsertEntireResource {
        @Test
        void Test_inserting_with_remainder() {
            // Arrange
            Storage<String> source = new CappedStorage<>(100);
            source.insert("A", MAX_COUNT, Action.EXECUTE);

            storageChannel.addSource(new CappedStorage<>(100));
            storageChannel.insert("A", 100 - MAX_COUNT + 1, Action.EXECUTE);

            // Act
            sut.insert("A", GridInsertMode.ENTIRE_RESOURCE, source);

            // Assert
            assertThat(storageChannel.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                    new ResourceAmount<>("A", 100)
            );
            assertThat(source.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                    new ResourceAmount<>("A", 1)
            );
            assertThat(storageChannel.getTracker().getEntry("A")).isPresent();
        }

        @Test
        void Test_inserting_when_less_is_requested_from_source_because_storage_is_almost_full() {
            // This tests the case for buckets. It is perfectly possible to extract 1 bucket (when simulating).
            // But, if the storage only has space for half a bucket, we'll try to extract half a bucket instead of 1 bucket.
            // However, extracting half a bucket isn't possible, so the code has to handle this as well.
            // This is why we override extract to block extraction of non-entire buckets.

            // Arrange
            Storage<String> source = new CappedStorage<>(100) {
                @Override
                public long extract(String resource, long amount, Action action) {
                    if (amount != MAX_COUNT) {
                        return 0;
                    }
                    return super.extract(resource, amount, action);
                }
            };
            source.insert("A", MAX_COUNT, Action.EXECUTE);

            storageChannel.addSource(new CappedStorage<>(100));
            storageChannel.insert("A", 100 - MAX_COUNT + 1, Action.EXECUTE);

            // Act
            sut.insert("A", GridInsertMode.ENTIRE_RESOURCE, source);

            // Assert
            assertThat(storageChannel.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                    new ResourceAmount<>("A", 100 - MAX_COUNT + 1)
            );
            assertThat(source.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                    new ResourceAmount<>("A", MAX_COUNT)
            );
            assertThat(storageChannel.getTracker().getEntry("A")).isEmpty();
        }
    }

    @Nested
    class Extract {
        @ParameterizedTest
        @EnumSource(GridExtractMode.class)
        void Test_extracting(GridExtractMode extractMode) {
            // Arrange
            Storage<String> destination = new CappedStorage<>(100);

            storageChannel.addSource(new CappedStorage<>(100));
            storageChannel.insert("A", 100, Action.EXECUTE);

            // Act
            sut.extract("A", extractMode, destination);

            // Assert
            long expectedExtracted = switch (extractMode) {
                case ENTIRE_RESOURCE -> MAX_COUNT;
                case HALF_RESOURCE -> MAX_COUNT / 2;
                case SINGLE_RESOURCE -> 1;
            };

            assertThat(storageChannel.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                    new ResourceAmount<>("A", 100 - expectedExtracted)
            );
            assertThat(destination.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                    new ResourceAmount<>("A", expectedExtracted)
            );
            assertThat(storageChannel.getTracker().getEntry("A")).isPresent();
        }

        @ParameterizedTest
        @EnumSource(GridExtractMode.class)
        void Test_extracting_resource_that_does_not_exist(GridExtractMode extractMode) {
            // Arrange
            Storage<String> destination = new CappedStorage<>(100);

            storageChannel.addSource(new CappedStorage<>(100));

            // Act
            sut.extract("A", extractMode, destination);

            // Assert
            assertThat(storageChannel.getAll()).isEmpty();
            assertThat(destination.getAll()).isEmpty();
            assertThat(storageChannel.getTracker().getEntry("A")).isNotPresent();
        }

        @ParameterizedTest
        @EnumSource(GridExtractMode.class)
        void Test_extracting_resource_with_no_space_in_destination(GridExtractMode extractMode) {
            // Arrange
            Storage<String> destination = new CappedStorage<>(100);
            destination.insert("B", 100, Action.EXECUTE);

            storageChannel.addSource(new CappedStorage<>(100));
            storageChannel.insert("A", 100, Action.EXECUTE);

            // Act
            sut.extract("A", extractMode, destination);

            // Assert
            assertThat(storageChannel.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                    new ResourceAmount<>("A", 100)
            );
            assertThat(destination.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                    new ResourceAmount<>("B", 100)
            );
            assertThat(storageChannel.getTracker().getEntry("A")).isEmpty();
        }
    }

    @Nested
    class ExtractEntireResource {
        @Test
        void Test_extracting_entire_resource_that_has_less_than_max_count() {
            // Arrange
            Storage<String> destination = new CappedStorage<>(100);

            storageChannel.addSource(new CappedStorage<>(100));
            storageChannel.insert("A", MAX_COUNT - 1, Action.EXECUTE);

            // Act
            sut.extract("A", GridExtractMode.ENTIRE_RESOURCE, destination);

            // Assert
            assertThat(storageChannel.getAll()).isEmpty();
            assertThat(destination.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                    new ResourceAmount<>("A", MAX_COUNT - 1)
            );
            assertThat(storageChannel.getTracker().getEntry("A")).isPresent();
        }

        @Test
        void Test_extracting_entire_resource_with_remainder_in_destination() {
            // Arrange
            Storage<String> destination = new CappedStorage<>(MAX_COUNT - 1);

            storageChannel.addSource(new CappedStorage<>(100));
            storageChannel.insert("A", 100, Action.EXECUTE);

            // Act
            sut.extract("A", GridExtractMode.ENTIRE_RESOURCE, destination);

            // Assert
            assertThat(storageChannel.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                    new ResourceAmount<>("A", 100 - MAX_COUNT + 1)
            );
            assertThat(destination.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                    new ResourceAmount<>("A", MAX_COUNT - 1)
            );
            assertThat(storageChannel.getTracker().getEntry("A")).isPresent();
        }
    }

    @Nested
    class ExtractHalfResource {
        @Test
        void Test_extracting_half_resource_with_single_resource_amount() {
            // Arrange
            Storage<String> destination = new CappedStorage<>(MAX_COUNT);

            storageChannel.addSource(new CappedStorage<>(100));
            storageChannel.insert("A", 1, Action.EXECUTE);

            // Act
            sut.extract("A", GridExtractMode.HALF_RESOURCE, destination);

            // Assert
            assertThat(storageChannel.getAll()).isEmpty();
            assertThat(destination.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                    new ResourceAmount<>("A", 1)
            );
            assertThat(storageChannel.getTracker().getEntry("A")).isPresent();
        }
    }
}
