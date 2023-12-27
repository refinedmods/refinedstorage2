package com.refinedmods.refinedstorage2.api.network.impl.node.multistorage;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.core.filter.FilterMode;
import com.refinedmods.refinedstorage2.api.network.Network;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.storage.AccessMode;
import com.refinedmods.refinedstorage2.api.storage.EmptyActor;
import com.refinedmods.refinedstorage2.api.storage.InMemoryStorageImpl;
import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannel;
import com.refinedmods.refinedstorage2.api.storage.limited.LimitedStorageImpl;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedStorageImpl;
import com.refinedmods.refinedstorage2.network.test.AddNetworkNode;
import com.refinedmods.refinedstorage2.network.test.InjectNetwork;
import com.refinedmods.refinedstorage2.network.test.InjectNetworkStorageChannel;
import com.refinedmods.refinedstorage2.network.test.NetworkTest;
import com.refinedmods.refinedstorage2.network.test.SetupNetwork;
import com.refinedmods.refinedstorage2.network.test.util.FakeActor;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import static com.refinedmods.refinedstorage2.network.test.nodefactory.AbstractNetworkNodeFactory.PROPERTY_ENERGY_USAGE;
import static com.refinedmods.refinedstorage2.network.test.nodefactory.MultiStorageNetworkNodeFactory.PROPERTY_ENERGY_USAGE_PER_STORAGE;
import static org.assertj.core.api.Assertions.assertThat;

@NetworkTest
@SetupNetwork
class MultiStorageNetworkNodeTest {
    private static final long BASE_USAGE = 10;
    private static final long USAGE_PER_STORAGE = 3;

    @AddNetworkNode(properties = {
        @AddNetworkNode.Property(key = PROPERTY_ENERGY_USAGE, longValue = BASE_USAGE),
        @AddNetworkNode.Property(key = PROPERTY_ENERGY_USAGE_PER_STORAGE, longValue = USAGE_PER_STORAGE)
    })
    MultiStorageNetworkNode sut;

    MultiStorageProviderImpl provider;

    @BeforeEach
    void setUp() {
        provider = new MultiStorageProviderImpl();
    }

    @Test
    void shouldInitializeButNotShowResourcesYet(
        @InjectNetworkStorageChannel final StorageChannel<String> networkStorage
    ) {
        // Arrange
        final Storage<String> storage = new LimitedStorageImpl<>(10);
        storage.insert("A", 5, Action.EXECUTE, EmptyActor.INSTANCE);
        provider.set(1, storage);

        // Act
        sut.setProvider(provider);

        // Assert
        assertThat(sut.getEnergyUsage()).isEqualTo(BASE_USAGE + USAGE_PER_STORAGE);
        assertThat(networkStorage.getAll()).isEmpty();
        assertThat(networkStorage.getStored()).isZero();
    }

    @Test
    void shouldInitializeAndShowResourcesAfterEnabling(
        @InjectNetworkStorageChannel final StorageChannel<String> networkStorage
    ) {
        // Arrange
        final Storage<String> storage = new LimitedStorageImpl<>(100);
        storage.insert("A", 50, Action.EXECUTE, EmptyActor.INSTANCE);
        storage.insert("B", 50, Action.EXECUTE, EmptyActor.INSTANCE);
        provider.set(1, storage);

        // Act
        sut.setProvider(provider);
        sut.setActive(true);

        // Assert
        assertThat(networkStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount<>("A", 50),
            new ResourceAmount<>("B", 50)
        );
    }

