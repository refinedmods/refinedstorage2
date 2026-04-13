package com.refinedmods.refinedstorage.api.network.impl.node.storage;

import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.network.Network;
import com.refinedmods.refinedstorage.api.network.impl.node.ProviderImpl;
import com.refinedmods.refinedstorage.api.network.storage.StorageNetworkComponent;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.filter.FilterMode;
import com.refinedmods.refinedstorage.api.storage.AccessMode;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.api.storage.StateTrackedStorage;
import com.refinedmods.refinedstorage.api.storage.Storage;
import com.refinedmods.refinedstorage.api.storage.StorageImpl;
import com.refinedmods.refinedstorage.api.storage.StorageState;
import com.refinedmods.refinedstorage.api.storage.limited.LimitedStorageImpl;
import com.refinedmods.refinedstorage.api.storage.tracked.TrackedStorageImpl;
import com.refinedmods.refinedstorage.network.test.AddNetworkNode;
import com.refinedmods.refinedstorage.network.test.InjectNetwork;
import com.refinedmods.refinedstorage.network.test.InjectNetworkStorageComponent;
import com.refinedmods.refinedstorage.network.test.NetworkTest;
import com.refinedmods.refinedstorage.network.test.SetupNetwork;
import com.refinedmods.refinedstorage.network.test.fixtures.ActorFixture;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import static com.refinedmods.refinedstorage.network.test.fixtures.ResourceFixtures.A;
import static com.refinedmods.refinedstorage.network.test.fixtures.ResourceFixtures.A_ALTERNATIVE;
import static com.refinedmods.refinedstorage.network.test.fixtures.ResourceFixtures.A_ALTERNATIVE2;
import static com.refinedmods.refinedstorage.network.test.fixtures.ResourceFixtures.B;
import static com.refinedmods.refinedstorage.network.test.fixtures.ResourceFixtures.B_ALTERNATIVE;
import static com.refinedmods.refinedstorage.network.test.fixtures.ResourceFixtures.C;
import static com.refinedmods.refinedstorage.network.test.nodefactory.AbstractNetworkNodeFactory.PROPERTY_ENERGY_USAGE;
import static com.refinedmods.refinedstorage.network.test.nodefactory.StorageNetworkNodeFactory.PROPERTY_ENERGY_USAGE_PER_STORAGE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@NetworkTest
@SetupNetwork
class StorageNetworkNodeTest {
    private static final long BASE_USAGE = 10;
    private static final long USAGE_PER_STORAGE = 3;

    @AddNetworkNode(properties = {
        @AddNetworkNode.Property(key = PROPERTY_ENERGY_USAGE, longValue = BASE_USAGE),
        @AddNetworkNode.Property(key = PROPERTY_ENERGY_USAGE_PER_STORAGE, longValue = USAGE_PER_STORAGE)
    })
    StorageNetworkNode sut;

    ProviderImpl provider;

    @BeforeEach
    void setUp() {
        provider = new ProviderImpl();
    }

    @Test
    void shouldInitializeButNotLoadResourcesInStorage(
        @InjectNetworkStorageComponent final StorageNetworkComponent networkStorage
    ) {
        // Arrange
        final Storage storage = new LimitedStorageImpl(10);
        storage.insert(A, 5, Action.EXECUTE, Actor.EMPTY);
        provider.set(1, storage);

        // Act
        sut.setProvider(provider);

        // Assert
        assertThat(sut.getEnergyUsage()).isEqualTo(BASE_USAGE + USAGE_PER_STORAGE);
        assertThat(sut.getStored()).isEqualTo(5);
        assertThat(sut.getCapacity()).isEqualTo(10);
        assertThat(networkStorage.getAll()).isEmpty();
        assertThat(networkStorage.getStored()).isZero();
    }

