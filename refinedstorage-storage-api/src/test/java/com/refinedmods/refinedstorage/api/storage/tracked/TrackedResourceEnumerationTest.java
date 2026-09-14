package com.refinedmods.refinedstorage.api.storage.tracked;

import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.resource.list.MutableResourceListImpl;
import com.refinedmods.refinedstorage.api.storage.AbstractProxyStorage;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.api.storage.ActorFixtures;
import com.refinedmods.refinedstorage.api.storage.StorageImpl;
import com.refinedmods.refinedstorage.api.storage.composite.CompositeStorageImpl;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.refinedmods.refinedstorage.api.storage.TestResource.A;
import static com.refinedmods.refinedstorage.api.storage.TestResource.B;
import static com.refinedmods.refinedstorage.api.storage.TestResource.C;
import static org.assertj.core.api.Assertions.assertThat;

class TrackedResourceEnumerationTest {
    private CompositeStorageImpl sut;
    private AtomicLong clock;

    @BeforeEach
    void setUp() {
        sut = new CompositeStorageImpl(MutableResourceListImpl.create());
        clock = new AtomicLong();
    }

    @Test
    void shouldNotInterrogateSourcesPerResource() {
        // Arrange
        final AtomicLong lookups = new AtomicLong();
        for (int i = 0; i < 10; i++) {
            final CountingStorage disk = new CountingStorage(new TrackedStorageImpl(new StorageImpl(), clock::get),
                lookups);
            disk.insert(new TestKey(i), 1, Action.EXECUTE, ActorFixtures.ActorFixture1.INSTANCE);
            sut.addSource(disk);
        }
        lookups.set(0);

        // Act
        final Map<ResourceKey, TrackedResource> result = snapshot(ActorFixtures.ActorFixture1.class);

        // Assert
        assertThat(result).hasSize(10);
        assertThat(lookups).hasValue(10);
    }

    @Test
    void shouldReturnNewestTrackedResourceAcrossSources() {
        // Arrange
        final TrackedStorageImpl older = disk();
        final TrackedStorageImpl newer = disk();
        clock.set(100);
        older.insert(A, 1, Action.EXECUTE, ActorFixtures.ActorFixture1.INSTANCE);
        clock.set(200);
        newer.insert(A, 1, Action.EXECUTE, ActorFixtures.ActorFixture1.INSTANCE);
        sut.addSource(older);
        sut.addSource(newer);

        // Act
        final Map<ResourceKey, TrackedResource> result = snapshot(ActorFixtures.ActorFixture1.class);

        // Assert
        assertThat(result.get(A)).isEqualTo(find(A, ActorFixtures.ActorFixture1.class));
        assertThat(result.get(A).getTime()).isEqualTo(200);
    }

    @Test
    void shouldResolveTieLikePerResourceLookup() {
        // Arrange
        final TrackedStorageImpl first = disk();
        final TrackedStorageImpl second = disk();
        clock.set(100);
        first.insert(A, 1, Action.EXECUTE, new NamedActor("first"));
        second.insert(A, 1, Action.EXECUTE, new NamedActor("second"));
        sut.addSource(first);
        sut.addSource(second);

        // Act
        final Map<ResourceKey, TrackedResource> result = snapshot(NamedActor.class);

        // Assert
        assertThat(result.get(A)).isEqualTo(find(A, NamedActor.class));
        assertThat(result.get(A).getSourceName()).isEqualTo("first");
    }

    @Test
    void shouldResolveTieAcrossNestedComposites() {
        // Arrange
        final TrackedStorageImpl first = disk();
        final TrackedStorageImpl second = disk();
        final TrackedStorageImpl third = disk();
        clock.set(200);
        first.insert(A, 1, Action.EXECUTE, new NamedActor("first"));
        second.insert(A, 1, Action.EXECUTE, new NamedActor("second"));
        clock.set(100);
        third.insert(A, 1, Action.EXECUTE, new NamedActor("third"));
        final CompositeStorageImpl nested = new CompositeStorageImpl(MutableResourceListImpl.create());
        nested.addSource(second);
        nested.addSource(third);
        sut.addSource(first);
        sut.addSource(nested);

        // Act
        final Map<ResourceKey, TrackedResource> result = snapshot(NamedActor.class);

        // Assert
        assertThat(result.get(A)).isEqualTo(find(A, NamedActor.class));
        assertThat(result.get(A).getSourceName()).isEqualTo("first");
    }

    @Test
    void shouldOnlyReturnRequestedActorType() {
        // Arrange
        final TrackedStorageImpl disk = disk();
        disk.insert(A, 1, Action.EXECUTE, ActorFixtures.ActorFixture1.INSTANCE);
        disk.insert(B, 1, Action.EXECUTE, ActorFixtures.ActorFixture2.INSTANCE);
        sut.addSource(disk);

        // Act
        final Map<ResourceKey, TrackedResource> result = snapshot(ActorFixtures.ActorFixture1.class);

        // Assert
        assertThat(result).containsOnlyKeys(A);
    }