    @Test
    void shouldInitializeMultipleTimes(
        @InjectNetworkStorageChannel final StorageChannel<String> networkStorage
    ) {
        // Arrange
        final Storage<String> storage1 = new LimitedStorageImpl<>(10);
        storage1.insert("A", 5, Action.EXECUTE, EmptyActor.INSTANCE);
        provider.set(8, storage1);
        sut.setProvider(provider);
        sut.setActive(true);

        final Storage<String> storage2 = new LimitedStorageImpl<>(10);
        storage2.insert("B", 5, Action.EXECUTE, EmptyActor.INSTANCE);
        provider.set(8, storage2);

        // Act
        sut.setProvider(provider);

        // Assert
        assertThat(sut.getEnergyUsage()).isEqualTo(BASE_USAGE + USAGE_PER_STORAGE);
        assertThat(networkStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount<>("B", 5)
        );
    }

    @Test
    void testInitialState(@InjectNetworkStorageChannel final StorageChannel<String> networkStorage) {
        // Assert
        assertThat(sut.getEnergyUsage()).isEqualTo(BASE_USAGE);
        assertThat(sut.getFilterMode()).isEqualTo(FilterMode.BLOCK);
        assertThat(networkStorage.getAll()).isEmpty();
        assertThat(networkStorage.getStored()).isZero();
        assertThat(sut.getSize()).isEqualTo(9);
        for (int i = 0; i < 9; ++i) {
            assertThat(sut.getState(i)).isEqualTo(MultiStorageStorageState.NONE);
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testState(final boolean active) {
        // Arrange
        final Storage<String> normalStorage = new LimitedStorageImpl<>(100);
        normalStorage.insert("A", 74, Action.EXECUTE, EmptyActor.INSTANCE);

        final Storage<String> nearCapacityStorage = new LimitedStorageImpl<>(100);
        nearCapacityStorage.insert("A", 75, Action.EXECUTE, EmptyActor.INSTANCE);

        final Storage<String> fullStorage = new LimitedStorageImpl<>(100);
        fullStorage.insert("A", 100, Action.EXECUTE, EmptyActor.INSTANCE);

        final Storage<String> unlimitedStorage = new InMemoryStorageImpl<>();

        provider.set(2, unlimitedStorage);
        provider.set(3, normalStorage);
        provider.set(5, nearCapacityStorage);
        provider.set(7, fullStorage);

        // Act
        sut.setProvider(provider);
        sut.setActive(active);

        // Assert
        assertThat(sut.getState(0)).isEqualTo(MultiStorageStorageState.NONE);
        assertThat(sut.getState(1)).isEqualTo(MultiStorageStorageState.NONE);
        assertThat(sut.getState(2)).isEqualTo(
            active ? MultiStorageStorageState.NORMAL : MultiStorageStorageState.INACTIVE);
        assertThat(sut.getState(3)).isEqualTo(
            active ? MultiStorageStorageState.NORMAL : MultiStorageStorageState.INACTIVE);
        assertThat(sut.getState(4)).isEqualTo(MultiStorageStorageState.NONE);
        assertThat(sut.getState(5)).isEqualTo(
            active ? MultiStorageStorageState.NEAR_CAPACITY : MultiStorageStorageState.INACTIVE);
        assertThat(sut.getState(6)).isEqualTo(MultiStorageStorageState.NONE);
        assertThat(sut.getState(7)).isEqualTo(
            active ? MultiStorageStorageState.FULL : MultiStorageStorageState.INACTIVE);
        assertThat(sut.getEnergyUsage()).isEqualTo(BASE_USAGE + (USAGE_PER_STORAGE * 4));
    }

    @Test
    void shouldDetectNewStorage(@InjectNetworkStorageChannel final StorageChannel<String> networkStorage) {
        // Arrange
        initializeAndActivate();

        final Storage<String> storage = new LimitedStorageImpl<>(10);
        storage.insert("A", 5, Action.EXECUTE, EmptyActor.INSTANCE);
        provider.set(8, storage);

        // Act
        sut.onStorageChanged(8);

        // Assert
        assertThat(sut.getEnergyUsage()).isEqualTo(BASE_USAGE + USAGE_PER_STORAGE);
        assertThat(networkStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount<>("A", 5)
        );
    }

    @Test
    void shouldDetectChangedStorage(@InjectNetworkStorageChannel final StorageChannel<String> networkStorage) {
        // Arrange
        final Storage<String> originalStorage = new LimitedStorageImpl<>(10);
        originalStorage.insert("A", 5, Action.EXECUTE, EmptyActor.INSTANCE);
        provider.set(0, originalStorage);
        initializeAndActivate();

        final Storage<String> replacedStorage = new LimitedStorageImpl<>(10);
        replacedStorage.insert("B", 2, Action.EXECUTE, EmptyActor.INSTANCE);
        provider.set(0, replacedStorage);

        // Act
        final Collection<ResourceAmount<String>> preChanging = new HashSet<>(networkStorage.getAll());
        sut.onStorageChanged(0);
        final Collection<ResourceAmount<String>> postChanging = networkStorage.getAll();

        // Assert
        assertThat(sut.getEnergyUsage()).isEqualTo(BASE_USAGE + USAGE_PER_STORAGE);
        assertThat(preChanging).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount<>("A", 5)
        );
        assertThat(postChanging).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount<>("B", 2)
        );
        assertThat(networkStorage.getStored()).isEqualTo(2L);
    }

