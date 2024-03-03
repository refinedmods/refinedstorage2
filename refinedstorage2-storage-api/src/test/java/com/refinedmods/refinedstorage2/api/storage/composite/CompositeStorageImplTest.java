package com.refinedmods.refinedstorage2.api.storage.composite;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.resource.list.ResourceListImpl;
import com.refinedmods.refinedstorage2.api.storage.EmptyActor;
import com.refinedmods.refinedstorage2.api.storage.FakeActors;
import com.refinedmods.refinedstorage2.api.storage.InMemoryStorageImpl;
import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.limited.LimitedStorageImpl;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedResource;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedStorage;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedStorageImpl;

import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.refinedmods.refinedstorage2.api.storage.TestResource.A;
import static com.refinedmods.refinedstorage2.api.storage.TestResource.B;
import static com.refinedmods.refinedstorage2.api.storage.TestResource.C;
import static org.assertj.core.api.Assertions.assertThat;

class CompositeStorageImplTest {
    private CompositeStorageImpl sut;

    @BeforeEach
    void setUp() {
        sut = new CompositeStorageImpl(new ResourceListImpl());
    }

    @Test
    void testInitialState() {
        // Act & assert
        assertThat(sut.getAll()).isEmpty();
        assertThat(sut.getStored()).isZero();
    }

