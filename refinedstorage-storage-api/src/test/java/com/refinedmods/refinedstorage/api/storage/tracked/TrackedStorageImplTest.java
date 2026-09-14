package com.refinedmods.refinedstorage.api.storage.tracked;

import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.api.storage.ActorFixtures;
import com.refinedmods.refinedstorage.api.storage.limited.LimitedStorageImpl;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static com.refinedmods.refinedstorage.api.storage.TestResource.A;
import static com.refinedmods.refinedstorage.api.storage.TestResource.B;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TrackedStorageImplTest {
    private final AtomicLong clock = new AtomicLong(0);

    private LimitedStorageImpl backed;
    private TrackedStorage sut;

    @BeforeEach
    void setUp() {
        backed = new LimitedStorageImpl(100);
        sut = new TrackedStorageImpl(backed, clock::get);
    }

    @Test
    void testInitialState() {
        // Act
        final Optional<TrackedResource> trackedResource = sut.findTrackedResourceByActorType(A, Actor.EMPTY.getClass());

        // Assert
        assertThat(trackedResource).isEmpty();
    }

    @Test
    void shouldCollectTrackedResourcesWithRepositoryThatCannotEnumerate() {
        // Arrange
        sut = new TrackedStorageImpl(backed, new MinimalRepository(), clock::get);
        sut.insert(A, 1, Action.EXECUTE, ActorFixtures.ActorFixture1.INSTANCE);

        // Act
        final Map<ResourceKey, TrackedResource> trackedResources = sut.getTrackedResourcesByActorType(
            ActorFixtures.ActorFixture1.class,
            Set.of(A, B)
        );

        // Assert
        assertThat(trackedResources).containsOnlyKeys(A);
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    void testInvalidParent() {
        // Act & assert
        assertThrows(NullPointerException.class, () -> new TrackedStorageImpl(null, clock::get));
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    void shouldNotInsertWithInvalidSource() {
        // Act
        final Executable action = () -> sut.insert(A, 1, Action.EXECUTE, null);

        // Assert
        assertThrows(NullPointerException.class, action);
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    void shouldNotExtractWithInvalidSource() {
        // Act
        final Executable action = () -> sut.extract(A, 1, Action.EXECUTE, null);

        // Assert
        assertThrows(NullPointerException.class, action);
    }

    @Nested
    class InitialTrackTest {
        @Test
        void shouldNotFindUntrackedResource() {
            // Act
            sut.insert(B, 100, Action.EXECUTE, ActorFixtures.ActorFixture1.INSTANCE);

            // Assert
            final Optional<TrackedResource> resourceA1 = sut.findTrackedResourceByActorType(A, Actor.EMPTY.getClass());
            final Optional<TrackedResource> resourceA2 =
                sut.findTrackedResourceByActorType(A, ActorFixtures.ActorFixture1.class);
            final Optional<TrackedResource> resourceB1 = sut.findTrackedResourceByActorType(B, Actor.EMPTY.getClass());
            final Optional<TrackedResource> resourceB2 =
                sut.findTrackedResourceByActorType(B, ActorFixtures.ActorFixture1.class);

            assertThat(resourceA1).isEmpty();
            assertThat(resourceA2).isEmpty();
            assertThat(resourceB1).isEmpty();
            assertThat(resourceB2).isNotEmpty();
        }

        @ParameterizedTest
        @EnumSource(Action.class)
        void shouldTrackResourceByInserting(final Action action) {
            // Arrange
            clock.set(1L);

            // Act
            final long inserted = sut.insert(A, 100, action, ActorFixtures.ActorFixture1.INSTANCE);

            // Assert
            assertThat(inserted).isEqualTo(100);

            final Optional<TrackedResource> trackedResource =
                sut.findTrackedResourceByActorType(A, ActorFixtures.ActorFixture1.class);

            if (action == Action.EXECUTE) {
                assertThat(trackedResource).isPresent();
                assertThat(trackedResource).get().hasToString("TrackedResource{"
                    + "sourceName='Source1'"
                    + ", time=1"
                    + '}');
                assertThat(trackedResource.get().getSourceName()).isEqualTo("Source1");
                assertThat(trackedResource.get().getTime()).isEqualTo(1);
            } else {
                assertThat(trackedResource).isEmpty();
            }
        }

        @ParameterizedTest
        @EnumSource(Action.class)
        void shouldNotTrackResourceByInsertingToAlreadyFullStorage(final Action action) {
            // Arrange
            backed.insert(A, 100, Action.EXECUTE, Actor.EMPTY);

            // Act
            final long inserted = sut.insert(A, 1, action, ActorFixtures.ActorFixture1.INSTANCE);

            // Assert
            assertThat(inserted).isZero();

            final Optional<TrackedResource> resource =
                sut.findTrackedResourceByActorType(A, ActorFixtures.ActorFixture1.class);
            assertThat(resource).isEmpty();
        }

        @ParameterizedTest
        @EnumSource(Action.class)
        void shouldTrackResourceByExtracting(final Action action) {
            // Arrange
            backed.insert(A, 100, Action.EXECUTE, Actor.EMPTY);

            // Act
            final long extracted = sut.extract(A, 10, action, ActorFixtures.ActorFixture1.INSTANCE);

            // Assert
            assertThat(extracted).isEqualTo(10);

            final Optional<TrackedResource> trackedResource =
                sut.findTrackedResourceByActorType(A, ActorFixtures.ActorFixture1.class);

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
            final long extracted = sut.extract(A, 1, action, ActorFixtures.ActorFixture1.INSTANCE);

            // Assert
            assertThat(extracted).isZero();

            final Optional<TrackedResource> trackedResource =
                sut.findTrackedResourceByActorType(A, ActorFixtures.ActorFixture1.class);
            assertThat(trackedResource).isEmpty();
        }

        @Test
        void shouldTrackMultipleResources() {
            // Act
            sut.insert(A, 1, Action.EXECUTE, ActorFixtures.ActorFixture1.INSTANCE);
            clock.set(1);
            sut.insert(B, 1, Action.EXECUTE, ActorFixtures.ActorFixture1.INSTANCE);

            // Assert
            final Optional<TrackedResource> resourceA =
                sut.findTrackedResourceByActorType(A, ActorFixtures.ActorFixture1.class);
            final Optional<TrackedResource> resourceB =
                sut.findTrackedResourceByActorType(B, ActorFixtures.ActorFixture1.class);

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
            sut.insert(A, 50, Action.EXECUTE, ActorFixtures.ActorFixture1.INSTANCE);
            clock.set(10);
            sut.insert(A, 60, action, ActorFixtures.ActorFixture1.INSTANCE);

            // Assert
            final Optional<TrackedResource> trackedResource =
                sut.findTrackedResourceByActorType(A, ActorFixtures.ActorFixture1.class);

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
            sut.insert(A, 100, Action.EXECUTE, ActorFixtures.ActorFixture1.INSTANCE);
            clock.set(10);
            sut.insert(A, 1, action, ActorFixtures.ActorFixture1.INSTANCE);

            // Assert
            final Optional<TrackedResource> trackedResource =
                sut.findTrackedResourceByActorType(A, ActorFixtures.ActorFixture1.class);
            assertThat(trackedResource).get().usingRecursiveComparison().isEqualTo(new TrackedResource("Source1", 0));
        }

        @ParameterizedTest
        @EnumSource(Action.class)
        void shouldUpdateTrackedResourceByExtracting(final Action action) {
            // Arrange
            backed.insert(A, 100, Action.EXECUTE, Actor.EMPTY);

            // Act
            sut.extract(A, 50, Action.EXECUTE, ActorFixtures.ActorFixture1.INSTANCE);
            clock.set(10);
            sut.extract(A, 60, action, ActorFixtures.ActorFixture1.INSTANCE);

            // Assert
            final Optional<TrackedResource> trackedResource =
                sut.findTrackedResourceByActorType(A, ActorFixtures.ActorFixture1.class);

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
            backed.insert(A, 100, Action.EXECUTE, Actor.EMPTY);

            // Act
            sut.extract(A, 100, Action.EXECUTE, ActorFixtures.ActorFixture1.INSTANCE);
            clock.set(10);
            sut.extract(A, 1, action, ActorFixtures.ActorFixture1.INSTANCE);

            // Assert
            final Optional<TrackedResource> trackedResource =
                sut.findTrackedResourceByActorType(A, ActorFixtures.ActorFixture1.class);
            assertThat(trackedResource).get().usingRecursiveComparison().isEqualTo(new TrackedResource("Source1", 0));
        }

        @Test
        void shouldBeAbleToUpdateMultipleTrackedResources() {
            // Act
            sut.insert(A, 1, Action.EXECUTE, ActorFixtures.ActorFixture1.INSTANCE);

            clock.set(1);
            sut.insert(B, 1, Action.EXECUTE, ActorFixtures.ActorFixture1.INSTANCE);

            clock.set(2);
            sut.insert(A, 1, Action.EXECUTE, ActorFixtures.ActorFixture1.INSTANCE);

            clock.set(3);
            sut.insert(B, 1, Action.EXECUTE, ActorFixtures.ActorFixture2.INSTANCE);

            // Assert
            final Optional<TrackedResource> resourceAWithSource1 =
                sut.findTrackedResourceByActorType(A, ActorFixtures.ActorFixture1.class);
            final Optional<TrackedResource> resourceAWithSource2 =
                sut.findTrackedResourceByActorType(A, ActorFixtures.ActorFixture2.class);
            final Optional<TrackedResource> resourceBWithSource1 =
                sut.findTrackedResourceByActorType(B, ActorFixtures.ActorFixture1.class);
            final Optional<TrackedResource> resourceBWithSource2 =
                sut.findTrackedResourceByActorType(B, ActorFixtures.ActorFixture2.class);

            assertThat(resourceAWithSource1).get().usingRecursiveComparison()
                .isEqualTo(new TrackedResource("Source1", 2));
            assertThat(resourceAWithSource2).isEmpty();
            assertThat(resourceBWithSource1).get().usingRecursiveComparison()
                .isEqualTo(new TrackedResource("Source1", 1));
            assertThat(resourceBWithSource2).get().usingRecursiveComparison()
                .isEqualTo(new TrackedResource("Source2", 3));
        }
    }

    // An addon repository that only implements the required methods, so collect falls back to the default body.
    private static class MinimalRepository implements TrackedStorageRepository {
        private final Map<Class<? extends Actor>, Map<ResourceKey, TrackedResource>> tracked = new HashMap<>();

        @Override
        public void update(final ResourceKey resource, final Actor actor, final long time) {
            tracked.computeIfAbsent(actor.getClass(), key -> new HashMap<>())
                .put(resource, new TrackedResource(actor.getName(), time));
        }

        @Override
        public Optional<TrackedResource> findTrackedResourceByActorType(final ResourceKey resource,
                                                                        final Class<? extends Actor> actorType) {
            return Optional.ofNullable(tracked.getOrDefault(actorType, Map.of()).get(resource));
        }
    }
}
