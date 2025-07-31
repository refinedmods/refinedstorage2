package com.refinedmods.refinedstorage.api.storage.composite;

import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.list.MutableResourceListImpl;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.api.storage.ActorFixtures;
import com.refinedmods.refinedstorage.api.storage.Storage;
import com.refinedmods.refinedstorage.api.storage.StorageImpl;
import com.refinedmods.refinedstorage.api.storage.limited.LimitedStorageImpl;
import com.refinedmods.refinedstorage.api.storage.tracked.TrackedResource;
import com.refinedmods.refinedstorage.api.storage.tracked.TrackedStorage;
import com.refinedmods.refinedstorage.api.storage.tracked.TrackedStorageImpl;

import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static com.refinedmods.refinedstorage.api.storage.TestResource.A;
import static com.refinedmods.refinedstorage.api.storage.TestResource.B;
import static com.refinedmods.refinedstorage.api.storage.TestResource.C;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CompositeStorageImplTest {
    private CompositeStorageImpl sut;

    @BeforeEach
    void setUp() {
        sut = new CompositeStorageImpl(MutableResourceListImpl.create());
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
        storage1.insert(A, 10, Action.EXECUTE, Actor.EMPTY);

        final Storage storage2 = new LimitedStorageImpl(10);
        storage2.insert(B, 5, Action.EXECUTE, Actor.EMPTY);

        final Storage storage3 = new LimitedStorageImpl(10);
        storage3.insert(C, 7, Action.EXECUTE, Actor.EMPTY);
        storage3.insert(A, 3, Action.EXECUTE, Actor.EMPTY);

        // Act
        sut.addSource(storage1);
        sut.addSource(storage2);
        sut.addSource(storage3);

        final long inserted = sut.insert(B, 6, Action.SIMULATE, Actor.EMPTY);

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
        storage1.insert(A, 10, Action.EXECUTE, Actor.EMPTY);

        final Storage storage2 = new LimitedStorageImpl(10);
        storage2.insert(B, 5, Action.EXECUTE, Actor.EMPTY);

        final Storage storage3 = new LimitedStorageImpl(10);
        storage3.insert(C, 7, Action.EXECUTE, Actor.EMPTY);
        storage3.insert(A, 3, Action.EXECUTE, Actor.EMPTY);

        sut.addSource(storage1);
        sut.addSource(storage2);
        sut.addSource(storage3);

        // Act
        sut.removeSource(storage3);

        final long extracted = sut.extract(C, 1, Action.EXECUTE, Actor.EMPTY);

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
        storage1.insert(A, 10, Action.EXECUTE, Actor.EMPTY);

        final Storage storage2 = new LimitedStorageImpl(10);
        storage2.insert(B, 5, Action.EXECUTE, Actor.EMPTY);

        final Storage storage3 = new LimitedStorageImpl(10);
        storage3.insert(C, 7, Action.EXECUTE, Actor.EMPTY);
        storage3.insert(A, 3, Action.EXECUTE, Actor.EMPTY);

        sut.addSource(storage1);
        sut.addSource(storage2);
        sut.addSource(storage3);

        // Act
        sut.clearSources();

        final long extracted = sut.extract(C, 1, Action.EXECUTE, Actor.EMPTY);

        // Assert
        assertThat(sut.getAll()).isEmpty();
        assertThat(sut.getSources()).isEmpty();
        assertThat(extracted).isZero();
    }

    @Test
    void shouldRespectInsertPriorityWhenAddingNewSources() {
        // Arrange
        final Storage storage1 = PriorityStorage.of(new LimitedStorageImpl(10), 20, 30);
        final Storage storage2 = PriorityStorage.of(new LimitedStorageImpl(10), 10, 10);
        final Storage storage3 = PriorityStorage.of(new LimitedStorageImpl(10), 30, 20);

        // Act
        sut.addSource(storage1);
        sut.addSource(storage2);
        sut.addSource(storage3);

        final long inserted = sut.insert(A, 12, Action.EXECUTE, Actor.EMPTY);

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
    void shouldRespectExtractPriorityWhenAddingNewSources() {
        // Arrange
        final Storage storage1 = PriorityStorage.of(new LimitedStorageImpl(10), 20, 30);
        final Storage storage2 = PriorityStorage.of(new LimitedStorageImpl(10), 10, 10);
        final Storage storage3 = PriorityStorage.of(new LimitedStorageImpl(10), 30, 20);

        // Act
        sut.addSource(storage1);
        sut.addSource(storage2);
        sut.addSource(storage3);
        sut.insert(A, 30, Action.EXECUTE, Actor.EMPTY);

        final long extracted = sut.extract(A, 12, Action.EXECUTE, Actor.EMPTY);

        // Assert
        assertThat(sut.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 18)
        );
        assertThat(sut.getSources()).containsExactly(storage3, storage1, storage2);
        assertThat(extracted).isEqualTo(12);
        assertThat(storage1.getStored()).isZero();
        assertThat(storage3.getStored()).isEqualTo(8);
        assertThat(storage2.getStored()).isEqualTo(10);
    }

    @Test
    void shouldRespectInsertPriorityWhenRemovingSources() {
        // Arrange
        final Storage storage1 = PriorityStorage.of(new LimitedStorageImpl(10), 20, 30);
        final Storage storage2 = PriorityStorage.of(new LimitedStorageImpl(10), 10, 10);
        final Storage storage3 = PriorityStorage.of(new LimitedStorageImpl(10), 30, 20);

        sut.addSource(storage1);
        sut.addSource(storage2);
        sut.addSource(storage3);
        sut.removeSource(storage3);

        // Act
        final long inserted = sut.insert(A, 12, Action.EXECUTE, Actor.EMPTY);

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
    void shouldRespectExtractPriorityWhenRemovingSources() {
        // Arrange
        final Storage storage1 = PriorityStorage.of(new LimitedStorageImpl(10), 20, 30);
        final Storage storage2 = PriorityStorage.of(new LimitedStorageImpl(10), 10, 10);
        final Storage storage3 = PriorityStorage.of(new LimitedStorageImpl(10), 30, 20);

        sut.addSource(storage1);
        sut.addSource(storage2);
        sut.addSource(storage3);
        sut.insert(A, 30, Action.EXECUTE, Actor.EMPTY);
        sut.removeSource(storage3);

        // Act
        final long extracted = sut.extract(A, 12, Action.EXECUTE, Actor.EMPTY);

        // Assert
        assertThat(sut.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 8)
        );
        assertThat(sut.getSources()).containsExactly(storage1, storage2);
        assertThat(extracted).isEqualTo(12);
        assertThat(storage1.getStored()).isZero();
        assertThat(storage2.getStored()).isEqualTo(8);
        assertThat(storage3.getStored()).isEqualTo(10);
    }

    @Test
    void shouldOnlyRespectInsertPriorityWhenSortingSourcesExplicitlyWhenChangingPriorityAfterAddingSource() {
        // Arrange
        final PriorityStorage storage1 = PriorityStorage.of(new LimitedStorageImpl(10), 1, 2);
        final Storage storage2 = PriorityStorage.of(new LimitedStorageImpl(10), 2, 1);

        sut.addSource(storage1);
        sut.addSource(storage2);

        // Act & assert
        sut.insert(A, 1, Action.EXECUTE, Actor.EMPTY);
        assertThat(storage1.getStored()).isZero();
        assertThat(storage2.getStored()).isEqualTo(1);

        storage1.setInsertPriority(3);

        sut.insert(A, 1, Action.EXECUTE, Actor.EMPTY);
        assertThat(storage1.getStored()).isZero();
        assertThat(storage2.getStored()).isEqualTo(2);

        sut.sortSources();

        sut.insert(A, 1, Action.EXECUTE, Actor.EMPTY);
        assertThat(storage1.getStored()).isEqualTo(1);
        assertThat(storage2.getStored()).isEqualTo(2);
    }

    @Test
    void shouldOnlyRespectExtractPriorityWhenSortingSourcesExplicitlyWhenChangingPriorityAfterAddingSource() {
        // Arrange
        final PriorityStorage storage1 = PriorityStorage.of(new LimitedStorageImpl(10), 1, 2);
        final Storage storage2 = PriorityStorage.of(new LimitedStorageImpl(10), 2, 1);

        sut.addSource(storage1);
        sut.addSource(storage2);
        sut.insert(A, 20, Action.EXECUTE, Actor.EMPTY);

        // Act & assert
        sut.extract(A, 1, Action.EXECUTE, Actor.EMPTY);
        assertThat(storage1.getStored()).isEqualTo(9);
        assertThat(storage2.getStored()).isEqualTo(10);

        storage1.setExtractPriority(0);

        sut.extract(A, 1, Action.EXECUTE, Actor.EMPTY);
        assertThat(storage1.getStored()).isEqualTo(8);
        assertThat(storage2.getStored()).isEqualTo(10);

        sut.sortSources();

        sut.extract(A, 1, Action.EXECUTE, Actor.EMPTY);
        assertThat(storage1.getStored()).isEqualTo(8);
        assertThat(storage2.getStored()).isEqualTo(9);
    }

    @Test
    void shouldFindMostRecentChange() {
        // Arrange
        final AtomicLong clock = new AtomicLong(0L);

        final TrackedStorage a = new TrackedStorageImpl(new StorageImpl(), clock::get);
        final TrackedStorage b = new TrackedStorageImpl(new StorageImpl(), clock::get);

        // Test if it uses the latest across 2 different storages
        a.insert(A, 1, Action.EXECUTE, ActorFixtures.ActorFixture1.INSTANCE);
        clock.set(1L);
        b.insert(A, 1, Action.EXECUTE, ActorFixtures.ActorFixture1.INSTANCE);

        // Test if it differentiates between source types properly
        clock.set(2L);
        b.insert(B, 1, Action.EXECUTE, ActorFixtures.ActorFixture1.INSTANCE);
        clock.set(3L);
        b.insert(B, 1, Action.EXECUTE, ActorFixtures.ActorFixture2.INSTANCE);

        sut.addSource(a);
        sut.addSource(b);

        // Act
        final var oneOne = sut.findTrackedResourceByActorType(A, ActorFixtures.ActorFixture1.class);
        final var oneTwo = sut.findTrackedResourceByActorType(A, ActorFixtures.ActorFixture2.class);
        final var twoOne = sut.findTrackedResourceByActorType(B, ActorFixtures.ActorFixture1.class);
        final var twoTwo = sut.findTrackedResourceByActorType(B, ActorFixtures.ActorFixture2.class);

        // Assert
        assertThat(oneOne).get().usingRecursiveComparison().isEqualTo(new TrackedResource("Source1", 1L));
        assertThat(oneTwo).isEmpty();
        assertThat(twoOne).get().usingRecursiveComparison().isEqualTo(new TrackedResource("Source1", 2L));
        assertThat(twoTwo).get().usingRecursiveComparison().isEqualTo(new TrackedResource("Source2", 3L));
    }

    @ParameterizedTest
    @ValueSource(longs = {0L, -1L})
    void shouldNotExtractInvalidAmount(final long amount) {
        // Arrange
        final Storage storage = new LimitedStorageImpl(10);
        storage.insert(A, 10, Action.EXECUTE, Actor.EMPTY);
        sut.addSource(storage);

        // Act & Assert
        assertThatThrownBy(() -> sut.extract(A, amount, Action.EXECUTE, Actor.EMPTY))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Amount must be larger than 0");
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    void shouldNotExtractInvalidResource() {
        // Arrange
        final Storage storage = new LimitedStorageImpl(10);
        storage.insert(A, 10, Action.EXECUTE, Actor.EMPTY);
        sut.addSource(storage);

        // Act & Assert
        assertThatThrownBy(() -> sut.extract(null, 1, Action.EXECUTE, Actor.EMPTY))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("Resource must not be null");
    }

    @ParameterizedTest
    @ValueSource(longs = {0L, -1L})
    void shouldNotInsertInvalidAmount(final long amount) {
        // Arrange
        final Storage storage = new LimitedStorageImpl(10);
        storage.insert(A, 10, Action.EXECUTE, Actor.EMPTY);
        sut.addSource(storage);

        // Act & Assert
        assertThatThrownBy(() -> sut.insert(A, amount, Action.EXECUTE, Actor.EMPTY))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Amount must be larger than 0");
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    void shouldNotInsertInvalidResource() {
        // Arrange
        final Storage storage = new LimitedStorageImpl(10);
        storage.insert(A, 10, Action.EXECUTE, Actor.EMPTY);
        sut.addSource(storage);

        // Act & Assert
        assertThatThrownBy(() -> sut.insert(null, 1, Action.EXECUTE, Actor.EMPTY))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("Resource must not be null");
    }
}
