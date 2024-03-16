package com.refinedmods.refinedstorage2.api.grid.service;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.grid.operations.GridExtractMode;
import com.refinedmods.refinedstorage2.api.grid.operations.GridInsertMode;
import com.refinedmods.refinedstorage2.api.grid.operations.GridOperationsImpl;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.resource.ResourceKey;
import com.refinedmods.refinedstorage2.api.storage.Actor;
import com.refinedmods.refinedstorage2.api.storage.EmptyActor;
import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannel;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelImpl;
import com.refinedmods.refinedstorage2.api.storage.limited.LimitedStorageImpl;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedResource;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedStorageImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static com.refinedmods.refinedstorage2.api.grid.TestResource.A;
import static com.refinedmods.refinedstorage2.api.grid.TestResource.B;
import static org.assertj.core.api.Assertions.assertThat;

class GridOperationsImplTest {
    private static final long MAX_COUNT = 15;

    private StorageChannel storageChannel;
    private GridOperationsImpl sut;

    @BeforeEach
    void setUp() {
        storageChannel = new StorageChannelImpl();
        sut = new GridOperationsImpl(storageChannel, GridActor.INSTANCE, r -> MAX_COUNT, 1);
    }

    @Nested
    class InsertTest {
        @ParameterizedTest
        @EnumSource(GridInsertMode.class)
        void shouldInsertIntoDestination(final GridInsertMode insertMode) {
            // Arrange
            final Storage source = new LimitedStorageImpl(100);
            source.insert(A, MAX_COUNT * 3, Action.EXECUTE, EmptyActor.INSTANCE);

            final Storage destination = new TrackedStorageImpl(new LimitedStorageImpl(100), () -> 0L);
            storageChannel.addSource(destination);

            // Act
            final boolean success = sut.insert(A, insertMode, source);

            // Assert
            assertThat(success).isTrue();

            final long expectedAmount = switch (insertMode) {
                case ENTIRE_RESOURCE -> MAX_COUNT;
                case SINGLE_RESOURCE -> 1;
            };

            assertThat(storageChannel.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount(A, expectedAmount)
            );
            assertThat(source.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount(A, (MAX_COUNT * 3) - expectedAmount)
            );
            assertThat(storageChannel.findTrackedResourceByActorType(A, GridActor.class))
                .get()
                .usingRecursiveComparison()
                .isEqualTo(new TrackedResource(GridActor.NAME, 0));
        }

        @ParameterizedTest
        @EnumSource(GridInsertMode.class)
        void shouldNotInsertIntoDestinationWhenResourceIsNotPresentInSource(final GridInsertMode insertMode) {
            // Arrange
            final Storage source = new LimitedStorageImpl(100);

            final Storage destination = new TrackedStorageImpl(new LimitedStorageImpl(100), () -> 0L);
            storageChannel.addSource(destination);

            // Act
            final boolean success = sut.insert(A, insertMode, source);

            // Assert
            assertThat(success).isFalse();
            assertThat(storageChannel.getAll()).isEmpty();
            assertThat(source.getAll()).isEmpty();
            assertThat(storageChannel.findTrackedResourceByActorType(A, GridActor.class)).isEmpty();
        }

        @ParameterizedTest
        @EnumSource(GridInsertMode.class)
        void shouldNotInsertIntoDestinationWhenNoSpaceIsPresentInDestination(final GridInsertMode insertMode) {
            // Arrange
            final Storage source = new LimitedStorageImpl(100);
            source.insert(A, 100, Action.EXECUTE, EmptyActor.INSTANCE);

            final Storage destination = new TrackedStorageImpl(new LimitedStorageImpl(100), () -> 0L);
            storageChannel.addSource(destination);
            storageChannel.insert(A, 100, Action.EXECUTE, EmptyActor.INSTANCE);

            // Act
            final boolean success = sut.insert(A, insertMode, source);

            // Assert
            assertThat(success).isFalse();
            assertThat(storageChannel.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount(A, 100)
            );
            assertThat(source.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount(A, 100)
            );
            assertThat(storageChannel.findTrackedResourceByActorType(A, GridActor.class)).isEmpty();
        }
    }

