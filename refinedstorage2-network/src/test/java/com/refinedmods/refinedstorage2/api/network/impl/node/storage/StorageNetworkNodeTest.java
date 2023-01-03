package com.refinedmods.refinedstorage2.api.network.impl.node.storage;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.core.filter.FilterMode;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.storage.AccessMode;
import com.refinedmods.refinedstorage2.api.storage.EmptyActor;
import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannel;
import com.refinedmods.refinedstorage2.api.storage.limited.LimitedStorage;
import com.refinedmods.refinedstorage2.api.storage.limited.LimitedStorageImpl;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedStorageImpl;
import com.refinedmods.refinedstorage2.network.test.AddNetworkNode;
import com.refinedmods.refinedstorage2.network.test.InjectNetworkStorageChannel;
import com.refinedmods.refinedstorage2.network.test.NetworkTest;
import com.refinedmods.refinedstorage2.network.test.SetupNetwork;
import com.refinedmods.refinedstorage2.network.test.util.FakeActor;

import java.util.Set;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import static com.refinedmods.refinedstorage2.network.test.nodefactory.AbstractNetworkNodeFactory.PROPERTY_ENERGY_USAGE;
import static org.assertj.core.api.Assertions.assertThat;

@NetworkTest
@SetupNetwork
class StorageNetworkNodeTest {
    private static final int ENERGY_USAGE = 5;

    @AddNetworkNode(properties = {
        @AddNetworkNode.Property(key = PROPERTY_ENERGY_USAGE, longValue = ENERGY_USAGE)
    })
    StorageNetworkNode<String> sut;

    @Test
    void testInitialState(@InjectNetworkStorageChannel final StorageChannel<String> networkStorage) {
        // Act
        final long inserted = networkStorage.insert("A", 10, Action.EXECUTE, EmptyActor.INSTANCE);
        final long extracted = networkStorage.extract("A", 10, Action.EXECUTE, EmptyActor.INSTANCE);

        // Assert
        assertThat(inserted).isZero();
        assertThat(extracted).isZero();
        assertThat(sut.getEnergyUsage()).isEqualTo(ENERGY_USAGE);
        assertThat(sut.getAccessMode()).isEqualTo(AccessMode.INSERT_EXTRACT);
        assertThat(sut.getFilterMode()).isEqualTo(FilterMode.BLOCK);
        assertThat(sut.getStored()).isZero();
        assertThat(sut.getCapacity()).isZero();
        assertThat(sut.getPriority()).isZero();
        assertThat(networkStorage.getAll()).isEmpty();
        assertThat(networkStorage.getStored()).isZero();
    }

    @Test
    void shouldInitialize(@InjectNetworkStorageChannel final StorageChannel<String> networkStorage) {
        // Arrange
        final LimitedStorage<String> limitedStorage = new LimitedStorageImpl<>(100);
        limitedStorage.insert("A", 50, Action.EXECUTE, EmptyActor.INSTANCE);

        // Act
        activateStorage(limitedStorage);

        // Assert
        assertThat(sut.getStored()).isEqualTo(50L);
        assertThat(sut.getCapacity()).isEqualTo(100L);
        assertThat(networkStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount<>("A", 50L)
        );
    }