    @Test
    void shouldDetectRemovedStorage(@InjectNetworkStorageChannel final StorageChannel<String> networkStorage) {
        // Arrange
        final Storage<String> storage = new LimitedStorageImpl<>(10);
        storage.insert("A", 5, Action.EXECUTE, EmptyActor.INSTANCE);
        provider.set(7, storage);
        initializeAndActivate();

        provider.remove(7);

        // Act
        final Collection<ResourceAmount<String>> preRemoval = new HashSet<>(networkStorage.getAll());
        sut.onStorageChanged(7);
        final Collection<ResourceAmount<String>> postRemoval = networkStorage.getAll();

        // Assert
        assertThat(sut.getEnergyUsage()).isEqualTo(BASE_USAGE);
        assertThat(preRemoval).isNotEmpty();
        assertThat(postRemoval).isEmpty();
        assertThat(networkStorage.getStored()).isZero();
    }

    @Test
    void shouldNotDetectStorageChangeInInvalidIndex() {
        // Act
        sut.onStorageChanged(-1);
        sut.onStorageChanged(9);

        // Assert
        assertThat(sut.getSize()).isEqualTo(9);
        for (int i = 0; i < 9; ++i) {
            assertThat(sut.getState(i)).isEqualTo(MultiStorageStorageState.NONE);
        }
    }

    @Test
    void shouldNotUpdateNetworkStorageWhenChangingStorageWhenInactive(
        @InjectNetworkStorageChannel final StorageChannel<String> networkStorage
    ) {
        // Arrange
        final Storage<String> storage = new LimitedStorageImpl<>(100);
        storage.insert("A", 50, Action.EXECUTE, EmptyActor.INSTANCE);
        storage.insert("B", 50, Action.EXECUTE, EmptyActor.INSTANCE);
        provider.set(1, storage);
        initializeAndActivate();

        // Act
        final Collection<ResourceAmount<String>> preInactiveness = new HashSet<>(networkStorage.getAll());
        sut.setActive(false);
        sut.onStorageChanged(1);
        final Collection<ResourceAmount<String>> postInactiveness = networkStorage.getAll();

        // Assert
        assertThat(preInactiveness).isNotEmpty();
        assertThat(postInactiveness).isEmpty();
        assertThat(networkStorage.getStored()).isZero();
    }

