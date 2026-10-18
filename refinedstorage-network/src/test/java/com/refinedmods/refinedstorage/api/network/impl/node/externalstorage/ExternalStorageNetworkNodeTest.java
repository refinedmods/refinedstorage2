package com.refinedmods.refinedstorage.api.network.impl.node.externalstorage;

import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.network.Network;
import com.refinedmods.refinedstorage.api.network.storage.StorageNetworkComponent;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.filter.FilterMode;
import com.refinedmods.refinedstorage.api.storage.AccessMode;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.api.storage.Storage;
import com.refinedmods.refinedstorage.api.storage.StorageImpl;
import com.refinedmods.refinedstorage.api.storage.external.ExternalStorageProvider;
import com.refinedmods.refinedstorage.api.storage.limited.LimitedStorageImpl;
import com.refinedmods.refinedstorage.api.storage.tracked.TrackedResource;
import com.refinedmods.refinedstorage.api.storage.tracked.TrackedStorageImpl;
import com.refinedmods.refinedstorage.network.test.AddNetworkNode;
import com.refinedmods.refinedstorage.network.test.InjectNetwork;
import com.refinedmods.refinedstorage.network.test.InjectNetworkStorageComponent;
import com.refinedmods.refinedstorage.network.test.NetworkTest;
import com.refinedmods.refinedstorage.network.test.SetupNetwork;
import com.refinedmods.refinedstorage.network.test.fixtures.ActorFixture;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import static com.refinedmods.refinedstorage.network.test.fixtures.ResourceFixtures.A;
import static com.refinedmods.refinedstorage.network.test.fixtures.ResourceFixtures.B;
import static com.refinedmods.refinedstorage.network.test.fixtures.ResourceFixtures.C;
import static com.refinedmods.refinedstorage.network.test.nodefactory.AbstractNetworkNodeFactory.PROPERTY_ENERGY_USAGE;
import static org.assertj.core.api.Assertions.assertThat;

@NetworkTest
@SetupNetwork
@SetupNetwork(id = "other")
class ExternalStorageNetworkNodeTest {
    private static final long ENERGY_USAGE = 5;

    @AddNetworkNode(properties = {
        @AddNetworkNode.Property(key = PROPERTY_ENERGY_USAGE, longValue = ENERGY_USAGE)
    })
    ExternalStorageNetworkNode sut;

    @Test
    void testInitialState(@InjectNetworkStorageComponent final StorageNetworkComponent networkStorage) {
        // Act
        final long inserted = networkStorage.insert(A, 10, Action.EXECUTE, ActorFixture.INSTANCE);
        final long extracted = networkStorage.extract(A, 10, Action.EXECUTE, ActorFixture.INSTANCE);

        // Assert
        assertThat(inserted).isZero();
        assertThat(extracted).isZero();
        assertThat(sut.getStorageConfiguration().getAccessMode()).isEqualTo(AccessMode.INSERT_EXTRACT);
        assertThat(sut.getEnergyUsage()).isEqualTo(ENERGY_USAGE);
        assertThat(sut.getStorageConfiguration().getInsertPriority()).isZero();
        assertThat(sut.getStorageConfiguration().getFilterMode()).isEqualTo(FilterMode.BLOCK);
        assertThat(networkStorage.getAll()).isEmpty();
        assertThat(networkStorage.getStored()).isZero();
        assertThat(networkStorage.findTrackedResourceByActorType(A, ActorFixture.class)).isEmpty();
    }

    @Test
    void shouldInitialize(@InjectNetworkStorageComponent final StorageNetworkComponent networkStorage) {
        // Arrange
        final Storage storage = new StorageImpl();
        final ExternalStorageProvider provider = new StorageExternalStorageProvider(storage);

        // Act
        sut.initialize(provider);

        // Assert
        assertThat(networkStorage.getAll()).isEmpty();
        assertThat(networkStorage.getStored()).isZero();
    }

    @Test
    void shouldBeAbleToInitializeMultipleTimes(
        @InjectNetworkStorageComponent final StorageNetworkComponent networkStorage
    ) {
        // Arrange
        final Storage storage1 = new StorageImpl();
        final ExternalStorageProvider provider1 = new StorageExternalStorageProvider(storage1);

        final Storage storage2 = new StorageImpl();
        final ExternalStorageProvider provider2 = new StorageExternalStorageProvider(storage2);

        // Act
        sut.initialize(provider1);
        networkStorage.insert(A, 10, Action.EXECUTE, Actor.EMPTY);

        sut.initialize(provider2);
        networkStorage.insert(B, 1, Action.EXECUTE, Actor.EMPTY);

        // Assert
        assertThat(networkStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(B, 1)
        );
        assertThat(networkStorage.getStored()).isEqualTo(1);
        assertThat(storage1.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(A, 10)
        );
        assertThat(storage2.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(B, 1)
        );
    }