    @Test
    void shouldAddSource() {
        // Arrange
        final Storage storage1 = new LimitedStorageImpl(10);
        storage1.insert(A, 10, Action.EXECUTE, EmptyActor.INSTANCE);

        final Storage storage2 = new LimitedStorageImpl(10);
        storage2.insert(B, 5, Action.EXECUTE, EmptyActor.INSTANCE);

        final Storage storage3 = new LimitedStorageImpl(10);
        storage3.insert(C, 7, Action.EXECUTE, EmptyActor.INSTANCE);
        storage3.insert(A, 3, Action.EXECUTE, EmptyActor.INSTANCE);

        // Act
        sut.addSource(storage1);
        sut.addSource(storage2);
        sut.addSource(storage3);

        final long inserted = sut.insert(B, 6, Action.SIMULATE, EmptyActor.INSTANCE);

        // Assert
        assertThat(sut.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 13),
            new ResourceAmount(B, 5),
            new ResourceAmount(C, 7)
        );
        assertThat(sut.getSources()).containsExactly(storage1, storage2, storage3);
        assertThat(inserted).isEqualTo(5);
    }

    @Test
    void shouldRemoveSource() {
        // Arrange
        final Storage storage1 = new LimitedStorageImpl(10);
        storage1.insert(A, 10, Action.EXECUTE, EmptyActor.INSTANCE);

        final Storage storage2 = new LimitedStorageImpl(10);
        storage2.insert(B, 5, Action.EXECUTE, EmptyActor.INSTANCE);

        final Storage storage3 = new LimitedStorageImpl(10);
        storage3.insert(C, 7, Action.EXECUTE, EmptyActor.INSTANCE);
        storage3.insert(A, 3, Action.EXECUTE, EmptyActor.INSTANCE);

        sut.addSource(storage1);
        sut.addSource(storage2);
        sut.addSource(storage3);

        // Act
        sut.removeSource(storage3);

        final long extracted = sut.extract(C, 1, Action.EXECUTE, EmptyActor.INSTANCE);

        // Assert
        assertThat(sut.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 10),
            new ResourceAmount(B, 5)
        );
        assertThat(sut.getSources()).containsExactly(storage1, storage2);
        assertThat(extracted).isZero();
    }

    @Test
    void shouldClearSources() {
        // Arrange
        final Storage storage1 = new LimitedStorageImpl(10);
        storage1.insert(A, 10, Action.EXECUTE, EmptyActor.INSTANCE);

        final Storage storage2 = new LimitedStorageImpl(10);
        storage2.insert(B, 5, Action.EXECUTE, EmptyActor.INSTANCE);

        final Storage storage3 = new LimitedStorageImpl(10);
        storage3.insert(C, 7, Action.EXECUTE, EmptyActor.INSTANCE);
        storage3.insert(A, 3, Action.EXECUTE, EmptyActor.INSTANCE);

        sut.addSource(storage1);
        sut.addSource(storage2);
        sut.addSource(storage3);

        // Act
        sut.clearSources();

        final long extracted = sut.extract(C, 1, Action.EXECUTE, EmptyActor.INSTANCE);

        // Assert
        assertThat(sut.getAll()).isEmpty();
        assertThat(sut.getSources()).isEmpty();
        assertThat(extracted).isZero();
    }

    @Test
    void shouldRespectPriorityWhenAddingNewSources() {
        // Arrange
        final Storage storage1 = new PrioritizedStorage(20, new LimitedStorageImpl(10));
        final Storage storage2 = new PrioritizedStorage(10, new LimitedStorageImpl(10));
        final Storage storage3 = new PrioritizedStorage(30, new LimitedStorageImpl(10));

        // Act
        sut.addSource(storage1);
        sut.addSource(storage2);
        sut.addSource(storage3);

        final long inserted = sut.insert(A, 12, Action.EXECUTE, EmptyActor.INSTANCE);

        // Assert
        assertThat(sut.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 12)
        );
        assertThat(sut.getSources()).containsExactly(storage3, storage1, storage2);
        assertThat(inserted).isEqualTo(12);
        assertThat(storage3.getStored()).isEqualTo(10);
        assertThat(storage1.getStored()).isEqualTo(2);
        assertThat(storage2.getStored()).isZero();
    }

    @Test
    void shouldRespectPriorityWhenRemovingSources() {
        // Arrange
        final Storage storage1 = new PrioritizedStorage(20, new LimitedStorageImpl(10));
        final Storage storage2 = new PrioritizedStorage(10, new LimitedStorageImpl(10));
        final Storage storage3 = new PrioritizedStorage(30, new LimitedStorageImpl(10));

        sut.addSource(storage1);
        sut.addSource(storage2);
        sut.addSource(storage3);
        sut.removeSource(storage3);

        // Act
        final long inserted = sut.insert(A, 12, Action.EXECUTE, EmptyActor.INSTANCE);

        // Assert
        assertThat(sut.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 12)
        );
        assertThat(sut.getSources()).containsExactly(storage1, storage2);
        assertThat(inserted).isEqualTo(12);
        assertThat(storage1.getStored()).isEqualTo(10);
        assertThat(storage2.getStored()).isEqualTo(2);
        assertThat(storage3.getStored()).isZero();
    }

    @Test
    void shouldOnlyRespectPriorityWhenSortingSourcesExplicitlyWhenChangingPriorityAfterAddingSource() {
        // Arrange
        final PrioritizedStorage storage1 = new PrioritizedStorage(1, new LimitedStorageImpl(10));
        final Storage storage2 = new PrioritizedStorage(2, new LimitedStorageImpl(10));

        sut.addSource(storage1);
        sut.addSource(storage2);

        // Act & assert
        sut.insert(A, 1, Action.EXECUTE, EmptyActor.INSTANCE);
        assertThat(storage1.getStored()).isZero();
        assertThat(storage2.getStored()).isEqualTo(1);

        storage1.setPriority(3);

        sut.insert(A, 1, Action.EXECUTE, EmptyActor.INSTANCE);
        assertThat(storage1.getStored()).isZero();
        assertThat(storage2.getStored()).isEqualTo(2);

        sut.sortSources();

        sut.insert(A, 1, Action.EXECUTE, EmptyActor.INSTANCE);
        assertThat(storage1.getStored()).isEqualTo(1);
        assertThat(storage2.getStored()).isEqualTo(2);
    }

    @Test
    void shouldFindMostRecentChange() {
        // Arrange
        final AtomicLong clock = new AtomicLong(0L);

        final TrackedStorage a = new TrackedStorageImpl(new InMemoryStorageImpl(), clock::get);
        final TrackedStorage b = new TrackedStorageImpl(new InMemoryStorageImpl(), clock::get);

        // Test if it uses the latest across 2 different storages
        a.insert(A, 1, Action.EXECUTE, FakeActors.FakeActor1.INSTANCE);
        clock.set(1L);
        b.insert(A, 1, Action.EXECUTE, FakeActors.FakeActor1.INSTANCE);

        // Test if it differentiates between source types properly
        clock.set(2L);
        b.insert(B, 1, Action.EXECUTE, FakeActors.FakeActor1.INSTANCE);
        clock.set(3L);
        b.insert(B, 1, Action.EXECUTE, FakeActors.FakeActor2.INSTANCE);

        sut.addSource(a);
        sut.addSource(b);

        // Act
        final var oneOne = sut.findTrackedResourceByActorType(A, FakeActors.FakeActor1.class);
        final var oneTwo = sut.findTrackedResourceByActorType(A, FakeActors.FakeActor2.class);
        final var twoOne = sut.findTrackedResourceByActorType(B, FakeActors.FakeActor1.class);
        final var twoTwo = sut.findTrackedResourceByActorType(B, FakeActors.FakeActor2.class);

        // Assert
        assertThat(oneOne).get().usingRecursiveComparison().isEqualTo(new TrackedResource("Source1", 1L));
        assertThat(oneTwo).isEmpty();
        assertThat(twoOne).get().usingRecursiveComparison().isEqualTo(new TrackedResource("Source1", 2L));
        assertThat(twoTwo).get().usingRecursiveComparison().isEqualTo(new TrackedResource("Source2", 3L));
    }
}