    @Nested
    class InsertEntireResourceTest {
        @Test
        void shouldInsertIntoDestinationWithRemainder() {
            // Arrange
            final Storage source = new LimitedStorageImpl(100);
            source.insert(A, MAX_COUNT, Action.EXECUTE, EmptyActor.INSTANCE);

            final Storage destination = new TrackedStorageImpl(new LimitedStorageImpl(100), () -> 0L);
            storageChannel.addSource(destination);
            storageChannel.insert(A, 100 - MAX_COUNT + 1, Action.EXECUTE, EmptyActor.INSTANCE);

            // Act
            final boolean success = sut.insert(A, GridInsertMode.ENTIRE_RESOURCE, source);

            // Assert
            assertThat(success).isTrue();
            assertThat(storageChannel.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount(A, 100)
            );
            assertThat(source.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount(A, 1)
            );
            assertThat(storageChannel.findTrackedResourceByActorType(A, GridActor.class))
                .get()
                .usingRecursiveComparison()
                .isEqualTo(new TrackedResource(GridActor.NAME, 0));
        }

        @Test
        void shouldInsertWhenLessIsRequestedFromSourceBecauseDestinationIsAlmostFull() {
            // This tests the case for buckets. It is perfectly possible to extract 1 bucket (when simulating).
            // But, if the destination only has space for half a bucket,
            // we'll try to extract half a bucket instead of 1 bucket.
            // However, extracting half a bucket isn't possible, so the code has to handle this as well.
            // This is why we override extract to block extraction of non-entire buckets.

            // Arrange
            final Storage source = new LimitedStorageImpl(100) {
                @Override
                public long extract(final ResourceKey resource,
                                    final long amount,
                                    final Action action,
                                    final Actor source) {
                    if (amount != MAX_COUNT) {
                        return 0;
                    }
                    return super.extract(resource, amount, action, source);
                }
            };
            source.insert(A, MAX_COUNT, Action.EXECUTE, EmptyActor.INSTANCE);

            final Storage destination = new TrackedStorageImpl(new LimitedStorageImpl(100), () -> 0L);
            storageChannel.addSource(destination);
            storageChannel.insert(A, 100 - MAX_COUNT + 1, Action.EXECUTE, EmptyActor.INSTANCE);

            // Act
            final boolean success = sut.insert(A, GridInsertMode.ENTIRE_RESOURCE, source);

            // Assert
            assertThat(success).isFalse();
            assertThat(storageChannel.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount(A, 100 - MAX_COUNT + 1)
            );
            assertThat(source.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount(A, MAX_COUNT)
            );
            assertThat(storageChannel.findTrackedResourceByActorType(A, GridActor.class)).isEmpty();
        }
    }

    @Nested
    class ExtractTest {
        @ParameterizedTest
        @EnumSource(GridExtractMode.class)
        void shouldExtractFromSourceToDestination(final GridExtractMode extractMode) {
            // Arrange
            final Storage destination = new LimitedStorageImpl(100);

            final Storage source = new TrackedStorageImpl(new LimitedStorageImpl(100), () -> 0L);
            storageChannel.addSource(source);
            storageChannel.insert(A, 100, Action.EXECUTE, EmptyActor.INSTANCE);

            // Act
            final boolean success = sut.extract(A, extractMode, destination);

            // Assert
            assertThat(success).isTrue();

            final long expectedExtracted = switch (extractMode) {
                case ENTIRE_RESOURCE -> MAX_COUNT;
                case HALF_RESOURCE -> MAX_COUNT / 2;
                case SINGLE_RESOURCE -> 1;
            };

            assertThat(storageChannel.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount(A, 100 - expectedExtracted)
            );
            assertThat(destination.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount(A, expectedExtracted)
            );
            assertThat(storageChannel.findTrackedResourceByActorType(A, GridActor.class))
                .get()
                .usingRecursiveComparison()
                .isEqualTo(new TrackedResource(GridActor.NAME, 0));
        }

        @ParameterizedTest
        @EnumSource(GridExtractMode.class)
        void shouldNotExtractFromSourceWhenResourceIsNotPresentInSource(final GridExtractMode extractMode) {
            // Arrange
            final Storage destination = new LimitedStorageImpl(100);

            final Storage source = new TrackedStorageImpl(new LimitedStorageImpl(100), () -> 0L);
            storageChannel.addSource(source);

            // Act
            final boolean success = sut.extract(A, extractMode, destination);

            // Assert
            assertThat(success).isFalse();
            assertThat(storageChannel.getAll()).isEmpty();
            assertThat(destination.getAll()).isEmpty();
            assertThat(storageChannel.findTrackedResourceByActorType(A, GridActor.class)).isNotPresent();
        }