    @Test
    void shouldHaveResourcesFromStoragePresentInNetwork(
        @InjectNetworkStorageChannel final StorageChannel<String> networkStorage
    ) {
        // Arrange
        final Storage<String> storage = new LimitedStorageImpl<>(100);
        storage.insert("A", 50, Action.EXECUTE, EmptyActor.INSTANCE);
        storage.insert("B", 50, Action.EXECUTE, EmptyActor.INSTANCE);
        provider.set(1, storage);

        initializeAndActivate();

        // Act
        final Collection<ResourceAmount<String>> resources = networkStorage.getAll();
        final long stored = networkStorage.getStored();

        // Assert
        assertThat(resources).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount<>("A", 50),
            new ResourceAmount<>("B", 50)
        );
        assertThat(stored).isEqualTo(100);
    }

    @Test
    void shouldInsert(@InjectNetworkStorageChannel final StorageChannel<String> networkStorage) {
        // Arrange
        final Storage<String> storage1 = new LimitedStorageImpl<>(100);
        provider.set(1, storage1);

        final Storage<String> storage2 = new LimitedStorageImpl<>(100);
        provider.set(2, storage2);

        final Storage<String> storage3 = new LimitedStorageImpl<>(100);
        provider.set(3, storage3);

        initializeAndActivate();

        // Act
        final long inserted1 = networkStorage.insert("A", 150, Action.EXECUTE, EmptyActor.INSTANCE);
        final long inserted2 = networkStorage.insert("A", 10, Action.EXECUTE, EmptyActor.INSTANCE);
        final long inserted3 = networkStorage.insert("B", 300, Action.EXECUTE, EmptyActor.INSTANCE);

        // Assert
        assertThat(inserted1).isEqualTo(150);
        assertThat(inserted2).isEqualTo(10);
        assertThat(inserted3).isEqualTo(140);

        assertThat(storage1.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount<>("A", 100)
        );
        assertThat(storage2.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount<>("A", 60),
            new ResourceAmount<>("B", 40)
        );
        assertThat(storage3.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount<>("B", 100)
        );

        assertThat(networkStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount<>("B", 140),
            new ResourceAmount<>("A", 160)
        );
        assertThat(networkStorage.getStored()).isEqualTo(inserted1 + inserted2 + inserted3);
    }

    @Test
    void shouldExtract(@InjectNetworkStorageChannel final StorageChannel<String> networkStorage) {
        // Arrange
        final Storage<String> storage1 = new LimitedStorageImpl<>(100);
        storage1.insert("A", 50, Action.EXECUTE, EmptyActor.INSTANCE);
        storage1.insert("B", 50, Action.EXECUTE, EmptyActor.INSTANCE);
        provider.set(1, storage1);

        final Storage<String> storage2 = new LimitedStorageImpl<>(100);
        storage2.insert("A", 50, Action.EXECUTE, EmptyActor.INSTANCE);
        storage2.insert("B", 50, Action.EXECUTE, EmptyActor.INSTANCE);
        provider.set(2, storage2);

        final Storage<String> storage3 = new LimitedStorageImpl<>(100);
        storage3.insert("C", 10, Action.EXECUTE, EmptyActor.INSTANCE);
        provider.set(3, storage3);

        initializeAndActivate();

        // Act
        final long extracted = networkStorage.extract("A", 85, Action.EXECUTE, EmptyActor.INSTANCE);

        // Assert
        assertThat(extracted).isEqualTo(85);

        assertThat(storage1.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount<>("B", 50)
        );
        assertThat(storage2.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount<>("B", 50),
            new ResourceAmount<>("A", 15)
        );
        assertThat(storage3.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount<>("C", 10)
        );

        assertThat(networkStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount<>("B", 100),
            new ResourceAmount<>("A", 15),
            new ResourceAmount<>("C", 10)
        );
        assertThat(networkStorage.getStored()).isEqualTo(125);
    }

    @Test
    void shouldRespectAllowlistWhenInserting(
        @InjectNetworkStorageChannel final StorageChannel<String> networkStorage
    ) {
        // Arrange
        sut.setFilterMode(FilterMode.ALLOW);
        sut.setFilterTemplates(Set.of("A", "B"));

        final Storage<String> storage = new LimitedStorageImpl<>(100);
        provider.set(1, storage);
        initializeAndActivate();

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
    void shouldRespectAllowlistWithNormalizerWhenInserting(
        @InjectNetworkStorageChannel final StorageChannel<String> networkStorage
    ) {
        // Arrange
        sut.setFilterMode(FilterMode.ALLOW);
        sut.setFilterTemplates(Set.of("A"));
        sut.setNormalizer(resource -> {
            if (resource instanceof String str) {
                return str.substring(0, 1);
            }
            return resource;
        });

        final Storage<String> storage = new LimitedStorageImpl<>(100);
        provider.set(1, storage);
        initializeAndActivate();

        // Act
        final long inserted1 = networkStorage.insert("A", 1, Action.EXECUTE, EmptyActor.INSTANCE);
        final long inserted2 = networkStorage.insert("A1", 1, Action.EXECUTE, EmptyActor.INSTANCE);
        final long inserted3 = networkStorage.insert("A2", 1, Action.EXECUTE, EmptyActor.INSTANCE);
        final long inserted4 = networkStorage.insert("B", 1, Action.EXECUTE, EmptyActor.INSTANCE);
        final long inserted5 = networkStorage.insert("B1", 1, Action.EXECUTE, EmptyActor.INSTANCE);

        // Assert
        assertThat(inserted1).isEqualTo(1);
        assertThat(inserted2).isEqualTo(1);
        assertThat(inserted3).isEqualTo(1);
        assertThat(inserted4).isZero();
        assertThat(inserted5).isZero();
    }

    @Test
    void shouldRespectEmptyAllowlistWhenInserting(
        @InjectNetworkStorageChannel final StorageChannel<String> networkStorage
    ) {
        // Arrange
        sut.setFilterMode(FilterMode.ALLOW);
        sut.setFilterTemplates(Set.of());

        final Storage<String> storage = new LimitedStorageImpl<>(100);
        provider.set(1, storage);
        initializeAndActivate();

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
    void shouldRespectBlocklistWhenInserting(
        @InjectNetworkStorageChannel final StorageChannel<String> networkStorage
    ) {
        // Arrange
        sut.setFilterMode(FilterMode.BLOCK);
        sut.setFilterTemplates(Set.of("A", "B"));

        final Storage<String> storage = new LimitedStorageImpl<>(100);
        provider.set(1, storage);
        initializeAndActivate();

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
        @InjectNetworkStorageChannel final StorageChannel<String> networkStorage
    ) {
        // Arrange
        sut.setFilterMode(FilterMode.BLOCK);
        sut.setFilterTemplates(Set.of());

        final Storage<String> storage = new LimitedStorageImpl<>(100);
        provider.set(1, storage);
        initializeAndActivate();

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
    void shouldRespectAccessModeWhenInserting(
        final AccessMode accessMode,
        @InjectNetworkStorageChannel final StorageChannel<String> networkStorage
    ) {
        // Arrange
        sut.setAccessMode(accessMode);

        final Storage<String> storage = new LimitedStorageImpl<>(100);
        provider.set(1, storage);
        initializeAndActivate();

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
    void shouldRespectAccessModeWhenExtracting(
        final AccessMode accessMode,
        @InjectNetworkStorageChannel final StorageChannel<String> networkStorage
    ) {
        // Arrange
        sut.setAccessMode(accessMode);

        final Storage<String> storage = new LimitedStorageImpl<>(100);
        provider.set(1, storage);
        initializeAndActivate();

        storage.insert("A", 20, Action.EXECUTE, EmptyActor.INSTANCE);

        // Act
        final long extracted = networkStorage.extract("A", 5, Action.EXECUTE, EmptyActor.INSTANCE);

        // Assert
        switch (accessMode) {
            case INSERT_EXTRACT, EXTRACT -> assertThat(extracted).isEqualTo(5);
            case INSERT -> assertThat(extracted).isZero();
        }
    }

    @Test
    void shouldNotAllowInsertsWhenInactive(
        @InjectNetworkStorageChannel final StorageChannel<String> networkStorage
    ) {
        // Arrange
        final Storage<String> storage = new LimitedStorageImpl<>(100);
        provider.set(1, storage);
        initializeAndActivate();

        sut.setActive(false);

        // Act
        final long inserted = networkStorage.insert("A", 5, Action.EXECUTE, EmptyActor.INSTANCE);

        // Assert
        assertThat(inserted).isZero();
    }

    @Test
    void shouldNotAllowExtractsWhenInactive(
        @InjectNetworkStorageChannel final StorageChannel<String> networkStorage
    ) {
        // Arrange
        final Storage<String> storage = new LimitedStorageImpl<>(100);
        storage.insert("A", 20, Action.EXECUTE, EmptyActor.INSTANCE);
        provider.set(1, storage);
        initializeAndActivate();

        sut.setActive(false);

        // Act
        final long extracted = networkStorage.extract("A", 5, Action.EXECUTE, EmptyActor.INSTANCE);

        // Assert
        assertThat(extracted).isZero();
    }

    @Test
    void shouldHideFromNetworkWhenInactive(
        @InjectNetworkStorageChannel final StorageChannel<String> networkStorage
    ) {
        // Arrange
        final Storage<String> storage = new LimitedStorageImpl<>(100);
        storage.insert("A", 50, Action.EXECUTE, EmptyActor.INSTANCE);
        storage.insert("B", 50, Action.EXECUTE, EmptyActor.INSTANCE);
        provider.set(1, storage);
        initializeAndActivate();

        // Act
        final Collection<ResourceAmount<String>> preInactiveness = new HashSet<>(networkStorage.getAll());
        sut.setActive(false);
        final Collection<ResourceAmount<String>> postInactiveness = networkStorage.getAll();

        // Assert
        assertThat(preInactiveness).isNotEmpty();
        assertThat(postInactiveness).isEmpty();
    }

    @Test
    void shouldNoLongerShowOnNetworkWhenRemoved(
        @InjectNetworkStorageChannel final StorageChannel<String> networkStorage,
        @InjectNetwork final Network network
    ) {
        // Arrange
        final Storage<String> storage1 = new LimitedStorageImpl<>(100);
        storage1.insert("A", 50, Action.EXECUTE, EmptyActor.INSTANCE);
        provider.set(1, storage1);
        initializeAndActivate();

        // Act & assert
        final Storage<String> storage2 = new LimitedStorageImpl<>(100);
        storage2.insert("B", 50, Action.EXECUTE, EmptyActor.INSTANCE);
        provider.set(2, storage2);
        sut.onStorageChanged(2);

        assertThat(networkStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount<>("A", 50),
            new ResourceAmount<>("B", 50)
        );

        network.removeContainer(() -> sut);
        assertThat(networkStorage.getAll()).isEmpty();

        final Storage<String> storage3 = new LimitedStorageImpl<>(100);
        storage3.insert("C", 50, Action.EXECUTE, EmptyActor.INSTANCE);
        provider.set(3, storage3);
        sut.onStorageChanged(3);

        assertThat(networkStorage.getAll()).isEmpty();
    }

    @Test
    void shouldTrackChanges(@InjectNetworkStorageChannel final StorageChannel<String> networkStorage) {
        // Arrange
        final Storage<String> storage = new TrackedStorageImpl<>(new LimitedStorageImpl<>(100), () -> 0L);
        provider.set(1, storage);
        initializeAndActivate();

        // Act
        final long inserted = networkStorage.insert("A", 10, Action.EXECUTE, FakeActor.INSTANCE);

        // Assert
        assertThat(inserted).isEqualTo(10);
        assertThat(networkStorage.findTrackedResourceByActorType("A", FakeActor.class)).isNotEmpty();
    }

    private void initializeAndActivate() {
        sut.setProvider(provider);
        sut.setActive(true);
    }
}
