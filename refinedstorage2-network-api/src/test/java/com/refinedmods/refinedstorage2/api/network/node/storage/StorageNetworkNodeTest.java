package com.refinedmods.refinedstorage2.api.network.node.storage;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.core.filter.FilterMode;
import com.refinedmods.refinedstorage2.api.network.extension.AddNetworkNode;
import com.refinedmods.refinedstorage2.api.network.extension.InjectNetworkStorageChannel;
import com.refinedmods.refinedstorage2.api.network.extension.NetworkTestExtension;
import com.refinedmods.refinedstorage2.api.network.extension.SetupNetwork;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.storage.AccessMode;
import com.refinedmods.refinedstorage2.api.storage.EmptySource;
import com.refinedmods.refinedstorage2.api.storage.Source;
import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.StorageRepository;
import com.refinedmods.refinedstorage2.api.storage.StorageRepositoryImpl;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannel;
import com.refinedmods.refinedstorage2.api.storage.limited.LimitedStorage;
import com.refinedmods.refinedstorage2.api.storage.limited.LimitedStorageImpl;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedStorageImpl;
import com.refinedmods.refinedstorage2.test.Rs2Test;

import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

@Rs2Test
@ExtendWith(NetworkTestExtension.class)
@SetupNetwork
class StorageNetworkNodeTest {
    private static final int ENERGY_USAGE = 5;

    @AddNetworkNode(energyUsage = ENERGY_USAGE)
    StorageNetworkNode<String> sut;

    StorageRepository storageRepository;

    @BeforeEach
    void setUp() {
        storageRepository = new StorageRepositoryImpl();
    }

    @Test
    void Test_initial_state(@InjectNetworkStorageChannel StorageChannel<String> networkStorage) {
        // Assert
        assertThat(sut.getEnergyUsage()).isEqualTo(ENERGY_USAGE);
        assertThat(sut.getAccessMode()).isEqualTo(AccessMode.INSERT_EXTRACT);
        assertThat(sut.getFilterMode()).isEqualTo(FilterMode.BLOCK);
        assertThat(sut.getStored()).isZero();
        assertThat(sut.getCapacity()).isZero();
        assertThat(networkStorage.getAll()).isEmpty();
    }

    @Test
    void Test_initializing_new_storage(@InjectNetworkStorageChannel StorageChannel<String> networkStorage) {
        // Arrange
        LimitedStorage<String> limitedStorage = new LimitedStorageImpl<>(100);
        limitedStorage.insert("A", 50, Action.EXECUTE, EmptySource.INSTANCE);
        UUID storageId = UUID.randomUUID();

        // Act
        sut.initializeNewStorage(storageRepository, limitedStorage, storageId);
        activateStorage();

        // Assert
        assertThat(sut.getStored()).isEqualTo(50L);
        assertThat(sut.getCapacity()).isEqualTo(100L);
        assertThat(storageRepository.get(storageId)).isNotEmpty();
        assertThat(networkStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount<>("A", 50L)
        );
    }

    @Test
    void Test_initializing_existing_storage(@InjectNetworkStorageChannel StorageChannel<String> networkStorage) {
        // Arrange
        LimitedStorage<String> limitedStorage = new LimitedStorageImpl<>(100);
        limitedStorage.insert("A", 50, Action.EXECUTE, EmptySource.INSTANCE);
        UUID storageId = UUID.randomUUID();
        storageRepository.set(storageId, limitedStorage);

        // Act
        sut.initializeExistingStorage(storageRepository, storageId);
        activateStorage();

        // Assert
        assertThat(sut.getStored()).isEqualTo(50L);
        assertThat(sut.getCapacity()).isEqualTo(100L);
        assertThat(networkStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount<>("A", 50L)
        );
    }

    @Test
    void Test_initializing_non_existent_storage(@InjectNetworkStorageChannel StorageChannel<String> networkStorage) {
        // Act
        sut.initializeExistingStorage(storageRepository, UUID.randomUUID());
        activateStorage();

        // Assert
        assertThat(sut.getStored()).isZero();
        assertThat(sut.getCapacity()).isZero();
        assertThat(networkStorage.getAll()).isEmpty();
    }

    @Test
    void Test_inserting(@InjectNetworkStorageChannel StorageChannel<String> networkStorage) {
        // Arrange
        Storage<String> storage = new LimitedStorageImpl<>(100);
        sut.initializeNewStorage(storageRepository, storage, UUID.randomUUID());
        activateStorage();

        // Act
        long inserted = networkStorage.insert("A", 100, Action.EXECUTE, EmptySource.INSTANCE);

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
    void Test_extracting(@InjectNetworkStorageChannel StorageChannel<String> networkStorage) {
        // Arrange
        Storage<String> storage = new LimitedStorageImpl<>(200);
        storage.insert("A", 100, Action.EXECUTE, EmptySource.INSTANCE);
        storage.insert("B", 50, Action.EXECUTE, EmptySource.INSTANCE);
        sut.initializeNewStorage(storageRepository, storage, UUID.randomUUID());
        activateStorage();

        // Act
        long extracted = networkStorage.extract("A", 30, Action.EXECUTE, EmptySource.INSTANCE);

        // Assert
        assertThat(extracted).isEqualTo(30);
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount<>("A", 70),
                new ResourceAmount<>("B", 50)
        );
        assertThat(networkStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
                new ResourceAmount<>("A", 70),
                new ResourceAmount<>("B", 50)
        );
    }