    @Test
    void shouldInsert(@InjectNetworkStorageChannel final StorageChannel<String> networkStorage) {
        // Arrange
        final Storage<String> storage = new LimitedStorageImpl<>(100);
        activateStorage(storage);

        // Act
        final long inserted = networkStorage.insert("A", 100, Action.EXECUTE, EmptyActor.INSTANCE);

        // Assert
        assertThat(inserted).isEqualTo(100);
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount<>("A", 100)
        );
        assertThat(networkStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount<>("A", 100)
        );
    }

    @Test
    void shouldExtract(@InjectNetworkStorageChannel final StorageChannel<String> networkStorage) {
        // Arrange
        final Storage<String> storage = new LimitedStorageImpl<>(200);
        storage.insert("A", 100, Action.EXECUTE, EmptyActor.INSTANCE);
        storage.insert("B", 50, Action.EXECUTE, EmptyActor.INSTANCE);
        activateStorage(storage);

        // Act
        final long extracted = networkStorage.extract("A", 30, Action.EXECUTE, EmptyActor.INSTANCE);

        // Assert
        assertThat(extracted).isEqualTo(30);
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount<>("A", 70),
            new ResourceAmount<>("B", 50)
        );
        assertThat(networkStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount<>("A", 70),
            new ResourceAmount<>("B", 50)
        );
    }

    @Test
    void shouldRespectAllowlistWhenInserting(@InjectNetworkStorageChannel final StorageChannel<String> networkStorage) {
        // Arrange
        sut.setFilterMode(FilterMode.ALLOW);
        sut.setFilterTemplates(Set.of("A", "B"));

        final Storage<String> storage = new LimitedStorageImpl<>(100);
        activateStorage(storage);

        // Act
        final long inserted1 = networkStorage.insert("A", 12, Action.EXECUTE, EmptyActor.INSTANCE);
        final long inserted2 = networkStorage.insert("B", 12, Action.EXECUTE, EmptyActor.INSTANCE);
        final long inserted3 = networkStorage.insert("C", 10, Action.EXECUTE, EmptyActor.INSTANCE);

        // Assert
        assertThat(inserted1).isEqualTo(12);
        assertThat(inserted2).isEqualTo(12);
        assertThat(inserted3).isZero();
    }

    @Test
    void shouldRespectEmptyAllowlistWhenInserting(
        @InjectNetworkStorageChannel final StorageChannel<String> networkStorage) {
        // Arrange
        sut.setFilterMode(FilterMode.ALLOW);
        sut.setFilterTemplates(Set.of());

        final Storage<String> storage = new LimitedStorageImpl<>(100);
        activateStorage(storage);

        // Act
        final long inserted1 = networkStorage.insert("A", 12, Action.EXECUTE, EmptyActor.INSTANCE);
        final long inserted2 = networkStorage.insert("B", 12, Action.EXECUTE, EmptyActor.INSTANCE);
        final long inserted3 = networkStorage.insert("C", 10, Action.EXECUTE, EmptyActor.INSTANCE);

        // Assert
        assertThat(inserted1).isZero();
        assertThat(inserted2).isZero();
        assertThat(inserted3).isZero();
    }

    @Test
    void shouldRespectBlocklistWhenInserting(@InjectNetworkStorageChannel final StorageChannel<String> networkStorage) {
        // Arrange
        sut.setFilterMode(FilterMode.BLOCK);
        sut.setFilterTemplates(Set.of("A", "B"));

        final Storage<String> storage = new LimitedStorageImpl<>(100);
        activateStorage(storage);

        // Act
        final long inserted1 = networkStorage.insert("A", 12, Action.EXECUTE, EmptyActor.INSTANCE);
        final long inserted2 = networkStorage.insert("B", 12, Action.EXECUTE, EmptyActor.INSTANCE);
        final long inserted3 = networkStorage.insert("C", 10, Action.EXECUTE, EmptyActor.INSTANCE);

        // Assert
        assertThat(inserted1).isZero();
        assertThat(inserted2).isZero();
        assertThat(inserted3).isEqualTo(10);
    }

    @Test
    void shouldRespectEmptyBlocklistWhenInserting(
        @InjectNetworkStorageChannel final StorageChannel<String> networkStorage) {
        // Arrange
        sut.setFilterMode(FilterMode.BLOCK);
        sut.setFilterTemplates(Set.of());

        final Storage<String> storage = new LimitedStorageImpl<>(100);
        activateStorage(storage);

        // Act
        final long inserted1 = networkStorage.insert("A", 12, Action.EXECUTE, EmptyActor.INSTANCE);
        final long inserted2 = networkStorage.insert("B", 12, Action.EXECUTE, EmptyActor.INSTANCE);
        final long inserted3 = networkStorage.insert("C", 10, Action.EXECUTE, EmptyActor.INSTANCE);

        // Assert
        assertThat(inserted1).isEqualTo(12);
        assertThat(inserted2).isEqualTo(12);
        assertThat(inserted3).isEqualTo(10);
    }

    @ParameterizedTest
    @EnumSource(AccessMode.class)
    void shouldRespectAccessModeWhenInserting(final AccessMode accessMode,
                                              @InjectNetworkStorageChannel final StorageChannel<String> networkStorage
    ) {
        // Arrange
        sut.setAccessMode(accessMode);

        final Storage<String> storage = new LimitedStorageImpl<>(100);
        activateStorage(storage);

        // Act
        final long inserted = networkStorage.insert("A", 5, Action.EXECUTE, EmptyActor.INSTANCE);

        // Assert
        switch (accessMode) {
            case INSERT_EXTRACT, INSERT -> assertThat(inserted).isEqualTo(5);
            case EXTRACT -> assertThat(inserted).isZero();
        }
    }

    @ParameterizedTest
    @EnumSource(AccessMode.class)
    void shouldRespectAccessModeWhenExtracting(final AccessMode accessMode,
                                               @InjectNetworkStorageChannel final StorageChannel<String> networkStorage
    ) {
        // Arrange
        sut.setAccessMode(accessMode);

        final Storage<String> storage = new LimitedStorageImpl<>(100);
        storage.insert("A", 20, Action.EXECUTE, EmptyActor.INSTANCE);
        activateStorage(storage);

        // Act
        final long extracted = networkStorage.extract("A", 5, Action.EXECUTE, EmptyActor.INSTANCE);

        // Assert
        switch (accessMode) {
            case INSERT_EXTRACT, EXTRACT -> assertThat(extracted).isEqualTo(5);
            case INSERT -> assertThat(extracted).isZero();
        }
    }

    @Test
    void shouldNotInsertWhenInactive(@InjectNetworkStorageChannel final StorageChannel<String> networkStorage) {
        // Arrange
        final Storage<String> storage = new LimitedStorageImpl<>(100);
        activateStorage(storage);
        sut.setActive(false);

        // Act
        final long inserted = networkStorage.insert("A", 5, Action.EXECUTE, EmptyActor.INSTANCE);

        // Assert
        assertThat(inserted).isZero();
    }

    @Test
    void shouldNotExtractWhenInactive(@InjectNetworkStorageChannel final StorageChannel<String> networkStorage) {
        // Arrange
        final Storage<String> storage = new LimitedStorageImpl<>(100);
        activateStorage(storage);

        sut.setActive(false);

        // Act
        final long extracted = networkStorage.extract("A", 5, Action.EXECUTE, EmptyActor.INSTANCE);

        // Assert
        assertThat(extracted).isZero();
    }

    @Test
    void shouldHideStorageContentsWhenInactive(
        @InjectNetworkStorageChannel final StorageChannel<String> networkStorage
    ) {
        // Arrange
        final Storage<String> storage = new LimitedStorageImpl<>(100);
        storage.insert("A", 50, Action.EXECUTE, EmptyActor.INSTANCE);
        storage.insert("B", 50, Action.EXECUTE, EmptyActor.INSTANCE);
        activateStorage(storage);

        // Act
        sut.setActive(false);

        // Assert
        assertThat(networkStorage.getAll()).isEmpty();
    }

    @Test
    void shouldShowStorageContentsWhenActive(@InjectNetworkStorageChannel final StorageChannel<String> networkStorage) {
        // Arrange
        final Storage<String> storage = new LimitedStorageImpl<>(100);
        storage.insert("A", 50, Action.EXECUTE, EmptyActor.INSTANCE);
        storage.insert("B", 50, Action.EXECUTE, EmptyActor.INSTANCE);

        // Act
        activateStorage(storage);

        // Assert
        assertThat(networkStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount<>("A", 50),
            new ResourceAmount<>("B", 50)
        );
    }

    @Test
    void shouldTrackChanges(@InjectNetworkStorageChannel final StorageChannel<String> networkStorage) {
        // Arrange
        activateStorage(new TrackedStorageImpl<>(new LimitedStorageImpl<>(100), () -> 0L));

        // Act
        final long inserted = networkStorage.insert("A", 10, Action.EXECUTE, FakeActor.INSTANCE);

        // Assert
        assertThat(inserted).isEqualTo(10);
        assertThat(networkStorage.findTrackedResourceByActorType("A", FakeActor.class)).isNotEmpty();
    }

    private void activateStorage(final Storage<String> storage) {
        sut.setStorage(storage);
        sut.setActive(true);
    }

    @Nested
    class PriorityTest {
        @AddNetworkNode
        StorageNetworkNode<String> otherStorage;

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        void shouldRespectPriority(
            final boolean oneHasPriority,
            @InjectNetworkStorageChannel final StorageChannel<String> networkStorage
        ) {
            // Arrange
            final LimitedStorageImpl<String> storage1 = new LimitedStorageImpl<>(100);
            sut.setStorage(storage1);
            sut.setActive(true);

            final LimitedStorageImpl<String> storage2 = new LimitedStorageImpl<>(100);
            otherStorage.setStorage(storage2);
            otherStorage.setActive(true);

            if (oneHasPriority) {
                sut.setPriority(5);
                otherStorage.setPriority(2);
            } else {
                sut.setPriority(2);
                otherStorage.setPriority(5);
            }

            // Act
            networkStorage.insert("A", 1, Action.EXECUTE, EmptyActor.INSTANCE);

            // Assert
            if (oneHasPriority) {
                assertThat(storage1.getAll()).isNotEmpty();
                assertThat(storage2.getAll()).isEmpty();
            } else {
                assertThat(storage1.getAll()).isEmpty();
                assertThat(storage2.getAll()).isNotEmpty();
            }
        }
    }
}
