package com.refinedmods.refinedstorage2.api.network.node.externalstorage;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.core.filter.FilterMode;
import com.refinedmods.refinedstorage2.api.network.Network;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.storage.AccessMode;
import com.refinedmods.refinedstorage2.api.storage.Actor;
import com.refinedmods.refinedstorage2.api.storage.EmptyActor;
import com.refinedmods.refinedstorage2.api.storage.InMemoryStorageImpl;
import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannel;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;
import com.refinedmods.refinedstorage2.api.storage.external.ExternalStorageProvider;
import com.refinedmods.refinedstorage2.api.storage.limited.LimitedStorageImpl;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedStorageImpl;
import com.refinedmods.refinedstorage2.network.test.AddNetworkNode;
import com.refinedmods.refinedstorage2.network.test.InjectNetwork;
import com.refinedmods.refinedstorage2.network.test.InjectNetworkStorageChannel;
import com.refinedmods.refinedstorage2.network.test.NetworkTest;
import com.refinedmods.refinedstorage2.network.test.NetworkTestFixtures;
import com.refinedmods.refinedstorage2.network.test.SetupNetwork;

import java.util.Iterator;
import java.util.Optional;
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
@SetupNetwork(id = "other")
class ExternalStorageNetworkNodeTest {
    private static final long ENERGY_USAGE = 5;

    @AddNetworkNode(properties = {
        @AddNetworkNode.Property(key = PROPERTY_ENERGY_USAGE, longValue = ENERGY_USAGE)
    })
    ExternalStorageNetworkNode sut;

    @Test
    void testInitialState(@InjectNetworkStorageChannel final StorageChannel<String> networkStorage) {
        // Act
        final long inserted = networkStorage.insert("A", 10, Action.EXECUTE, EmptyActor.INSTANCE);
        final long extracted = networkStorage.extract("A", 10, Action.EXECUTE, EmptyActor.INSTANCE);

        // Assert
        assertThat(inserted).isZero();
        assertThat(extracted).isZero();
        assertThat(sut.getAccessMode()).isEqualTo(AccessMode.INSERT_EXTRACT);
        assertThat(sut.getEnergyUsage()).isEqualTo(ENERGY_USAGE);
        assertThat(sut.getPriority()).isZero();
        assertThat(sut.getFilterMode()).isEqualTo(FilterMode.BLOCK);
        assertThat(networkStorage.getAll()).isEmpty();
        assertThat(networkStorage.getStored()).isZero();
    }

    @Test
    void shouldInitialize(@InjectNetworkStorageChannel final StorageChannel<String> networkStorage) {
        // Arrange
        final Storage<String> storage = new InMemoryStorageImpl<>();
        final ExternalStorageProvider<String> provider = new ExternalStorageProviderImpl<>(storage);

        // Act
        sut.initialize(new FakeExternalStorageProviderFactory(provider));

        // Assert
        assertThat(networkStorage.getAll()).isEmpty();
        assertThat(networkStorage.getStored()).isZero();
    }

    @Test
    void shouldBeAbleToInitializeMultipleTimes(
        @InjectNetworkStorageChannel final StorageChannel<String> networkStorage
    ) {
        // Arrange
        final Storage<String> storage1 = new InMemoryStorageImpl<>();
        final ExternalStorageProvider<String> provider1 = new ExternalStorageProviderImpl<>(storage1);

        final Storage<String> storage2 = new InMemoryStorageImpl<>();
        final ExternalStorageProvider<String> provider2 = new ExternalStorageProviderImpl<>(storage2);

        // Act
        sut.initialize(new FakeExternalStorageProviderFactory(provider1));
        networkStorage.insert("A", 10, Action.EXECUTE, EmptyActor.INSTANCE);

        sut.initialize(new FakeExternalStorageProviderFactory(provider2));
        networkStorage.insert("B", 1, Action.EXECUTE, EmptyActor.INSTANCE);

        // Assert
        assertThat(networkStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount<>("B", 1)
        );
        assertThat(networkStorage.getStored()).isEqualTo(1);
        assertThat(storage1.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount<>("A", 10)
        );
        assertThat(storage2.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount<>("B", 1)
        );
    }