    @Test
    void shouldInsert(@InjectNetworkStorageComponent final StorageNetworkComponent networkStorage) {
        // Arrange
        final Storage storage = new StorageImpl();
        final ExternalStorageProvider provider = new StorageExternalStorageProvider(storage);
        sut.initialize(provider);

        // Act
        final long inserted = networkStorage.insert(A, 10, Action.EXECUTE, Actor.EMPTY);

        // Assert
        assertThat(inserted).isEqualTo(10);
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(A, 10)
        );
        assertThat(networkStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(A, 10)
        );
        assertThat(networkStorage.getStored()).isEqualTo(10);
    }

    @Test
    void shouldExtract(@InjectNetworkStorageComponent final StorageNetworkComponent networkStorage) {
        // Arrange
        final Storage storage = new StorageImpl();
        storage.insert(A, 10, Action.EXECUTE, Actor.EMPTY);
        final ExternalStorageProvider provider = new StorageExternalStorageProvider(storage);
        sut.initialize(provider);

        // Act
        final long extracted = networkStorage.extract(A, 7, Action.EXECUTE, Actor.EMPTY);

        // Assert
        assertThat(extracted).isEqualTo(7);
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(A, 3)
        );
        assertThat(networkStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(A, 3)
        );
        assertThat(networkStorage.getStored()).isEqualTo(3);
    }

    @Test
    void shouldRespectAllowlistWhenInserting(
        @InjectNetworkStorageComponent final StorageNetworkComponent networkStorage) {
        // Arrange
        sut.getStorageConfiguration().setFilterMode(FilterMode.ALLOW);
        sut.getStorageConfiguration().setFilters(Set.of(A, B));

        final Storage storage = new StorageImpl();
        final ExternalStorageProvider provider = new StorageExternalStorageProvider(storage);
        sut.initialize(provider);

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
    void shouldRespectEmptyAllowlistWhenInserting(
        @InjectNetworkStorageComponent final StorageNetworkComponent networkStorage
    ) {
        // Arrange
        sut.getStorageConfiguration().setFilterMode(FilterMode.ALLOW);
        sut.getStorageConfiguration().setFilters(Set.of());

        final Storage storage = new StorageImpl();
        final ExternalStorageProvider provider = new StorageExternalStorageProvider(storage);
        sut.initialize(provider);

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
        @InjectNetworkStorageComponent final StorageNetworkComponent networkStorage) {
        // Arrange
        sut.getStorageConfiguration().setFilterMode(FilterMode.BLOCK);
        sut.getStorageConfiguration().setFilters(Set.of(A, B));

        final Storage storage = new StorageImpl();
        final ExternalStorageProvider provider = new StorageExternalStorageProvider(storage);
        sut.initialize(provider);

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
        @InjectNetworkStorageComponent final StorageNetworkComponent networkStorage) {
        // Arrange
        sut.getStorageConfiguration().setFilterMode(FilterMode.BLOCK);
        sut.getStorageConfiguration().setFilters(Set.of());

        final Storage storage = new StorageImpl();
        final ExternalStorageProvider provider = new StorageExternalStorageProvider(storage);
        sut.initialize(provider);

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
    void shouldRespectAccessModeWhenInserting(final AccessMode accessMode,
                                              @InjectNetworkStorageComponent
                                              final StorageNetworkComponent networkStorage
    ) {
        // Arrange
        sut.getStorageConfiguration().setAccessMode(accessMode);

        final Storage storage = new StorageImpl();
        final ExternalStorageProvider provider = new StorageExternalStorageProvider(storage);
        sut.initialize(provider);

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
    void shouldRespectAccessModeWhenExtracting(final AccessMode accessMode,
                                               @InjectNetworkStorageComponent
                                               final StorageNetworkComponent networkStorage
    ) {
        // Arrange
        sut.getStorageConfiguration().setAccessMode(accessMode);

        final Storage storage = new StorageImpl();
        storage.insert(A, 20, Action.EXECUTE, Actor.EMPTY);
        final ExternalStorageProvider provider = new StorageExternalStorageProvider(storage);

        sut.initialize(provider);

        // Act
        final long extracted = networkStorage.extract(A, 5, Action.EXECUTE, Actor.EMPTY);

        // Assert
        switch (accessMode) {
            case INSERT_EXTRACT, EXTRACT -> assertThat(extracted).isEqualTo(5);
            case INSERT -> assertThat(extracted).isZero();
        }
    }

    @Test
    void shouldNotInsertWhenInactive(@InjectNetworkStorageComponent final StorageNetworkComponent networkStorage) {
        // Arrange
        final Storage storage = new StorageImpl();
        final ExternalStorageProvider provider = new StorageExternalStorageProvider(storage);

        sut.initialize(provider);
        sut.setActive(false);

        // Act
        final long inserted = networkStorage.insert(A, 5, Action.EXECUTE, Actor.EMPTY);

        // Assert
        assertThat(inserted).isZero();
    }

    @Test
    void shouldNotExtractWhenInactive(@InjectNetworkStorageComponent final StorageNetworkComponent networkStorage) {
        // Arrange
        final Storage storage = new StorageImpl();
        storage.insert(A, 5, Action.EXECUTE, Actor.EMPTY);
        final ExternalStorageProvider provider = new StorageExternalStorageProvider(storage);

        sut.initialize(provider);
        sut.setActive(false);

        // Act
        final long extracted = networkStorage.extract(A, 5, Action.EXECUTE, Actor.EMPTY);

        // Assert
        assertThat(extracted).isZero();
    }

    @Test
    void shouldHideStorageContentsWhenInactive(
        @InjectNetworkStorageComponent final StorageNetworkComponent networkStorage
    ) {
        // Arrange
        final Storage storage = new LimitedStorageImpl(100);
        final ExternalStorageProvider provider = new StorageExternalStorageProvider(storage);
        sut.initialize(provider);

        networkStorage.insert(A, 50, Action.EXECUTE, Actor.EMPTY);
        networkStorage.insert(B, 50, Action.EXECUTE, Actor.EMPTY);

        // Act
        sut.setActive(false);

        // Assert
        assertThat(networkStorage.getAll()).isEmpty();
        assertThat(networkStorage.getStored()).isZero();
    }

    @Test
    void shouldShowStorageContentsWhenActive(
        @InjectNetworkStorageComponent final StorageNetworkComponent networkStorage) {
        // Arrange
        final Storage storage = new LimitedStorageImpl(100);
        final ExternalStorageProvider provider = new StorageExternalStorageProvider(storage);
        sut.initialize(provider);

        networkStorage.insert(A, 50, Action.EXECUTE, Actor.EMPTY);
        networkStorage.insert(B, 50, Action.EXECUTE, Actor.EMPTY);

        sut.setActive(false);

        // Arrange
        sut.setActive(true);

        // Assert
        assertThat(networkStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 50),
            new ResourceAmount(B, 50)
        );
    }

    @Test
    void shouldNoLongerShowOnNetworkWhenRemoved(
        @InjectNetworkStorageComponent final StorageNetworkComponent networkStorage,
        @InjectNetwork final Network network
    ) {
        // Arrange
        final Storage storage = new LimitedStorageImpl(100);
        storage.insert(A, 100, Action.EXECUTE, Actor.EMPTY);
        final ExternalStorageProvider provider = new StorageExternalStorageProvider(storage);
        sut.initialize(provider);

        // Act & assert
        network.removeContainer(() -> sut);
        assertThat(networkStorage.getAll()).isEmpty();

        storage.insert(B, 100, Action.EXECUTE, Actor.EMPTY);
        sut.detectChanges();

        assertThat(networkStorage.getAll()).isEmpty();
    }

    @Test
    void shouldNotifyNewNetworkAboutChangesWhenChangingNetworks(
        @InjectNetworkStorageComponent final StorageNetworkComponent networkStorage,
        @InjectNetworkStorageComponent(networkId = "other") final StorageNetworkComponent otherNetworkStorage,
        @InjectNetwork final Network network,
        @InjectNetwork("other") final Network otherNetwork
    ) {
        // Arrange
        final Storage storage = new LimitedStorageImpl(100);
        storage.insert(A, 100, Action.EXECUTE, Actor.EMPTY);
        final ExternalStorageProvider provider = new StorageExternalStorageProvider(storage);
        sut.initialize(provider);

        // Act & assert
        // Remove the external storage from the existing network, and add it to the other network.
        network.removeContainer(() -> sut);
        sut.setNetwork(otherNetwork);
        otherNetwork.addContainer(() -> sut);

        // The network storage should now be empty.
        assertThat(networkStorage.getAll()).isEmpty();

        // Now reinsert.
        storage.insert(B, 100, Action.EXECUTE, Actor.EMPTY);
        sut.detectChanges();

        // This is the desired state, the old parent should be cleaned up and not used.
        assertThat(networkStorage.getAll()).isEmpty();
        assertThat(otherNetworkStorage.getAll()).isNotEmpty();
    }

    @Test
    void shouldNoLongerNotifyOldNetworkAboutChangesWhenChangingNetworks(
        @InjectNetworkStorageComponent final StorageNetworkComponent networkStorage,
        @InjectNetworkStorageComponent(networkId = "other") final StorageNetworkComponent otherNetworkStorage,
        @InjectNetwork final Network network,
        @InjectNetwork("other") final Network otherNetwork
    ) {
        // Arrange
        final Storage storage = new LimitedStorageImpl(100);
        storage.insert(A, 100, Action.EXECUTE, Actor.EMPTY);
        final ExternalStorageProvider provider = new StorageExternalStorageProvider(storage);
        sut.initialize(provider);

        // Act & assert
        // Remove the external storage from the existing network, and add it to the other network.
        network.removeContainer(() -> sut);
        sut.setNetwork(otherNetwork);
        otherNetwork.addContainer(() -> sut);

        // The network storage should now be empty.
        assertThat(networkStorage.getAll()).isEmpty();

        // Reset the external storage, so the parents in the NetworkNodeStorage are reused.
        sut.setActive(false);
        sut.setActive(true);

        // Now reinsert.
        storage.insert(B, 100, Action.EXECUTE, Actor.EMPTY);
        sut.detectChanges();

        // This is the desired state, the old parent should be cleaned up and not used.
        assertThat(networkStorage.getAll()).isEmpty();
        assertThat(otherNetworkStorage.getAll()).isNotEmpty();
    }

    @Test
    void shouldNoLongerShowOnNetworkWhenRemovedWithoutInitializedStorage(
        @InjectNetworkStorageComponent final StorageNetworkComponent networkStorage,
        @InjectNetwork final Network network
    ) {
        // Act
        network.removeContainer(() -> sut);
        sut.detectChanges();

        // Assert
        assertThat(networkStorage.getAll()).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void shouldTrackChangesWhenExtracting(
        final Action action,
        @InjectNetworkStorageComponent final StorageNetworkComponent networkStorage
    ) {
        // Arrange
        final Storage storage = new LimitedStorageImpl(100);
        storage.insert(A, 10, Action.EXECUTE, Actor.EMPTY);
        final ExternalStorageProvider provider = new StorageExternalStorageProvider(storage);
        sut.initialize(provider);

        final AtomicBoolean trackedResourceWasPresent = trackWhetherResourceHasChangedAndTrackedResourceIsAvailable(
            networkStorage
        );

        // Act
        final long extracted = networkStorage.extract(A, 7, action, ActorFixture.INSTANCE);

        // Assert
        assertThat(extracted).isEqualTo(7);
        final Optional<TrackedResource> trackedResource = networkStorage.findTrackedResourceByActorType(
            A,
            ActorFixture.class
        );
        if (action == Action.EXECUTE) {
            assertThat(trackedResource).get().usingRecursiveComparison().isEqualTo(new TrackedResource(
                ActorFixture.INSTANCE.getName(),
                0
            ));
            assertThat(networkStorage.getTrackedResourcesByActorType(ActorFixture.class, Set.of(A)))
                .containsOnlyKeys(A);
            assertThat(trackedResourceWasPresent).describedAs("tracked resource was present").isTrue();
        } else {
            assertThat(trackedResource).isEmpty();
            assertThat(networkStorage.getTrackedResourcesByActorType(ActorFixture.class, Set.of(A))).isEmpty();
            assertThat(trackedResourceWasPresent).describedAs("tracked resource was present").isFalse();
        }
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void shouldNotTrackChangesWhenExtractionFailed(
        final Action action,
        @InjectNetworkStorageComponent final StorageNetworkComponent networkStorage
    ) {
        // Arrange
        final Storage storage = new LimitedStorageImpl(100);
        final ExternalStorageProvider provider = new StorageExternalStorageProvider(storage);
        sut.initialize(provider);

        // Act
        final long extracted = networkStorage.extract(A, 7, action, ActorFixture.INSTANCE);

        // Assert
        assertThat(extracted).isZero();
        final Optional<TrackedResource> trackedResource = networkStorage.findTrackedResourceByActorType(
            A,
            ActorFixture.class
        );
        assertThat(trackedResource).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void shouldTrackChangesWhenInserting(
        final Action action,
        @InjectNetworkStorageComponent final StorageNetworkComponent networkStorage
    ) {
        // Arrange
        final Storage storage = new LimitedStorageImpl(100);
        final ExternalStorageProvider provider = new StorageExternalStorageProvider(storage);
        sut.initialize(provider);

        final AtomicBoolean trackedResourceWasPresent = trackWhetherResourceHasChangedAndTrackedResourceIsAvailable(
            networkStorage
        );

        // Act
        final long inserted = networkStorage.insert(A, 10, action, ActorFixture.INSTANCE);

        // Assert
        assertThat(inserted).isEqualTo(10);
        final Optional<TrackedResource> trackedResource = networkStorage.findTrackedResourceByActorType(
            A,
            ActorFixture.class
        );
        if (action == Action.EXECUTE) {
            assertThat(trackedResource).get().usingRecursiveComparison().isEqualTo(new TrackedResource(
                ActorFixture.INSTANCE.getName(),
                0
            ));
            assertThat(trackedResourceWasPresent).describedAs("tracked resource was present").isTrue();
        } else {
            assertThat(trackedResource).isEmpty();
            assertThat(trackedResourceWasPresent).describedAs("tracked resource was present").isFalse();
        }
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void shouldNotTrackChangesWhenInsertionFailed(
        final Action action,
        @InjectNetworkStorageComponent final StorageNetworkComponent networkStorage
    ) {
        // Arrange
        final Storage storage = new LimitedStorageImpl(0);
        final ExternalStorageProvider provider = new StorageExternalStorageProvider(storage);
        sut.initialize(provider);

        // Act
        final long inserted = networkStorage.insert(A, 10, action, ActorFixture.INSTANCE);

        // Assert
        assertThat(inserted).isZero();
        final Optional<TrackedResource> trackedResource = networkStorage.findTrackedResourceByActorType(
            A,
            ActorFixture.class
        );
        assertThat(trackedResource).isEmpty();
    }

    private AtomicBoolean trackWhetherResourceHasChangedAndTrackedResourceIsAvailable(
        final StorageNetworkComponent networkStorage
    ) {
        final AtomicBoolean found = new AtomicBoolean();
        networkStorage.addListener(change -> {
            if (change.resource().equals(A)) {
                found.set(networkStorage.findTrackedResourceByActorType(A, ActorFixture.class).isPresent());
            }
        });
        return found;
    }

    @Test
    void shouldNotDetectChangesWithoutConnectedStorage() {
        // Act
        final boolean hasChanges = sut.detectChanges();

        // Assert
        assertThat(hasChanges).isFalse();
    }

    @Test
    void shouldDetectChanges(@InjectNetworkStorageComponent final StorageNetworkComponent networkStorage) {
        // Arrange
        final Storage storage = new LimitedStorageImpl(100);
        final ExternalStorageProvider provider = new StorageExternalStorageProvider(storage);
        sut.initialize(provider);

        // Act
        storage.insert(A, 10, Action.EXECUTE, Actor.EMPTY);
        final boolean hasChanges1 = sut.detectChanges();
        networkStorage.insert(B, 1, Action.EXECUTE, Actor.EMPTY);
        final boolean hasChanges2 = sut.detectChanges();

        // Assert
        assertThat(hasChanges1).isTrue();
        assertThat(hasChanges2).isFalse();
        assertThat(networkStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 10),
            new ResourceAmount(B, 1)
        );
    }

    @Nested
    class PriorityTest {
        @AddNetworkNode
        ExternalStorageNetworkNode otherStorage;

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        void shouldRespectPriority(final boolean oneHasPriority,
                                   @InjectNetworkStorageComponent final StorageNetworkComponent networkStorage) {
            // Arrange
            final Storage storage1 = new TrackedStorageImpl(new LimitedStorageImpl(100), () -> 0L);
            final ExternalStorageProvider provider1 = new StorageExternalStorageProvider(storage1);
            sut.initialize(provider1);

            final Storage storage2 = new TrackedStorageImpl(new LimitedStorageImpl(100), () -> 0L);
            final ExternalStorageProvider provider2 = new StorageExternalStorageProvider(storage2);
            otherStorage.initialize(provider2);

            if (oneHasPriority) {
                sut.getStorageConfiguration().setInsertPriority(5);
                otherStorage.getStorageConfiguration().setInsertPriority(2);
            } else {
                sut.getStorageConfiguration().setInsertPriority(2);
                otherStorage.getStorageConfiguration().setInsertPriority(5);
            }

            // Act
            networkStorage.insert(A, 1, Action.EXECUTE, Actor.EMPTY);

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