    @Test
    void shouldInitializeAndLoadResourcesAfterEnabling(
        @InjectNetworkStorageComponent final StorageNetworkComponent networkStorage
    ) {
        // Arrange
        final Storage storage = new LimitedStorageImpl(100);
        storage.insert(A, 50, Action.EXECUTE, Actor.EMPTY);
        storage.insert(B, 50, Action.EXECUTE, Actor.EMPTY);
        provider.set(1, storage);

        // Act
        sut.setProvider(provider);
        sut.setActive(true);

        // Assert
        assertThat(sut.getStored()).isEqualTo(100);
        assertThat(sut.getCapacity()).isEqualTo(100);
        assertThat(networkStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 50),
            new ResourceAmount(B, 50)
        );
    }

    @Test
    void shouldInitializeMultipleTimes(
        @InjectNetworkStorageComponent final StorageNetworkComponent networkStorage
    ) {
        // Arrange
        final Storage storage1 = new LimitedStorageImpl(10);
        storage1.insert(A, 5, Action.EXECUTE, Actor.EMPTY);
        provider.set(8, storage1);
        sut.setProvider(provider);
        sut.setActive(true);

        final Storage storage2 = new LimitedStorageImpl(10);
        storage2.insert(B, 5, Action.EXECUTE, Actor.EMPTY);
        provider.set(8, storage2);

        // Act
        sut.setProvider(provider);

        // Assert
        assertThat(sut.getEnergyUsage()).isEqualTo(BASE_USAGE + USAGE_PER_STORAGE);
        assertThat(networkStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(B, 5)
        );
    }

    @Test
    void testInitialState(@InjectNetworkStorageComponent final StorageNetworkComponent networkStorage) {
        // Assert
        assertThat(sut.getEnergyUsage()).isEqualTo(BASE_USAGE);
        assertThat(sut.getStorageConfiguration().getFilterMode()).isEqualTo(FilterMode.BLOCK);
        assertThat(networkStorage.getAll()).isEmpty();
        assertThat(networkStorage.getStored()).isZero();
        assertThat(sut.getStored()).isZero();
        assertThat(sut.getCapacity()).isZero();
        assertThat(sut.getSize()).isEqualTo(9);
        for (int i = 0; i < 9; ++i) {
            assertThat(sut.getState(i)).isEqualTo(StorageState.NONE);
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testState(final boolean active) {
        // Arrange
        final Storage normalStorage = new LimitedStorageImpl(100);
        normalStorage.insert(A, 74, Action.EXECUTE, Actor.EMPTY);

        final Storage nearCapacityStorage = new LimitedStorageImpl(100);
        nearCapacityStorage.insert(A, 75, Action.EXECUTE, Actor.EMPTY);

        final Storage fullStorage = new LimitedStorageImpl(100);
        fullStorage.insert(A, 100, Action.EXECUTE, Actor.EMPTY);

        final Storage unlimitedStorage = new StorageImpl();

        provider.set(2, unlimitedStorage);
        provider.set(3, normalStorage);
        provider.set(5, nearCapacityStorage);
        provider.set(7, fullStorage);

        // Act
        sut.setProvider(provider);
        sut.setActive(active);

        // Assert
        assertThat(sut.getState(0)).isEqualTo(StorageState.NONE);
        assertThat(sut.getState(1)).isEqualTo(StorageState.NONE);
        assertThat(sut.getState(2)).isEqualTo(active ? StorageState.NORMAL : StorageState.INACTIVE);
        assertThat(sut.getState(3)).isEqualTo(active ? StorageState.NORMAL : StorageState.INACTIVE);
        assertThat(sut.getState(4)).isEqualTo(StorageState.NONE);
        assertThat(sut.getState(5)).isEqualTo(active ? StorageState.NEAR_CAPACITY : StorageState.INACTIVE);
        assertThat(sut.getState(6)).isEqualTo(StorageState.NONE);
        assertThat(sut.getState(7)).isEqualTo(active ? StorageState.FULL : StorageState.INACTIVE);
        assertThat(sut.getEnergyUsage()).isEqualTo(BASE_USAGE + (USAGE_PER_STORAGE * 4));
    }

    @Test
    void shouldDetectNewStorage(@InjectNetworkStorageComponent final StorageNetworkComponent networkStorage) {
        // Arrange
        final Storage remainingStorage = new LimitedStorageImpl(10);
        remainingStorage.insert(C, 7, Action.EXECUTE, Actor.EMPTY);
        provider.set(8, remainingStorage);

        initializeAndActivate();

        final Storage storage = new LimitedStorageImpl(10);
        storage.insert(A, 5, Action.EXECUTE, Actor.EMPTY);
        provider.set(0, storage);

        // Act
        final List<ResourceAmount> beforeChanging = new ArrayList<>(networkStorage.getAll());
        sut.onStorageChanged();
        final List<ResourceAmount> afterChanging = new ArrayList<>(networkStorage.getAll());

        // Assert
        assertThat(sut.getEnergyUsage()).isEqualTo(BASE_USAGE + (USAGE_PER_STORAGE * 2));
        assertThat(beforeChanging).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(C, 7)
        );
        assertThat(afterChanging).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(C, 7),
            new ResourceAmount(A, 5)
        );
    }

    @Test
    void shouldNotDetectAnythingWhenThereAreNoChanges(
        @InjectNetworkStorageComponent final StorageNetworkComponent networkStorage
    ) {
        // Arrange
        initializeAndActivate();

        final Storage storage = new LimitedStorageImpl(10);
        storage.insert(A, 5, Action.EXECUTE, Actor.EMPTY);
        provider.set(8, storage);
        sut.onStorageChanged();

        // Act
        sut.onStorageChanged();

        // Assert
        assertThat(sut.getEnergyUsage()).isEqualTo(BASE_USAGE + USAGE_PER_STORAGE);
        assertThat(networkStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(A, 5)
        );
        assertThat(networkStorage.getStored()).isEqualTo(5);
    }

    @Test
    void shouldDetectReplacedStorage(@InjectNetworkStorageComponent final StorageNetworkComponent networkStorage) {
        // Arrange
        final Storage originalStorage = new LimitedStorageImpl(10);
        originalStorage.insert(A, 5, Action.EXECUTE, Actor.EMPTY);
        provider.set(0, originalStorage);

        final Storage remainingStorage = new LimitedStorageImpl(10);
        remainingStorage.insert(C, 7, Action.EXECUTE, Actor.EMPTY);
        provider.set(8, remainingStorage);

        initializeAndActivate();

        final Storage replacedStorage = new LimitedStorageImpl(10);
        replacedStorage.insert(B, 2, Action.EXECUTE, Actor.EMPTY);
        provider.set(0, replacedStorage);

        // Act
        final Collection<ResourceAmount> beforeChanging = new ArrayList<>(networkStorage.getAll());
        sut.onStorageChanged();
        final Collection<ResourceAmount> afterChanging = networkStorage.getAll();

        // Assert
        assertThat(sut.getEnergyUsage()).isEqualTo(BASE_USAGE + (USAGE_PER_STORAGE * 2));
        assertThat(beforeChanging).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 5),
            new ResourceAmount(C, 7)
        );
        assertThat(afterChanging).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(B, 2),
            new ResourceAmount(C, 7)
        );
        assertThat(networkStorage.getStored()).isEqualTo(9);
    }

    @Test
    void shouldDetectRemovedStorage(@InjectNetworkStorageComponent final StorageNetworkComponent networkStorage) {
        // Arrange
        final Storage storage = new LimitedStorageImpl(10);
        storage.insert(A, 5, Action.EXECUTE, Actor.EMPTY);
        provider.set(7, storage);

        final Storage remainingStorage = new LimitedStorageImpl(10);
        remainingStorage.insert(B, 7, Action.EXECUTE, Actor.EMPTY);
        provider.set(8, remainingStorage);

        initializeAndActivate();

        provider.remove(7);

        // Act
        final Collection<ResourceAmount> beforeChanging = new ArrayList<>(networkStorage.getAll());
        sut.onStorageChanged();
        final Collection<ResourceAmount> afterChanging = networkStorage.getAll();

        // Assert
        assertThat(sut.getEnergyUsage()).isEqualTo(BASE_USAGE + USAGE_PER_STORAGE);
        assertThat(beforeChanging).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 5),
            new ResourceAmount(B, 7)
        );
        assertThat(afterChanging).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(B, 7)
        );
        assertThat(networkStorage.getStored()).isEqualTo(7);
    }

    @Test
    void shouldNotUpdateNetworkStorageWhenChangingStorageDuringInactiveness(
        @InjectNetworkStorageComponent final StorageNetworkComponent networkStorage
    ) {
        // Arrange
        final Storage storage = new LimitedStorageImpl(100);
        storage.insert(A, 50, Action.EXECUTE, Actor.EMPTY);
        storage.insert(B, 50, Action.EXECUTE, Actor.EMPTY);
        provider.set(1, storage);
        initializeAndActivate();

        // Act
        final Collection<ResourceAmount> beforeBecomingInactive = new ArrayList<>(networkStorage.getAll());
        sut.setActive(false);
        sut.onStorageChanged();
        final Collection<ResourceAmount> afterBecomingInactive = networkStorage.getAll();

        // Assert
        assertThat(beforeBecomingInactive).isNotEmpty();
        assertThat(afterBecomingInactive).isEmpty();
        assertThat(networkStorage.getStored()).isZero();
    }

    @Test
    void shouldHaveResourcesFromStoragePresentInNetwork(
        @InjectNetworkStorageComponent final StorageNetworkComponent networkStorage
    ) {
        // Arrange
        final Storage storage = new LimitedStorageImpl(100);
        storage.insert(A, 50, Action.EXECUTE, Actor.EMPTY);
        storage.insert(B, 50, Action.EXECUTE, Actor.EMPTY);
        provider.set(1, storage);

        initializeAndActivate();

        // Act
        final Collection<ResourceAmount> resources = networkStorage.getAll();
        final long stored = networkStorage.getStored();

        // Assert
        assertThat(resources).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 50),
            new ResourceAmount(B, 50)
        );
        assertThat(stored).isEqualTo(100);
    }

    @Test
    void shouldInsert(@InjectNetworkStorageComponent final StorageNetworkComponent networkStorage) {
        // Arrange
        final Storage storage1 = new LimitedStorageImpl(100);
        provider.set(1, storage1);

        final Storage storage2 = new LimitedStorageImpl(100);
        provider.set(2, storage2);

        final Storage storage3 = new LimitedStorageImpl(100);
        provider.set(3, storage3);

        initializeAndActivate();

        // Act
        final long inserted1 = networkStorage.insert(A, 150, Action.EXECUTE, Actor.EMPTY);
        final long inserted2 = networkStorage.insert(A, 10, Action.EXECUTE, Actor.EMPTY);
        final long inserted3 = networkStorage.insert(B, 300, Action.EXECUTE, Actor.EMPTY);

        // Assert
        assertThat(inserted1).isEqualTo(150);
        assertThat(inserted2).isEqualTo(10);
        assertThat(inserted3).isEqualTo(140);

        assertThat(storage1.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(A, 100)
        );
        assertThat(storage2.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 60),
            new ResourceAmount(B, 40)
        );
        assertThat(storage3.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(B, 100)
        );

        assertThat(networkStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(B, 140),
            new ResourceAmount(A, 160)
        );
        assertThat(networkStorage.getStored()).isEqualTo(inserted1 + inserted2 + inserted3);
    }

    @Test
    void shouldExtract(@InjectNetworkStorageComponent final StorageNetworkComponent networkStorage) {
        // Arrange
        final Storage storage1 = new LimitedStorageImpl(100);
        storage1.insert(A, 50, Action.EXECUTE, Actor.EMPTY);
        storage1.insert(B, 50, Action.EXECUTE, Actor.EMPTY);
        provider.set(1, storage1);

        final Storage storage2 = new LimitedStorageImpl(100);
        storage2.insert(A, 50, Action.EXECUTE, Actor.EMPTY);
        storage2.insert(B, 50, Action.EXECUTE, Actor.EMPTY);
        provider.set(2, storage2);

        final Storage storage3 = new LimitedStorageImpl(100);
        storage3.insert(C, 10, Action.EXECUTE, Actor.EMPTY);
        provider.set(3, storage3);

        initializeAndActivate();

        // Act
        final long extracted = networkStorage.extract(A, 85, Action.EXECUTE, Actor.EMPTY);

        // Assert
        assertThat(extracted).isEqualTo(85);

        assertThat(storage1.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(B, 50)
        );
        assertThat(storage2.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(B, 50),
            new ResourceAmount(A, 15)
        );
        assertThat(storage3.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(C, 10)
        );

        assertThat(networkStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(B, 100),
            new ResourceAmount(A, 15),
            new ResourceAmount(C, 10)
        );
        assertThat(networkStorage.getStored()).isEqualTo(125);
    }

    @Test
    void shouldRespectAllowlistWhenInserting(
        @InjectNetworkStorageComponent final StorageNetworkComponent networkStorage
    ) {
        // Arrange
        sut.getStorageConfiguration().setFilterMode(FilterMode.ALLOW);
        sut.getStorageConfiguration().setFilters(Set.of(A, B));

        final Storage storage = new LimitedStorageImpl(100);
        provider.set(1, storage);
        initializeAndActivate();

        // Act
        final long inserted1 = networkStorage.insert(A, 12, Action.EXECUTE, Actor.EMPTY);
        final long inserted2 = networkStorage.insert(B, 12, Action.EXECUTE, Actor.EMPTY);
        final long inserted3 = networkStorage.insert(C, 10, Action.EXECUTE, Actor.EMPTY);

        // Assert
        assertThat(inserted1).isEqualTo(12);
        assertThat(inserted2).isEqualTo(12);
        assertThat(inserted3).isZero();
    }

    @Test
    void shouldRespectAllowlistWithNormalizerWhenInserting(
        @InjectNetworkStorageComponent final StorageNetworkComponent networkStorage
    ) {
        // Arrange
        sut.getStorageConfiguration().setFilterMode(FilterMode.ALLOW);
        sut.getStorageConfiguration().setFilters(Set.of(A));
        sut.getStorageConfiguration().setNormalizer(resource -> {
            if (resource == A_ALTERNATIVE || resource == A_ALTERNATIVE2) {
                return A;
            }
            if (resource == B_ALTERNATIVE) {
                return B;
            }
            return resource;
        });

        final Storage storage = new LimitedStorageImpl(100);
        provider.set(1, storage);
        initializeAndActivate();

        // Act
        final long inserted1 = networkStorage.insert(A, 1, Action.EXECUTE, Actor.EMPTY);
        final long inserted2 = networkStorage.insert(A_ALTERNATIVE, 1, Action.EXECUTE, Actor.EMPTY);
        final long inserted3 = networkStorage.insert(A_ALTERNATIVE2, 1, Action.EXECUTE, Actor.EMPTY);
        final long inserted4 = networkStorage.insert(B, 1, Action.EXECUTE, Actor.EMPTY);
        final long inserted5 = networkStorage.insert(B_ALTERNATIVE, 1, Action.EXECUTE, Actor.EMPTY);

        // Assert
        assertThat(inserted1).isEqualTo(1);
        assertThat(inserted2).isEqualTo(1);
        assertThat(inserted3).isEqualTo(1);
        assertThat(inserted4).isZero();
        assertThat(inserted5).isZero();
    }

    @Test
    void shouldRespectEmptyAllowlistWhenInserting(
        @InjectNetworkStorageComponent final StorageNetworkComponent networkStorage
    ) {
        // Arrange
        sut.getStorageConfiguration().setFilterMode(FilterMode.ALLOW);
        sut.getStorageConfiguration().setFilters(Set.of());

        final Storage storage = new LimitedStorageImpl(100);
        provider.set(1, storage);
        initializeAndActivate();

        // Act
        final long inserted1 = networkStorage.insert(A, 12, Action.EXECUTE, Actor.EMPTY);
        final long inserted2 = networkStorage.insert(B, 12, Action.EXECUTE, Actor.EMPTY);
        final long inserted3 = networkStorage.insert(C, 10, Action.EXECUTE, Actor.EMPTY);

        // Assert
        assertThat(inserted1).isZero();
        assertThat(inserted2).isZero();
        assertThat(inserted3).isZero();
    }

    @Test
    void shouldRespectBlocklistWhenInserting(
        @InjectNetworkStorageComponent final StorageNetworkComponent networkStorage
    ) {
        // Arrange
        sut.getStorageConfiguration().setFilterMode(FilterMode.BLOCK);
        sut.getStorageConfiguration().setFilters(Set.of(A, B));

        final Storage storage = new LimitedStorageImpl(100);
        provider.set(1, storage);
        initializeAndActivate();

        // Act
        final long inserted1 = networkStorage.insert(A, 12, Action.EXECUTE, Actor.EMPTY);
        final long inserted2 = networkStorage.insert(B, 12, Action.EXECUTE, Actor.EMPTY);
        final long inserted3 = networkStorage.insert(C, 10, Action.EXECUTE, Actor.EMPTY);

        // Assert
        assertThat(inserted1).isZero();
        assertThat(inserted2).isZero();
        assertThat(inserted3).isEqualTo(10);
    }

    @Test
    void shouldRespectEmptyBlocklistWhenInserting(
        @InjectNetworkStorageComponent final StorageNetworkComponent networkStorage
    ) {
        // Arrange
        sut.getStorageConfiguration().setFilterMode(FilterMode.BLOCK);
        sut.getStorageConfiguration().setFilters(Set.of());

        final Storage storage = new LimitedStorageImpl(100);
        provider.set(1, storage);
        initializeAndActivate();

        // Act
        final long inserted1 = networkStorage.insert(A, 12, Action.EXECUTE, Actor.EMPTY);
        final long inserted2 = networkStorage.insert(B, 12, Action.EXECUTE, Actor.EMPTY);
        final long inserted3 = networkStorage.insert(C, 10, Action.EXECUTE, Actor.EMPTY);

        // Assert
        assertThat(inserted1).isEqualTo(12);
        assertThat(inserted2).isEqualTo(12);
        assertThat(inserted3).isEqualTo(10);
    }

    @ParameterizedTest
    @EnumSource(AccessMode.class)
    void shouldRespectAccessModeWhenInserting(
        final AccessMode accessMode,
        @InjectNetworkStorageComponent final StorageNetworkComponent networkStorage
    ) {
        // Arrange
        sut.getStorageConfiguration().setAccessMode(accessMode);

        final Storage storage = new LimitedStorageImpl(100);
        provider.set(1, storage);
        initializeAndActivate();

        // Act
        final long inserted = networkStorage.insert(A, 5, Action.EXECUTE, Actor.EMPTY);

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
        @InjectNetworkStorageComponent final StorageNetworkComponent networkStorage
    ) {
        // Arrange
        sut.getStorageConfiguration().setAccessMode(accessMode);

        final Storage storage = new LimitedStorageImpl(100);
        provider.set(1, storage);
        initializeAndActivate();

        storage.insert(A, 20, Action.EXECUTE, Actor.EMPTY);

        // Act
        final long extracted = networkStorage.extract(A, 5, Action.EXECUTE, Actor.EMPTY);

        // Assert
        switch (accessMode) {
            case INSERT_EXTRACT, EXTRACT -> assertThat(extracted).isEqualTo(5);
            case INSERT -> assertThat(extracted).isZero();
        }
    }

    @Test
    void shouldNotAllowInsertsWhenInactive(
        @InjectNetworkStorageComponent final StorageNetworkComponent networkStorage
    ) {
        // Arrange
        final Storage storage = new LimitedStorageImpl(100);
        provider.set(1, storage);
        initializeAndActivate();

        sut.setActive(false);

        // Act
        final long inserted = networkStorage.insert(A, 5, Action.EXECUTE, Actor.EMPTY);

        // Assert
        assertThat(inserted).isZero();
    }

    @Test
    void shouldNotAllowExtractsWhenInactive(
        @InjectNetworkStorageComponent final StorageNetworkComponent networkStorage
    ) {
        // Arrange
        final Storage storage = new LimitedStorageImpl(100);
        storage.insert(A, 20, Action.EXECUTE, Actor.EMPTY);
        provider.set(1, storage);
        initializeAndActivate();

        sut.setActive(false);

        // Act
        final long extracted = networkStorage.extract(A, 5, Action.EXECUTE, Actor.EMPTY);

        // Assert
        assertThat(extracted).isZero();
    }

    @Test
    void shouldHideFromNetworkWhenInactive(
        @InjectNetworkStorageComponent final StorageNetworkComponent networkStorage
    ) {
        // Arrange
        final Storage storage = new LimitedStorageImpl(100);
        storage.insert(A, 50, Action.EXECUTE, Actor.EMPTY);
        storage.insert(B, 50, Action.EXECUTE, Actor.EMPTY);
        provider.set(1, storage);
        initializeAndActivate();

        // Act
        final Collection<ResourceAmount> beforeBecomingInactive = new ArrayList<>(networkStorage.getAll());
        sut.setActive(false);
        final Collection<ResourceAmount> afterBecomingInactive = networkStorage.getAll();

        // Assert
        assertThat(beforeBecomingInactive).isNotEmpty();
        assertThat(afterBecomingInactive).isEmpty();
        assertThat(sut.getStored()).isEqualTo(100);
        assertThat(sut.getCapacity()).isEqualTo(100);
    }

    @Test
    void shouldNoLongerShowOnNetworkWhenRemoved(
        @InjectNetworkStorageComponent final StorageNetworkComponent networkStorage,
        @InjectNetwork final Network network
    ) {
        // Arrange
        final Storage storage1 = new LimitedStorageImpl(100);
        storage1.insert(A, 50, Action.EXECUTE, Actor.EMPTY);
        provider.set(1, storage1);
        initializeAndActivate();

        // Act & assert
        final Storage storage2 = new LimitedStorageImpl(100);
        storage2.insert(B, 50, Action.EXECUTE, Actor.EMPTY);
        provider.set(2, storage2);
        sut.onStorageChanged();

        assertThat(networkStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 50),
            new ResourceAmount(B, 50)
        );

        network.removeContainer(() -> sut);
        assertThat(networkStorage.getAll()).isEmpty();

        final Storage storage3 = new LimitedStorageImpl(100);
        storage3.insert(C, 50, Action.EXECUTE, Actor.EMPTY);
        provider.set(3, storage3);
        sut.onStorageChanged();

        assertThat(networkStorage.getAll()).isEmpty();
    }

    @Test
    void shouldTrackChanges(@InjectNetworkStorageComponent final StorageNetworkComponent networkStorage) {
        // Arrange
        final Storage storage = new TrackedStorageImpl(new LimitedStorageImpl(100), () -> 0L);
        provider.set(1, storage);
        initializeAndActivate();

        // Act
        final long inserted = networkStorage.insert(A, 10, Action.EXECUTE, ActorFixture.INSTANCE);

        // Assert
        assertThat(inserted).isEqualTo(10);
        assertThat(networkStorage.findTrackedResourceByActorType(A, ActorFixture.class)).isNotEmpty();
    }

    @Test
    void shouldNotifyListenerWhenStateChanges(
        @InjectNetworkStorageComponent final StorageNetworkComponent networkStorage
    ) {
        // Arrange
        final StateTrackedStorage.Listener listener = mock(StateTrackedStorage.Listener.class);
        sut.setListener(listener);

        final Storage storage = new LimitedStorageImpl(100);
        provider.set(1, storage);
        initializeAndActivate();

        // Act
        networkStorage.insert(A, 75, Action.EXECUTE, ActorFixture.INSTANCE);

        // Assert
        verify(listener, times(1)).onStorageStateChanged();
    }

    private void initializeAndActivate() {
        sut.setProvider(provider);
        sut.setActive(true);
    }
}