    @Test
    void Test_inserting_with_allowlist_filter(@InjectNetworkStorageChannel StorageChannel<String> networkStorage) {
        // Arrange
        sut.setFilterMode(FilterMode.ALLOW);
        sut.setFilterTemplates(Set.of("A", "B"));

        Storage<String> storage = new LimitedStorageImpl<>(100);
        sut.initializeNewStorage(storageRepository, storage, UUID.randomUUID());
        activateStorage();

        // Act
        long inserted1 = networkStorage.insert("A", 12, Action.EXECUTE, EmptySource.INSTANCE);
        long inserted2 = networkStorage.insert("B", 12, Action.EXECUTE, EmptySource.INSTANCE);
        long inserted3 = networkStorage.insert("C", 10, Action.EXECUTE, EmptySource.INSTANCE);

        // Assert
        assertThat(inserted1).isEqualTo(12);
        assertThat(inserted2).isEqualTo(12);
        assertThat(inserted3).isZero();
    }

    @Test
    void Test_inserting_with_empty_allowlist_filter(@InjectNetworkStorageChannel StorageChannel<String> networkStorage) {
        // Arrange
        sut.setFilterMode(FilterMode.ALLOW);
        sut.setFilterTemplates(Set.of());

        Storage<String> storage = new LimitedStorageImpl<>(100);
        sut.initializeNewStorage(storageRepository, storage, UUID.randomUUID());
        activateStorage();

        // Act
        long inserted1 = networkStorage.insert("A", 12, Action.EXECUTE, EmptySource.INSTANCE);
        long inserted2 = networkStorage.insert("B", 12, Action.EXECUTE, EmptySource.INSTANCE);
        long inserted3 = networkStorage.insert("C", 10, Action.EXECUTE, EmptySource.INSTANCE);

        // Assert
        assertThat(inserted1).isZero();
        assertThat(inserted2).isZero();
        assertThat(inserted3).isZero();
    }

    @Test
    void Test_inserting_with_blocklist_filter(@InjectNetworkStorageChannel StorageChannel<String> networkStorage) {
        // Arrange
        sut.setFilterMode(FilterMode.BLOCK);
        sut.setFilterTemplates(Set.of("A", "B"));

        Storage<String> storage = new LimitedStorageImpl<>(100);
        sut.initializeNewStorage(storageRepository, storage, UUID.randomUUID());
        activateStorage();

        // Act
        long inserted1 = networkStorage.insert("A", 12, Action.EXECUTE, EmptySource.INSTANCE);
        long inserted2 = networkStorage.insert("B", 12, Action.EXECUTE, EmptySource.INSTANCE);
        long inserted3 = networkStorage.insert("C", 10, Action.EXECUTE, EmptySource.INSTANCE);

        // Assert
        assertThat(inserted1).isZero();
        assertThat(inserted2).isZero();
        assertThat(inserted3).isEqualTo(10);
    }

    @Test
    void Test_inserting_with_empty_blocklist_filter(@InjectNetworkStorageChannel StorageChannel<String> networkStorage) {
        // Arrange
        sut.setFilterMode(FilterMode.BLOCK);
        sut.setFilterTemplates(Set.of());

        Storage<String> storage = new LimitedStorageImpl<>(100);
        sut.initializeNewStorage(storageRepository, storage, UUID.randomUUID());
        activateStorage();

        // Act
        long inserted1 = networkStorage.insert("A", 12, Action.EXECUTE, EmptySource.INSTANCE);
        long inserted2 = networkStorage.insert("B", 12, Action.EXECUTE, EmptySource.INSTANCE);
        long inserted3 = networkStorage.insert("C", 10, Action.EXECUTE, EmptySource.INSTANCE);

        // Assert
        assertThat(inserted1).isEqualTo(12);
        assertThat(inserted2).isEqualTo(12);
        assertThat(inserted3).isEqualTo(10);
    }

    @ParameterizedTest
    @EnumSource(AccessMode.class)
    void Test_inserting_with_access_mode(AccessMode accessMode, @InjectNetworkStorageChannel StorageChannel<String> networkStorage) {
        // Arrange
        sut.setAccessMode(accessMode);

        Storage<String> storage = new LimitedStorageImpl<>(100);
        sut.initializeNewStorage(storageRepository, storage, UUID.randomUUID());
        activateStorage();

        // Act
        long inserted = networkStorage.insert("A", 5, Action.EXECUTE, EmptySource.INSTANCE);

        // Assert
        switch (accessMode) {
            case INSERT_EXTRACT, INSERT -> assertThat(inserted).isEqualTo(5);
            case EXTRACT -> assertThat(inserted).isZero();
        }
    }