    @Test
    void shouldInsert(@InjectNetworkStorageChannel final StorageChannel<String> networkStorage) {
        // Arrange
        final Storage<String> storage = new InMemoryStorageImpl<>();
        final ExternalStorageProvider<String> provider = new ExternalStorageProviderImpl<>(storage);
        sut.initialize(new FakeExternalStorageProviderFactory(provider));

        // Act
        final long inserted = networkStorage.insert("A", 10, Action.EXECUTE, EmptyActor.INSTANCE);

        // Assert
        assertThat(inserted).isEqualTo(10);
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount<>("A", 10)
        );
        assertThat(networkStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount<>("A", 10)
        );
        assertThat(networkStorage.getStored()).isEqualTo(10);
    }

    @Test
    void shouldExtract(@InjectNetworkStorageChannel final StorageChannel<String> networkStorage) {
        // Arrange
        final Storage<String> storage = new InMemoryStorageImpl<>();
        storage.insert("A", 10, Action.EXECUTE, EmptyActor.INSTANCE);
        final ExternalStorageProvider<String> provider = new ExternalStorageProviderImpl<>(storage);
        sut.initialize(new FakeExternalStorageProviderFactory(provider));

        // Act
        final long extracted = networkStorage.extract("A", 7, Action.EXECUTE, EmptyActor.INSTANCE);

        // Assert
        assertThat(extracted).isEqualTo(7);
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount<>("A", 3)
        );
        assertThat(networkStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount<>("A", 3)
        );
        assertThat(networkStorage.getStored()).isEqualTo(3);
    }

    @Test
    void shouldRespectAllowlistWhenInserting(@InjectNetworkStorageChannel final StorageChannel<String> networkStorage) {
        // Arrange
        sut.setFilterMode(FilterMode.ALLOW);
        sut.setFilterTemplates(Set.of("A", "B"));

        final Storage<String> storage = new InMemoryStorageImpl<>();
        final ExternalStorageProvider<String> provider = new ExternalStorageProviderImpl<>(storage);
        sut.initialize(new FakeExternalStorageProviderFactory(provider));

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
        @InjectNetworkStorageChannel final StorageChannel<String> networkStorage
    ) {
        // Arrange
        sut.setFilterMode(FilterMode.ALLOW);
        sut.setFilterTemplates(Set.of());

        final Storage<String> storage = new InMemoryStorageImpl<>();
        final ExternalStorageProvider<String> provider = new ExternalStorageProviderImpl<>(storage);
        sut.initialize(new FakeExternalStorageProviderFactory(provider));

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

        final Storage<String> storage = new InMemoryStorageImpl<>();
        final ExternalStorageProvider<String> provider = new ExternalStorageProviderImpl<>(storage);
        sut.initialize(new FakeExternalStorageProviderFactory(provider));

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

        final Storage<String> storage = new InMemoryStorageImpl<>();
        final ExternalStorageProvider<String> provider = new ExternalStorageProviderImpl<>(storage);
        sut.initialize(new FakeExternalStorageProviderFactory(provider));

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

        final Storage<String> storage = new InMemoryStorageImpl<>();
        final ExternalStorageProvider<String> provider = new ExternalStorageProviderImpl<>(storage);
        sut.initialize(new FakeExternalStorageProviderFactory(provider));

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

        final Storage<String> storage = new InMemoryStorageImpl<>();
        storage.insert("A", 20, Action.EXECUTE, EmptyActor.INSTANCE);
        final ExternalStorageProvider<String> provider = new ExternalStorageProviderImpl<>(storage);

        sut.initialize(new FakeExternalStorageProviderFactory(provider));

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
        final Storage<String> storage = new InMemoryStorageImpl<>();
        final ExternalStorageProvider<String> provider = new ExternalStorageProviderImpl<>(storage);

        sut.initialize(new FakeExternalStorageProviderFactory(provider));
        sut.setActive(false);

        // Act
        final long inserted = networkStorage.insert("A", 5, Action.EXECUTE, EmptyActor.INSTANCE);

        // Assert
        assertThat(inserted).isZero();
    }

    @Test
    void shouldNotExtractWhenInactive(@InjectNetworkStorageChannel final StorageChannel<String> networkStorage) {
        // Arrange
        final Storage<String> storage = new InMemoryStorageImpl<>();
        storage.insert("A", 5, Action.EXECUTE, EmptyActor.INSTANCE);
        final ExternalStorageProvider<String> provider = new ExternalStorageProviderImpl<>(storage);

        sut.initialize(new FakeExternalStorageProviderFactory(provider));
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
        final ExternalStorageProvider<String> provider = new ExternalStorageProviderImpl<>(storage);
        sut.initialize(new FakeExternalStorageProviderFactory(provider));

        networkStorage.insert("A", 50, Action.EXECUTE, EmptyActor.INSTANCE);
        networkStorage.insert("B", 50, Action.EXECUTE, EmptyActor.INSTANCE);

        // Act
        sut.setActive(false);

        // Assert
        assertThat(networkStorage.getAll()).isEmpty();
        assertThat(networkStorage.getStored()).isZero();
    }

    @Test
    void shouldShowStorageContentsWhenActive(@InjectNetworkStorageChannel final StorageChannel<String> networkStorage) {
        // Arrange
        final Storage<String> storage = new LimitedStorageImpl<>(100);
        final ExternalStorageProvider<String> provider = new ExternalStorageProviderImpl<>(storage);
        sut.initialize(new FakeExternalStorageProviderFactory(provider));

        networkStorage.insert("A", 50, Action.EXECUTE, EmptyActor.INSTANCE);
        networkStorage.insert("B", 50, Action.EXECUTE, EmptyActor.INSTANCE);

        sut.setActive(false);

        // Arrange
        sut.setActive(true);

        // Assert
        assertThat(networkStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount<>("A", 50),
            new ResourceAmount<>("B", 50)
        );
    }

    @Test
    void shouldNoLongerShowOnNetworkWhenRemoved(
        @InjectNetworkStorageChannel final StorageChannel<String> networkStorage,
        @InjectNetwork final Network network
    ) {
        // Arrange
        final Storage<String> storage = new LimitedStorageImpl<>(100);
        storage.insert("A", 100, Action.EXECUTE, EmptyActor.INSTANCE);
        final ExternalStorageProvider<String> provider = new ExternalStorageProviderImpl<>(storage);
        sut.initialize(new FakeExternalStorageProviderFactory(provider));

        // Act & assert
        network.removeContainer(() -> sut);
        assertThat(networkStorage.getAll()).isEmpty();

        storage.insert("B", 100, Action.EXECUTE, EmptyActor.INSTANCE);
        sut.detectChanges();

        assertThat(networkStorage.getAll()).isEmpty();
    }

    @Test
    void shouldNotifyNewNetworkAboutChangesWhenChangingNetworks(
        @InjectNetworkStorageChannel final StorageChannel<String> networkStorage,
        @InjectNetworkStorageChannel(networkId = "other") final StorageChannel<String> otherNetworkStorage,
        @InjectNetwork final Network network,
        @InjectNetwork("other") final Network otherNetwork
    ) {
        // Arrange
        final Storage<String> storage = new LimitedStorageImpl<>(100);
        storage.insert("A", 100, Action.EXECUTE, EmptyActor.INSTANCE);
        final ExternalStorageProvider<String> provider = new ExternalStorageProviderImpl<>(storage);
        sut.initialize(new FakeExternalStorageProviderFactory(provider));

        // Act & assert
        // Remove the external storage from the existing network, and add it to the other network.
        network.removeContainer(() -> sut);
        sut.setNetwork(otherNetwork);
        otherNetwork.addContainer(() -> sut);

        // The network storage should now be empty.
        assertThat(networkStorage.getAll()).isEmpty();

        // Now reinsert.
        storage.insert("B", 100, Action.EXECUTE, EmptyActor.INSTANCE);
        sut.detectChanges();

        // This is the desired state, the old parent should be cleaned up and not used.
        assertThat(networkStorage.getAll()).isEmpty();
        assertThat(otherNetworkStorage.getAll()).isNotEmpty();
    }

    @Test
    void shouldNoLongerNotifyOldNetworkAboutChangesWhenChangingNetworks(
        @InjectNetworkStorageChannel final StorageChannel<String> networkStorage,
        @InjectNetworkStorageChannel(networkId = "other") final StorageChannel<String> otherNetworkStorage,
        @InjectNetwork final Network network,
        @InjectNetwork("other") final Network otherNetwork
    ) {
        // Arrange
        final Storage<String> storage = new LimitedStorageImpl<>(100);
        storage.insert("A", 100, Action.EXECUTE, EmptyActor.INSTANCE);
        final ExternalStorageProvider<String> provider = new ExternalStorageProviderImpl<>(storage);
        sut.initialize(new FakeExternalStorageProviderFactory(provider));

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
        storage.insert("B", 100, Action.EXECUTE, EmptyActor.INSTANCE);
        sut.detectChanges();

        // This is the desired state, the old parent should be cleaned up and not used.
        assertThat(networkStorage.getAll()).isEmpty();
        assertThat(otherNetworkStorage.getAll()).isNotEmpty();
    }

    @Test
    void shouldNoLongerShowOnNetworkWhenRemovedWithoutInitializedStorage(
        @InjectNetworkStorageChannel final StorageChannel<String> networkStorage,
        @InjectNetwork final Network network
    ) {
        // Act
        network.removeContainer(() -> sut);
        sut.detectChanges();

        // Assert
        assertThat(networkStorage.getAll()).isEmpty();
    }

    /* TODO - change tracking
    @Test
    void shouldTrackChanges(@InjectNetworkStorageChannel final StorageChannel<String> networkStorage) {
        // Arrange
        final Storage<String> storage = new TrackedStorageImpl<>(new LimitedStorageImpl<>(100), () -> 0L);
        final ExternalStorageProvider<String> provider = new ExternalStorageProviderImpl<>(storage);
        sut.initialize(new FakeExternalStorageProviderFactory(provider));

        // Act
        final long inserted = networkStorage.insert("A", 10, Action.EXECUTE, FakeActor.INSTANCE);

        // Assert
        assertThat(inserted).isEqualTo(10);
        assertThat(networkStorage.findTrackedResourceByActorType("A", FakeActor.class)).isNotEmpty();
    } */

    @Test
    void shouldDetectChanges(@InjectNetworkStorageChannel final StorageChannel<String> networkStorage) {
        // Arrange
        final Storage<String> storage = new LimitedStorageImpl<>(100);
        final ExternalStorageProvider<String> provider = new ExternalStorageProviderImpl<>(storage);
        sut.initialize(new FakeExternalStorageProviderFactory(provider));

        // Act
        storage.insert("A", 10, Action.EXECUTE, EmptyActor.INSTANCE);
        final boolean hasChanges1 = sut.detectChanges();
        networkStorage.insert("B", 1, Action.EXECUTE, EmptyActor.INSTANCE);
        final boolean hasChanges2 = sut.detectChanges();

        // Assert
        assertThat(hasChanges1).isTrue();
        assertThat(hasChanges2).isFalse();
        assertThat(networkStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount<>("A", 10),
            new ResourceAmount<>("B", 1)
        );
    }

    @Nested
    class PriorityTest {
        @AddNetworkNode
        ExternalStorageNetworkNode otherStorage;

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        void shouldRespectPriority(final boolean oneHasPriority,
                                   @InjectNetworkStorageChannel final StorageChannel<String> networkStorage) {
            // Arrange
            final Storage<String> storage1 = new TrackedStorageImpl<>(new LimitedStorageImpl<>(100), () -> 0L);
            final ExternalStorageProvider<String> provider1 = new ExternalStorageProviderImpl<>(storage1);
            sut.initialize(new FakeExternalStorageProviderFactory(provider1));

            final Storage<String> storage2 = new TrackedStorageImpl<>(new LimitedStorageImpl<>(100), () -> 0L);
            final ExternalStorageProvider<String> provider2 = new ExternalStorageProviderImpl<>(storage2);
            otherStorage.initialize(new FakeExternalStorageProviderFactory(provider2));

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

    private record FakeExternalStorageProviderFactory(ExternalStorageProvider<String> provider)
        implements ExternalStorageProviderFactory {
        @Override
        @SuppressWarnings("unchecked")
        public <T> Optional<ExternalStorageProvider<T>> create(final StorageChannelType<T> channelType) {
            if (channelType == NetworkTestFixtures.STORAGE_CHANNEL_TYPE) {
                return Optional.of((ExternalStorageProvider<T>) provider);
            }
            return Optional.empty();
        }
    }

    private static class ExternalStorageProviderImpl<T> implements ExternalStorageProvider<T> {
        private final Storage<T> storage;

        ExternalStorageProviderImpl(final Storage<T> storage) {
            this.storage = storage;
        }

        @Override
        public long extract(final T resource, final long amount, final Action action, final Actor actor) {
            return storage.extract(resource, amount, action, actor);
        }

        @Override
        public long insert(final T resource, final long amount, final Action action, final Actor actor) {
            return storage.insert(resource, amount, action, actor);
        }

        @Override
        public Iterator<ResourceAmount<T>> iterator() {
            return storage.getAll().iterator();
        }
    }
}
