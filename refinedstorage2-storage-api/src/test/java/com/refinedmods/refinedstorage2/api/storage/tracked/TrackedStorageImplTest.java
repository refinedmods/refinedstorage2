package com.refinedmods.refinedstorage2.api.storage.tracked;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.storage.EmptySource;
import com.refinedmods.refinedstorage2.api.storage.FakeSources;
import com.refinedmods.refinedstorage2.api.storage.limited.LimitedStorageImpl;
import com.refinedmods.refinedstorage2.test.Rs2Test;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Rs2Test
class TrackedStorageImplTest {
    private final AtomicLong clock = new AtomicLong(0);

    private LimitedStorageImpl<String> backed;
    private TrackedStorage<String> sut;

    @BeforeEach
    void setUp() {
        backed = new LimitedStorageImpl<>(100);
        sut = new TrackedStorageImpl<>(backed, clock::get);
    }

    @Test
    void Test_initial_state() {
        // Act
        Optional<TrackedResource> trackedResource = sut.findTrackedResourceBySourceType("A", EmptySource.class);

        // Assert
        assertThat(trackedResource).isEmpty();
    }

    @Test
    void Test_invalid_parent() {
        // Act & assert
        assertThrows(NullPointerException.class, () -> new TrackedStorageImpl<>(null, clock::get));
    }

    @Test
    void Test_inserting_with_invalid_source() {
        // Act
        Executable action = () -> sut.insert("A", 1, Action.EXECUTE, null);

        // Assert
        assertThrows(NullPointerException.class, action);
    }

    @Test
    void Test_extracting_with_invalid_source() {
        // Act
        Executable action = () -> sut.extract("A", 1, Action.EXECUTE, null);

        // Assert
        assertThrows(NullPointerException.class, action);
    }

    @Nested
    class InitialTrackTest {
        @Test
        void Test_should_not_find_untracked_resource() {
            // Act
            sut.insert("B", 100, Action.EXECUTE, FakeSources.FakeSource1.INSTANCE);

            // Assert
            Optional<TrackedResource> resourceA1 = sut.findTrackedResourceBySourceType("A", EmptySource.class);
            Optional<TrackedResource> resourceA2 = sut.findTrackedResourceBySourceType("A", FakeSources.FakeSource1.class);
            Optional<TrackedResource> resourceB1 = sut.findTrackedResourceBySourceType("B", EmptySource.class);
            Optional<TrackedResource> resourceB2 = sut.findTrackedResourceBySourceType("B", FakeSources.FakeSource1.class);

            assertThat(resourceA1).isEmpty();
            assertThat(resourceA2).isEmpty();
            assertThat(resourceB1).isEmpty();
            assertThat(resourceB2).isNotEmpty();
        }

        @ParameterizedTest
        @EnumSource(Action.class)
        void Test_tracking_a_resource_by_inserting(Action action) {
            // Act
            long inserted = sut.insert("A", 100, action, FakeSources.FakeSource1.INSTANCE);

            // Assert
            assertThat(inserted).isEqualTo(100);

            Optional<TrackedResource> trackedResource = sut.findTrackedResourceBySourceType("A", FakeSources.FakeSource1.class);

            if (action == Action.EXECUTE) {
                assertThat(trackedResource).get().usingRecursiveComparison().isEqualTo(new TrackedResource("Source1", 0));
            } else {
                assertThat(trackedResource).isEmpty();
            }
        }

        @ParameterizedTest
        @EnumSource(Action.class)
        void Test_tracking_a_resource_by_extracting(Action action) {
            // Arrange
            backed.insert("A", 100, Action.EXECUTE, EmptySource.INSTANCE);

            // Act
            long extracted = sut.extract("A", 10, action, FakeSources.FakeSource1.INSTANCE);

            // Assert
            assertThat(extracted).isEqualTo(10);

            Optional<TrackedResource> trackedResource = sut.findTrackedResourceBySourceType("A", FakeSources.FakeSource1.class);

            if (action == Action.EXECUTE) {
                assertThat(trackedResource).get().usingRecursiveComparison().isEqualTo(new TrackedResource("Source1", 0));
            } else {
                assertThat(trackedResource).isEmpty();
            }
        }

        @ParameterizedTest
        @EnumSource(Action.class)
        void Test_should_not_track_resource_when_inserting_to_an_already_full_storage(Action action) {
            // Arrange
            backed.insert("A", 100, Action.EXECUTE, EmptySource.INSTANCE);

            // Act
            long inserted = sut.insert("A", 1, action, FakeSources.FakeSource1.INSTANCE);

            // Assert
            assertThat(inserted).isZero();

            Optional<TrackedResource> resource = sut.findTrackedResourceBySourceType("A", FakeSources.FakeSource1.class);
            assertThat(resource).isEmpty();
        }

        @ParameterizedTest
        @EnumSource(Action.class)
        void Test_should_not_track_resource_when_extracting_nothing(Action action) {
            // Act
            long extracted = sut.extract("A", 1, action, FakeSources.FakeSource1.INSTANCE);

            // Assert
            assertThat(extracted).isZero();

            Optional<TrackedResource> trackedResource = sut.findTrackedResourceBySourceType("A", FakeSources.FakeSource1.class);
            assertThat(trackedResource).isEmpty();
        }

