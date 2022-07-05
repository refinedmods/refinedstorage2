package com.refinedmods.refinedstorage2.api.storage.tracked;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.storage.EmptySource;
import com.refinedmods.refinedstorage2.api.storage.FakeSources;
import com.refinedmods.refinedstorage2.api.storage.limited.LimitedStorageImpl;

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
    void testInitialState() {
        // Act
        final Optional<TrackedResource> trackedResource = sut.findTrackedResourceBySourceType("A", EmptySource.class);

        // Assert
        assertThat(trackedResource).isEmpty();
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    void testInvalidParent() {
        // Act & assert
        assertThrows(NullPointerException.class, () -> new TrackedStorageImpl<>(null, clock::get));
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    void shouldNotInsertWithInvalidSource() {
        // Act
        final Executable action = () -> sut.insert("A", 1, Action.EXECUTE, null);

        // Assert
        assertThrows(NullPointerException.class, action);
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    void shouldNotExtractWithInvalidSource() {
        // Act
        final Executable action = () -> sut.extract("A", 1, Action.EXECUTE, null);

        // Assert
        assertThrows(NullPointerException.class, action);
    }

    @Nested
    class InitialTrackTest {
        @Test
        void shouldNotFindUntrackedResource() {
            // Act
            sut.insert("B", 100, Action.EXECUTE, FakeSources.FakeSource1.INSTANCE);

            // Assert
            final Optional<TrackedResource> resourceA1 = sut.findTrackedResourceBySourceType("A", EmptySource.class);
            final Optional<TrackedResource> resourceA2 =
                sut.findTrackedResourceBySourceType("A", FakeSources.FakeSource1.class);
            final Optional<TrackedResource> resourceB1 = sut.findTrackedResourceBySourceType("B", EmptySource.class);
            final Optional<TrackedResource> resourceB2 =
                sut.findTrackedResourceBySourceType("B", FakeSources.FakeSource1.class);

            assertThat(resourceA1).isEmpty();
            assertThat(resourceA2).isEmpty();
            assertThat(resourceB1).isEmpty();
            assertThat(resourceB2).isNotEmpty();
        }

        @ParameterizedTest
        @EnumSource(Action.class)
        void shouldTrackResourceByInserting(final Action action) {
            // Act
            final long inserted = sut.insert("A", 100, action, FakeSources.FakeSource1.INSTANCE);

            // Assert
            assertThat(inserted).isEqualTo(100);

            final Optional<TrackedResource> trackedResource =
                sut.findTrackedResourceBySourceType("A", FakeSources.FakeSource1.class);

            if (action == Action.EXECUTE) {
                assertThat(trackedResource).get().usingRecursiveComparison()
                    .isEqualTo(new TrackedResource("Source1", 0));
            } else {
                assertThat(trackedResource).isEmpty();
            }
        }

        @ParameterizedTest
        @EnumSource(Action.class)
        void shouldNotTrackResourceByInsertingToAlreadyFullStorage(final Action action) {
            // Arrange
            backed.insert("A", 100, Action.EXECUTE, EmptySource.INSTANCE);

            // Act
            final long inserted = sut.insert("A", 1, action, FakeSources.FakeSource1.INSTANCE);

            // Assert
            assertThat(inserted).isZero();

            final Optional<TrackedResource> resource =
                sut.findTrackedResourceBySourceType("A", FakeSources.FakeSource1.class);
            assertThat(resource).isEmpty();
        }

        @ParameterizedTest
        @EnumSource(Action.class)
        void shouldTrackResourceByExtracting(final Action action) {
            // Arrange
            backed.insert("A", 100, Action.EXECUTE, EmptySource.INSTANCE);

            // Act
            final long extracted = sut.extract("A", 10, action, FakeSources.FakeSource1.INSTANCE);

            // Assert
            assertThat(extracted).isEqualTo(10);

            final Optional<TrackedResource> trackedResource =
                sut.findTrackedResourceBySourceType("A", FakeSources.FakeSource1.class);

            if (action == Action.EXECUTE) {
                assertThat(trackedResource).get().usingRecursiveComparison()
                    .isEqualTo(new TrackedResource("Source1", 0));
            } else {
                assertThat(trackedResource).isEmpty();
            }
        }

        @ParameterizedTest
        @EnumSource(Action.class)
        void shouldNotTrackResourceByExtractingNothing(final Action action) {
            // Act
            final long extracted = sut.extract("A", 1, action, FakeSources.FakeSource1.INSTANCE);

            // Assert
            assertThat(extracted).isZero();

            final Optional<TrackedResource> trackedResource =
                sut.findTrackedResourceBySourceType("A", FakeSources.FakeSource1.class);
            assertThat(trackedResource).isEmpty();
        }

        @Test
        void shouldTrackMultipleResources() {
            // Act
            sut.insert("A", 1, Action.EXECUTE, FakeSources.FakeSource1.INSTANCE);
            clock.set(1);
            sut.insert("B", 1, Action.EXECUTE, FakeSources.FakeSource1.INSTANCE);

            // Assert
            final Optional<TrackedResource> resourceA =
                sut.findTrackedResourceBySourceType("A", FakeSources.FakeSource1.class);
            final Optional<TrackedResource> resourceB =
                sut.findTrackedResourceBySourceType("B", FakeSources.FakeSource1.class);

            assertThat(resourceA).get().usingRecursiveComparison().isEqualTo(new TrackedResource("Source1", 0));
            assertThat(resourceB).get().usingRecursiveComparison().isEqualTo(new TrackedResource("Source1", 1));
        }
    }

    @Nested
    class UpdateTrackedResourceTest {
        @ParameterizedTest
        @EnumSource(Action.class)
        void shouldUpdateTrackedResourceByInserting(final Action action) {
            // Act
            sut.insert("A", 50, Action.EXECUTE, FakeSources.FakeSource1.INSTANCE);
            clock.set(10);
            sut.insert("A", 60, action, FakeSources.FakeSource1.INSTANCE);

            // Assert
            final Optional<TrackedResource> trackedResource =
                sut.findTrackedResourceBySourceType("A", FakeSources.FakeSource1.class);

            if (action == Action.EXECUTE) {
                assertThat(trackedResource).get().usingRecursiveComparison()
                    .isEqualTo(new TrackedResource("Source1", 10));
            } else {
                assertThat(trackedResource).get().usingRecursiveComparison()
                    .isEqualTo(new TrackedResource("Source1", 0));
            }
        }

        @ParameterizedTest
        @EnumSource(Action.class)
        void shouldNotUpdateTrackedResourceByInsertingToAnAlreadyFullStorage(final Action action) {
            // Act
            sut.insert("A", 100, Action.EXECUTE, FakeSources.FakeSource1.INSTANCE);
            clock.set(10);
            sut.insert("A", 1, action, FakeSources.FakeSource1.INSTANCE);

            // Assert
            final Optional<TrackedResource> trackedResource =
                sut.findTrackedResourceBySourceType("A", FakeSources.FakeSource1.class);
            assertThat(trackedResource).get().usingRecursiveComparison().isEqualTo(new TrackedResource("Source1", 0));
        }

        @ParameterizedTest
        @EnumSource(Action.class)
        void shouldUpdateTrackedResourceByExtracting(final Action action) {
            // Arrange
            backed.insert("A", 100, Action.EXECUTE, EmptySource.INSTANCE);

            // Act
            sut.extract("A", 50, Action.EXECUTE, FakeSources.FakeSource1.INSTANCE);
            clock.set(10);
            sut.extract("A", 60, action, FakeSources.FakeSource1.INSTANCE);

            // Assert
            final Optional<TrackedResource> trackedResource =
                sut.findTrackedResourceBySourceType("A", FakeSources.FakeSource1.class);

            if (action == Action.EXECUTE) {
                assertThat(trackedResource).get().usingRecursiveComparison()
                    .isEqualTo(new TrackedResource("Source1", 10));
            } else {
                assertThat(trackedResource).get().usingRecursiveComparison()
                    .isEqualTo(new TrackedResource("Source1", 0));
            }
        }

        @ParameterizedTest
        @EnumSource(Action.class)
        void shouldNotUpdateTrackedResourceByExtractingNothing(final Action action) {
            // Arrange
            backed.insert("A", 100, Action.EXECUTE, EmptySource.INSTANCE);

            // Act
            sut.extract("A", 100, Action.EXECUTE, FakeSources.FakeSource1.INSTANCE);
            clock.set(10);
            sut.extract("A", 1, action, FakeSources.FakeSource1.INSTANCE);

            // Assert
            final Optional<TrackedResource> trackedResource =
                sut.findTrackedResourceBySourceType("A", FakeSources.FakeSource1.class);
            assertThat(trackedResource).get().usingRecursiveComparison().isEqualTo(new TrackedResource("Source1", 0));
        }

        @Test
        void shouldBeAbleToUpdateMultipleTrackedResources() {
            // Act
            sut.insert("A", 1, Action.EXECUTE, FakeSources.FakeSource1.INSTANCE);

            clock.set(1);
            sut.insert("B", 1, Action.EXECUTE, FakeSources.FakeSource1.INSTANCE);

            clock.set(2);
            sut.insert("A", 1, Action.EXECUTE, FakeSources.FakeSource1.INSTANCE);

            clock.set(3);
            sut.insert("B", 1, Action.EXECUTE, FakeSources.FakeSource2.INSTANCE);

            // Assert
            final Optional<TrackedResource> resourceAWithSource1 =
                sut.findTrackedResourceBySourceType("A", FakeSources.FakeSource1.class);
            final Optional<TrackedResource> resourceAWithSource2 =
                sut.findTrackedResourceBySourceType("A", FakeSources.FakeSource2.class);
            final Optional<TrackedResource> resourceBWithSource1 =
                sut.findTrackedResourceBySourceType("B", FakeSources.FakeSource1.class);
            final Optional<TrackedResource> resourceBWithSource2 =
                sut.findTrackedResourceBySourceType("B", FakeSources.FakeSource2.class);

            assertThat(resourceAWithSource1).get().usingRecursiveComparison()
                .isEqualTo(new TrackedResource("Source1", 2));
            assertThat(resourceAWithSource2).isEmpty();
            assertThat(resourceBWithSource1).get().usingRecursiveComparison()
                .isEqualTo(new TrackedResource("Source1", 1));
            assertThat(resourceBWithSource2).get().usingRecursiveComparison()
                .isEqualTo(new TrackedResource("Source2", 3));
        }
    }
}