        @ParameterizedTest
        @EnumSource(GridExtractMode.class)
        void shouldNotExtractFromSourceIfThereIsNoSpaceInDestination(final GridExtractMode extractMode) {
            // Arrange
            final Storage destination = new LimitedStorageImpl(100);
            destination.insert(B, 100, Action.EXECUTE, EmptyActor.INSTANCE);

            final Storage source = new TrackedStorageImpl(new LimitedStorageImpl(100), () -> 0L);
            storageChannel.addSource(source);
            storageChannel.insert(A, 100, Action.EXECUTE, EmptyActor.INSTANCE);

            // Act
            final boolean success = sut.extract(A, extractMode, destination);

            // Assert
            assertThat(success).isFalse();
            assertThat(storageChannel.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount(A, 100)
            );
            assertThat(destination.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount(B, 100)
            );
            assertThat(storageChannel.findTrackedResourceByActorType(A, GridActor.class)).isEmpty();
        }
    }

    @Nested
    class ExtractEntireResourceTest {
        @Test
        void shouldExtractEntireResourceFromSourceToDestinationIfResourceIsLessThanMaxCount() {
            // Arrange
            final Storage destination = new LimitedStorageImpl(100);

            final Storage source = new TrackedStorageImpl(new LimitedStorageImpl(100), () -> 0L);
            storageChannel.addSource(source);
            storageChannel.insert(A, MAX_COUNT - 1, Action.EXECUTE, EmptyActor.INSTANCE);

            // Act
            final boolean success = sut.extract(A, GridExtractMode.ENTIRE_RESOURCE, destination);

            // Assert
            assertThat(success).isTrue();
            assertThat(storageChannel.getAll()).isEmpty();
            assertThat(destination.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount(A, MAX_COUNT - 1)
            );
            assertThat(storageChannel.findTrackedResourceByActorType(A, GridActor.class))
                .get()
                .usingRecursiveComparison()
                .isEqualTo(new TrackedResource(GridActor.NAME, 0));
        }

        @Test
        void shouldExtractEntireResourceWithRemainderFromSourceToDestinationIfThereIsNotEnoughSpaceInDestination() {
            // Arrange
            final Storage destination = new LimitedStorageImpl(MAX_COUNT - 1);

            final Storage source = new TrackedStorageImpl(new LimitedStorageImpl(100), () -> 0L);
            storageChannel.addSource(source);
            storageChannel.insert(A, 100, Action.EXECUTE, EmptyActor.INSTANCE);

            // Act
            final boolean success = sut.extract(A, GridExtractMode.ENTIRE_RESOURCE, destination);

            // Assert
            assertThat(success).isTrue();
            assertThat(storageChannel.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount(A, 100 - MAX_COUNT + 1)
            );
            assertThat(destination.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount(A, MAX_COUNT - 1)
            );
            assertThat(storageChannel.findTrackedResourceByActorType(A, GridActor.class))
                .get()
                .usingRecursiveComparison()
                .isEqualTo(new TrackedResource(GridActor.NAME, 0));
        }
    }

    @Nested
    class ExtractHalfResourceTest {
        @Test
        void shouldExtractSingleAmountIfResourceHasSingleAmountWhenExtractingHalfResourceFromSourceToDestination() {
            // Arrange
            final Storage destination = new LimitedStorageImpl(MAX_COUNT);

            final Storage source = new TrackedStorageImpl(new LimitedStorageImpl(100), () -> 0L);
            storageChannel.addSource(source);
            storageChannel.insert(A, 1, Action.EXECUTE, EmptyActor.INSTANCE);

            // Act
            final boolean success = sut.extract(A, GridExtractMode.HALF_RESOURCE, destination);

            // Assert
            assertThat(success).isTrue();
            assertThat(storageChannel.getAll()).isEmpty();
            assertThat(destination.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount(A, 1)
            );
            assertThat(storageChannel.findTrackedResourceByActorType(A, GridActor.class))
                .get()
                .usingRecursiveComparison()
                .isEqualTo(new TrackedResource(GridActor.NAME, 0));
        }
    }

    private static class GridActor implements Actor {
        private static final String NAME = "GridSource";
        private static final GridActor INSTANCE = new GridActor();

        @Override
        public String getName() {
            return NAME;
        }
    }
}