    @ParameterizedTest
    @EnumSource(AccessMode.class)
    void Test_extracting_with_access_mode(AccessMode accessMode, @InjectNetworkStorageChannel StorageChannel<String> networkStorage) {
        // Arrange
        sut.setAccessMode(accessMode);

        Storage<String> storage = new LimitedStorageImpl<>(100);
        storage.insert("A", 20, Action.EXECUTE, EmptySource.INSTANCE);
        sut.initializeNewStorage(storageRepository, storage, UUID.randomUUID());
        activateStorage();

        // Act
        long extracted = networkStorage.extract("A", 5, Action.EXECUTE, EmptySource.INSTANCE);

        // Assert
        switch (accessMode) {
            case INSERT_EXTRACT, EXTRACT -> assertThat(extracted).isEqualTo(5);
            case INSERT -> assertThat(extracted).isZero();
        }
    }

    @Test
    void Test_inserting_when_inactive(@InjectNetworkStorageChannel StorageChannel<String> networkStorage) {
        // Arrange
        Storage<String> storage = new LimitedStorageImpl<>(100);
        sut.initializeNewStorage(storageRepository, storage, UUID.randomUUID());
        activateStorage();

        sut.setActivenessProvider(() -> false);

        // Act
        long inserted = networkStorage.insert("A", 5, Action.EXECUTE, EmptySource.INSTANCE);

        // Assert
        assertThat(inserted).isZero();
    }

    @Test
    void Test_extracting_when_inactive(@InjectNetworkStorageChannel StorageChannel<String> networkStorage) {
        // Arrange
        Storage<String> storage = new LimitedStorageImpl<>(100);
        sut.initializeNewStorage(storageRepository, storage, UUID.randomUUID());
        activateStorage();

        sut.setActivenessProvider(() -> false);

        // Act
        long extracted = networkStorage.extract("A", 5, Action.EXECUTE, EmptySource.INSTANCE);

        // Assert
        assertThat(extracted).isZero();
    }

    @Test
    void Test_inactiveness(@InjectNetworkStorageChannel StorageChannel<String> networkStorage) {
        // Arrange
        Storage<String> storage = new LimitedStorageImpl<>(100);
        storage.insert("A", 50, Action.EXECUTE, EmptySource.INSTANCE);
        storage.insert("B", 50, Action.EXECUTE, EmptySource.INSTANCE);
        sut.initializeNewStorage(storageRepository, storage, UUID.randomUUID());
        activateStorage();

        // Act
        sut.onActiveChanged(false);

        // Assert
        assertThat(networkStorage.getAll()).isEmpty();
    }

    @Test
    void Test_activeness(@InjectNetworkStorageChannel StorageChannel<String> networkStorage) {
        // Arrange
        Storage<String> storage = new LimitedStorageImpl<>(100);
        storage.insert("A", 50, Action.EXECUTE, EmptySource.INSTANCE);
        storage.insert("B", 50, Action.EXECUTE, EmptySource.INSTANCE);
        sut.initializeNewStorage(storageRepository, storage, UUID.randomUUID());

        // Act
        sut.onActiveChanged(true);

        // Assert
        assertThat(networkStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
                new ResourceAmount<>("A", 50),
                new ResourceAmount<>("B", 50)
        );
    }

    @Test
    void Test_tracking_changes(@InjectNetworkStorageChannel StorageChannel<String> networkStorage) {
        // Arrange
        sut.initializeNewStorage(storageRepository, new TrackedStorageImpl<>(new LimitedStorageImpl<>(100), () -> 0L), UUID.randomUUID());
        activateStorage();

        // Act
        long inserted = networkStorage.insert("A", 10, Action.EXECUTE, CustomSource1.INSTANCE);

        // Assert
        assertThat(inserted).isEqualTo(10);
        assertThat(networkStorage.findTrackedResourceBySourceType("A", CustomSource1.class)).isNotEmpty();
    }

    private static class CustomSource1 implements Source {
        private static final Source INSTANCE = new CustomSource1();

        @Override
        public String getName() {
            return "Custom1";
        }
    }

    @Nested
    class PriorityTest {
        @AddNetworkNode
        StorageNetworkNode<String> otherStorage;

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        void Test_setting_priority(boolean oneHasPriority, @InjectNetworkStorageChannel StorageChannel<String> networkStorage) {
            // Arrange
            LimitedStorageImpl<String> storage1 = new LimitedStorageImpl<>(100);
            sut.initializeNewStorage(storageRepository, storage1, UUID.randomUUID());
            sut.onActiveChanged(true);

            LimitedStorageImpl<String> storage2 = new LimitedStorageImpl<>(100);
            otherStorage.initializeNewStorage(storageRepository, storage2, UUID.randomUUID());
            otherStorage.onActiveChanged(true);

            if (oneHasPriority) {
                sut.setPriority(5);
                otherStorage.setPriority(2);
            } else {
                sut.setPriority(2);
                otherStorage.setPriority(5);
            }

            // Act
            networkStorage.insert("A", 1, Action.EXECUTE, EmptySource.INSTANCE);

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

    private void activateStorage() {
        sut.onActiveChanged(true);
    }
}