    @Test
    void shouldNotReturnResourcesOutsideRequestedSet() {
        // Arrange
        final TrackedStorageImpl disk = disk();
        disk.insert(A, 1, Action.EXECUTE, ActorFixtures.ActorFixture1.INSTANCE);
        disk.insert(B, 1, Action.EXECUTE, ActorFixtures.ActorFixture1.INSTANCE);
        sut.addSource(disk);

        // Act
        final Map<ResourceKey, TrackedResource> result = sut.getTrackedResourcesByActorType(
            ActorFixtures.ActorFixture1.class,
            Set.of(A, new TestKey(1), new TestKey(2))
        );

        // Assert
        assertThat(result).containsOnlyKeys(A);
    }

    @Test
    void shouldNotEnumerateTrackingMapLargerThanRequestedResources() {
        // Arrange
        final TrackedStorageImpl disk = disk();
        for (int i = 0; i < 100; i++) {
            final TestKey stale = new TestKey(i);
            disk.insert(stale, 1, Action.EXECUTE, ActorFixtures.ActorFixture1.INSTANCE);
            disk.extract(stale, 1, Action.EXECUTE, ActorFixtures.ActorFixture1.INSTANCE);
        }
        disk.insert(A, 1, Action.EXECUTE, ActorFixtures.ActorFixture1.INSTANCE);
        sut.addSource(disk);

        // Act
        final Map<ResourceKey, TrackedResource> result = snapshot(ActorFixtures.ActorFixture1.class);

        // Assert
        assertThat(result).containsOnlyKeys(A);
    }

    @Test
    void shouldIncludeSourceThatCannotEnumerate() {
        // Arrange
        final TrackedStorageImpl disk = disk();
        disk.insert(A, 1, Action.EXECUTE, ActorFixtures.ActorFixture1.INSTANCE);
        sut.addSource(disk);
        final OpaqueStorage opaque = new OpaqueStorage();
        opaque.insert(C, 1, Action.EXECUTE, ActorFixtures.ActorFixture1.INSTANCE);
        sut.addSource(opaque);

        // Act
        final Map<ResourceKey, TrackedResource> result = snapshot(ActorFixtures.ActorFixture1.class);

        // Assert
        assertThat(result).containsOnlyKeys(A, C);
    }

    private TrackedStorageImpl disk() {
        return new TrackedStorageImpl(new StorageImpl(), clock::get);
    }

    private Map<ResourceKey, TrackedResource> snapshot(final Class<? extends Actor> actorType) {
        return sut.getTrackedResourcesByActorType(
            actorType,
            sut.getAll().stream().map(ResourceAmount::resource).collect(Collectors.toSet())
        );
    }

    @Nullable
    private TrackedResource find(final ResourceKey resource, final Class<? extends Actor> actorType) {
        return sut.findTrackedResourceByActorType(resource, actorType).orElse(null);
    }

    private record NamedActor(String name) implements Actor {
        @Override
        public String getName() {
            return name;
        }
    }

    private static class CountingStorage extends AbstractProxyStorage implements TrackedStorage {
        private final TrackedStorageImpl trackedDelegate;
        private final AtomicLong lookups;

        CountingStorage(final TrackedStorageImpl delegate, final AtomicLong lookups) {
            super(delegate);
            this.trackedDelegate = delegate;
            this.lookups = lookups;
        }

        @Override
        public Optional<TrackedResource> findTrackedResourceByActorType(final ResourceKey resource,
                                                                        final Class<? extends Actor> actorType) {
            lookups.incrementAndGet();
            return trackedDelegate.findTrackedResourceByActorType(resource, actorType);
        }

        @Override
        public void collectTrackedResourcesByActorType(final Class<? extends Actor> actorType,
                                                       final Set<ResourceKey> resources,
                                                       final BiConsumer<ResourceKey, TrackedResource> consumer) {
            lookups.incrementAndGet();
            trackedDelegate.collectTrackedResourcesByActorType(actorType, resources, consumer);
        }
    }

    private static class OpaqueStorage extends StorageImpl implements TrackedStorage {
        private final Map<ResourceKey, TrackedResource> tracked = new HashMap<>();

        @Override
        public long insert(final ResourceKey resource, final long amount, final Action action, final Actor actor) {
            final long inserted = super.insert(resource, amount, action, actor);
            if (inserted > 0 && action == Action.EXECUTE) {
                tracked.put(resource, new TrackedResource(actor.getName(), 1L));
            }
            return inserted;
        }

        @Override
        public Optional<TrackedResource> findTrackedResourceByActorType(final ResourceKey resource,
                                                                        final Class<? extends Actor> actorType) {
            return Optional.ofNullable(tracked.get(resource));
        }
    }

    private record TestKey(int id) implements ResourceKey {
    }
}