        @Test
        void Test_should_be_able_to_track_multiple_resources() {
            // Act
            sut.insert("A", 1, Action.EXECUTE, FakeSources.FakeSource1.INSTANCE);

            clock.set(1);
            sut.insert("B", 1, Action.EXECUTE, FakeSources.FakeSource1.INSTANCE);

            // Assert
            Optional<TrackedResource> resourceA = sut.findTrackedResourceBySourceType("A", FakeSources.FakeSource1.class);
            Optional<TrackedResource> resourceB = sut.findTrackedResourceBySourceType("B", FakeSources.FakeSource1.class);

            assertThat(resourceA).get().usingRecursiveComparison().isEqualTo(new TrackedResource("Source1", 0));
            assertThat(resourceB).get().usingRecursiveComparison().isEqualTo(new TrackedResource("Source1", 1));
        }
    }

    @Nested
    class UpdateTrackedResourceTest {
        @ParameterizedTest
        @EnumSource(Action.class)
        void Test_updating_a_tracked_resource_by_inserting(Action action) {
            // Act
            sut.insert("A", 50, Action.EXECUTE, FakeSources.FakeSource1.INSTANCE);
            clock.set(10);
            sut.insert("A", 60, action, FakeSources.FakeSource1.INSTANCE);

            // Assert
            Optional<TrackedResource> trackedResource = sut.findTrackedResourceBySourceType("A", FakeSources.FakeSource1.class);

            if (action == Action.EXECUTE) {
                assertThat(trackedResource).get().usingRecursiveComparison().isEqualTo(new TrackedResource("Source1", 10));
            } else {
                assertThat(trackedResource).get().usingRecursiveComparison().isEqualTo(new TrackedResource("Source1", 0));
            }
        }

        @ParameterizedTest
        @EnumSource(Action.class)
        void Test_updating_a_tracked_resource_by_extracting(Action action) {
            // Arrange
            backed.insert("A", 100, Action.EXECUTE, EmptySource.INSTANCE);

            // Act
            sut.extract("A", 50, Action.EXECUTE, FakeSources.FakeSource1.INSTANCE);
            clock.set(10);
            sut.extract("A", 60, action, FakeSources.FakeSource1.INSTANCE);

            // Assert
            Optional<TrackedResource> trackedResource = sut.findTrackedResourceBySourceType("A", FakeSources.FakeSource1.class);

            if (action == Action.EXECUTE) {
                assertThat(trackedResource).get().usingRecursiveComparison().isEqualTo(new TrackedResource("Source1", 10));
            } else {
                assertThat(trackedResource).get().usingRecursiveComparison().isEqualTo(new TrackedResource("Source1", 0));
            }
        }

        @ParameterizedTest
        @EnumSource(Action.class)
        void Test_should_not_update_tracked_resource_when_inserting_to_an_already_full_storage(Action action) {
            // Act
            sut.insert("A", 100, Action.EXECUTE, FakeSources.FakeSource1.INSTANCE);
            clock.set(10);
            sut.insert("A", 1, action, FakeSources.FakeSource1.INSTANCE);

            // Assert
            Optional<TrackedResource> trackedResource = sut.findTrackedResourceBySourceType("A", FakeSources.FakeSource1.class);
            assertThat(trackedResource).get().usingRecursiveComparison().isEqualTo(new TrackedResource("Source1", 0));
        }

        @ParameterizedTest
        @EnumSource(Action.class)
        void Test_should_not_update_tracked_resource_when_extracting_nothing(Action action) {
            // Arrange
            backed.insert("A", 100, Action.EXECUTE, EmptySource.INSTANCE);

            // Act
            sut.extract("A", 100, Action.EXECUTE, FakeSources.FakeSource1.INSTANCE);
            clock.set(10);
            sut.extract("A", 1, action, FakeSources.FakeSource1.INSTANCE);

            // Assert
            Optional<TrackedResource> trackedResource = sut.findTrackedResourceBySourceType("A", FakeSources.FakeSource1.class);
            assertThat(trackedResource).get().usingRecursiveComparison().isEqualTo(new TrackedResource("Source1", 0));
        }

        @Test
        void Test_should_be_able_to_update_tracked_multiple_resources() {
            // Act
            sut.insert("A", 1, Action.EXECUTE, FakeSources.FakeSource1.INSTANCE);

            clock.set(1);
            sut.insert("B", 1, Action.EXECUTE, FakeSources.FakeSource1.INSTANCE);

            clock.set(2);
            sut.insert("A", 1, Action.EXECUTE, FakeSources.FakeSource1.INSTANCE);

            clock.set(3);
            sut.insert("B", 1, Action.EXECUTE, FakeSources.FakeSource2.INSTANCE);

            // Assert
            Optional<TrackedResource> resourceAWithSource1 = sut.findTrackedResourceBySourceType("A", FakeSources.FakeSource1.class);
            Optional<TrackedResource> resourceAWithSource2 = sut.findTrackedResourceBySourceType("A", FakeSources.FakeSource2.class);
            Optional<TrackedResource> resourceBWithSource1 = sut.findTrackedResourceBySourceType("B", FakeSources.FakeSource1.class);
            Optional<TrackedResource> resourceBWithSource2 = sut.findTrackedResourceBySourceType("B", FakeSources.FakeSource2.class);

            assertThat(resourceAWithSource1).get().usingRecursiveComparison().isEqualTo(new TrackedResource("Source1", 2));
            assertThat(resourceAWithSource2).isEmpty();
            assertThat(resourceBWithSource1).get().usingRecursiveComparison().isEqualTo(new TrackedResource("Source1", 1));
            assertThat(resourceBWithSource2).get().usingRecursiveComparison().isEqualTo(new TrackedResource("Source2", 3));
        }
    }
}
