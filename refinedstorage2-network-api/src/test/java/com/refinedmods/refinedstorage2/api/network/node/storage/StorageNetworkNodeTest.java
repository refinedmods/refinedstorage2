package com.refinedmods.refinedstorage2.api.network.node.storage;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.core.filter.FilterMode;
import com.refinedmods.refinedstorage2.api.network.Network;
import com.refinedmods.refinedstorage2.api.network.test.StorageChannelTypes;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.storage.AccessMode;
import com.refinedmods.refinedstorage2.api.storage.EmptySource;
import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.StorageRepository;
import com.refinedmods.refinedstorage2.api.storage.StorageRepositoryImpl;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;
import com.refinedmods.refinedstorage2.api.storage.limited.LimitedStorage;
import com.refinedmods.refinedstorage2.api.storage.limited.LimitedStorageImpl;
import com.refinedmods.refinedstorage2.test.Rs2Test;

import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import static com.refinedmods.refinedstorage2.api.network.NetworkUtil.create;
import static com.refinedmods.refinedstorage2.api.network.NetworkUtil.fakeStorageChannelOf;
import static org.assertj.core.api.Assertions.assertThat;

@Rs2Test
class StorageNetworkNodeTest {
    private static final int ENERGY_USAGE = 5;

    private Network network;
    private StorageNetworkNode<String> sut;
    private StorageRepository storageRepository;

    @BeforeEach
    void setUp() {
        network = create();
        sut = new StorageNetworkNode<>(ENERGY_USAGE, StorageChannelTypes.FAKE);
        storageRepository = new StorageRepositoryImpl();
    }

    private <T> Storage<T> storageOf(StorageNetworkNode<T> storage, StorageChannelType<T> type) {
        return storage.getStorageForChannel(type).get();
    }

    private Storage<String> storageOf(StorageNetworkNode<String> storage) {
        return storageOf(storage, StorageChannelTypes.FAKE);
    }

    private void initializeStorageIntoNetwork() {
        initializeStorageIntoNetwork(sut);
    }

    private void initializeStorageIntoNetwork(StorageNetworkNode<String> storage) {
        storage.setNetwork(network);
        network.addContainer(() -> storage);
    }

    @Test
    void Test_initial_state() {
        // Assert
        assertThat(sut.getEnergyUsage()).isEqualTo(ENERGY_USAGE);
        assertThat(sut.getAccessMode()).isEqualTo(AccessMode.INSERT_EXTRACT);
        assertThat(sut.getFilterMode()).isEqualTo(FilterMode.BLOCK);
        assertThat(sut.getStored()).isZero();
        assertThat(sut.getCapacity()).isZero();
        assertThat(fakeStorageChannelOf(network).getAll()).isEmpty();
        assertThat(sut.getStorageForChannel(StorageChannelTypes.FAKE)).isEmpty();
    }

    @Test
    void Test_initializing_new_storage() {
        // Arrange
        LimitedStorage<String> limitedStorage = new LimitedStorageImpl<>(100);
        limitedStorage.insert("A", 50, Action.EXECUTE, EmptySource.INSTANCE);
        UUID storageId = UUID.randomUUID();

        // Act
        sut.initializeNewStorage(storageRepository, limitedStorage, storageId);
        initializeStorageIntoNetwork();

        // Assert
        assertThat(sut.getStored()).isEqualTo(50L);
        assertThat(sut.getCapacity()).isEqualTo(100L);
        assertThat(storageRepository.get(storageId)).isNotEmpty();
        assertThat(storageOf(sut).getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount<>("A", 50L)
        );
        assertThat(fakeStorageChannelOf(network).getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount<>("A", 50L)
        );
        assertThat(storageOf(sut).getStored()).isEqualTo(50L);
        assertThat(sut.getStorageForChannel(StorageChannelTypes.FAKE)).isPresent();
    }

    @Test
    void Test_initializing_existing_storage() {
        // Arrange
        LimitedStorage<String> limitedStorage = new LimitedStorageImpl<>(100);
        limitedStorage.insert("A", 50, Action.EXECUTE, EmptySource.INSTANCE);
        UUID storageId = UUID.randomUUID();
        storageRepository.set(storageId, limitedStorage);

        // Act
        sut.initializeExistingStorage(storageRepository, storageId);
        initializeStorageIntoNetwork();

        // Assert
        assertThat(sut.getStored()).isEqualTo(50L);
        assertThat(sut.getCapacity()).isEqualTo(100L);
        assertThat(storageOf(sut).getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount<>("A", 50L)
        );
        assertThat(fakeStorageChannelOf(network).getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount<>("A", 50L)
        );
        assertThat(storageOf(sut).getStored()).isEqualTo(50L);
        assertThat(sut.getStorageForChannel(StorageChannelTypes.FAKE)).isPresent();
    }

    @Test
    void Test_initializing_non_existent_storage() {
        // Act
        sut.initializeExistingStorage(storageRepository, UUID.randomUUID());
        initializeStorageIntoNetwork();

        // Assert
        assertThat(sut.getStored()).isZero();
        assertThat(sut.getCapacity()).isZero();
        assertThat(fakeStorageChannelOf(network).getAll()).isEmpty();
    }

    @Test
    void Test_inserting() {
        // Arrange
        Storage<String> storage = new LimitedStorageImpl<>(100);
        sut.initializeNewStorage(storageRepository, storage, UUID.randomUUID());

        initializeStorageIntoNetwork();

        // Act
        long inserted = fakeStorageChannelOf(network).insert("A", 100, Action.EXECUTE, EmptySource.INSTANCE);

        // Assert
        assertThat(inserted).isEqualTo(100);
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount<>("A", 100)
        );
        assertThat(fakeStorageChannelOf(network).getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
                new ResourceAmount<>("A", 100)
        );
        assertThat(storageOf(sut).getStored()).isEqualTo(100L);
    }

    @Test
    void Test_extracting() {
        // Arrange
        Storage<String> storage = new LimitedStorageImpl<>(200);
        storage.insert("A", 100, Action.EXECUTE, EmptySource.INSTANCE);
        storage.insert("B", 50, Action.EXECUTE, EmptySource.INSTANCE);
        sut.initializeNewStorage(storageRepository, storage, UUID.randomUUID());

        initializeStorageIntoNetwork();

        // Act
        long extracted = fakeStorageChannelOf(network).extract("A", 30, Action.EXECUTE, EmptySource.INSTANCE);

        // Assert
        assertThat(extracted).isEqualTo(30);

        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount<>("A", 70),
                new ResourceAmount<>("B", 50)
        );
        assertThat(fakeStorageChannelOf(network).getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
                new ResourceAmount<>("A", 70),
                new ResourceAmount<>("B", 50)
        );
        assertThat(storageOf(sut).getStored()).isEqualTo(70 + 50);
    }

    @Test
    void Test_inserting_with_allowlist_filter() {
        // Arrange
        sut.setFilterMode(FilterMode.ALLOW);
        sut.setFilterTemplates(Set.of("A", "B"));

        Storage<String> storage = new LimitedStorageImpl<>(100);
        sut.initializeNewStorage(storageRepository, storage, UUID.randomUUID());

        initializeStorageIntoNetwork();

        // Act
        long inserted1 = storageOf(sut).insert("A", 12, Action.EXECUTE, EmptySource.INSTANCE);
        long inserted2 = storageOf(sut).insert("B", 12, Action.EXECUTE, EmptySource.INSTANCE);
        long inserted3 = storageOf(sut).insert("C", 10, Action.EXECUTE, EmptySource.INSTANCE);

        // Assert
        assertThat(inserted1).isEqualTo(12);
        assertThat(inserted2).isEqualTo(12);
        assertThat(inserted3).isZero();
    }

    @Test
    void Test_inserting_with_empty_allowlist_filter() {
        // Arrange
        sut.setFilterMode(FilterMode.ALLOW);
        sut.setFilterTemplates(Set.of());

        Storage<String> storage = new LimitedStorageImpl<>(100);
        sut.initializeNewStorage(storageRepository, storage, UUID.randomUUID());

        initializeStorageIntoNetwork();

        // Act
        long inserted1 = storageOf(sut).insert("A", 12, Action.EXECUTE, EmptySource.INSTANCE);
        long inserted2 = storageOf(sut).insert("B", 12, Action.EXECUTE, EmptySource.INSTANCE);
        long inserted3 = storageOf(sut).insert("C", 10, Action.EXECUTE, EmptySource.INSTANCE);

        // Assert
        assertThat(inserted1).isZero();
        assertThat(inserted2).isZero();
        assertThat(inserted3).isZero();
    }

    @Test
    void Test_inserting_with_blocklist_filter() {
        // Arrange
        sut.setFilterMode(FilterMode.BLOCK);
        sut.setFilterTemplates(Set.of("A", "B"));

        Storage<String> storage = new LimitedStorageImpl<>(100);
        sut.initializeNewStorage(storageRepository, storage, UUID.randomUUID());

        initializeStorageIntoNetwork();

        // Act
        long inserted1 = storageOf(sut).insert("A", 12, Action.EXECUTE, EmptySource.INSTANCE);
        long inserted2 = storageOf(sut).insert("B", 12, Action.EXECUTE, EmptySource.INSTANCE);
        long inserted3 = storageOf(sut).insert("C", 10, Action.EXECUTE, EmptySource.INSTANCE);

        // Assert
        assertThat(inserted1).isZero();
        assertThat(inserted2).isZero();
        assertThat(inserted3).isEqualTo(10);
    }

    @Test
    void Test_inserting_with_empty_blocklist_filter() {
        // Arrange
        sut.setFilterMode(FilterMode.BLOCK);
        sut.setFilterTemplates(Set.of());

        Storage<String> storage = new LimitedStorageImpl<>(100);
        sut.initializeNewStorage(storageRepository, storage, UUID.randomUUID());

        initializeStorageIntoNetwork();

        // Act
        long inserted1 = storageOf(sut).insert("A", 12, Action.EXECUTE, EmptySource.INSTANCE);
        long inserted2 = storageOf(sut).insert("B", 12, Action.EXECUTE, EmptySource.INSTANCE);
        long inserted3 = storageOf(sut).insert("C", 10, Action.EXECUTE, EmptySource.INSTANCE);

        // Assert
        assertThat(inserted1).isEqualTo(12);
        assertThat(inserted2).isEqualTo(12);
        assertThat(inserted3).isEqualTo(10);
    }

    @ParameterizedTest
    @EnumSource(AccessMode.class)
    void Test_inserting_with_access_mode(AccessMode accessMode) {
        // Arrange
        sut.setAccessMode(accessMode);

        Storage<String> storage = new LimitedStorageImpl<>(100);
        sut.initializeNewStorage(storageRepository, storage, UUID.randomUUID());

        initializeStorageIntoNetwork();

        // Act
        long inserted = storageOf(sut).insert("A", 5, Action.EXECUTE, EmptySource.INSTANCE);

        // Assert
        switch (accessMode) {
            case INSERT_EXTRACT, INSERT -> assertThat(inserted).isEqualTo(5);
            case EXTRACT -> assertThat(inserted).isZero();
        }
    }

    @ParameterizedTest
    @EnumSource(AccessMode.class)
    void Test_extracting_with_access_mode(AccessMode accessMode) {
        // Arrange
        sut.setAccessMode(accessMode);

        Storage<String> storage = new LimitedStorageImpl<>(100);
        sut.initializeNewStorage(storageRepository, storage, UUID.randomUUID());

        initializeStorageIntoNetwork();

        storage.insert("A", 20, Action.EXECUTE, EmptySource.INSTANCE);

        // Act
        long extracted = storageOf(sut).extract("A", 5, Action.EXECUTE, EmptySource.INSTANCE);

        // Assert
        switch (accessMode) {
            case INSERT_EXTRACT, EXTRACT -> assertThat(extracted).isEqualTo(5);
            case INSERT -> assertThat(extracted).isZero();
        }
    }

    @Test
    void Test_inserting_when_inactive() {
        // Arrange
        Storage<String> storage = new LimitedStorageImpl<>(100);
        sut.initializeNewStorage(storageRepository, storage, UUID.randomUUID());

        initializeStorageIntoNetwork();

        sut.setActivenessProvider(() -> false);

        // Act
        long inserted = storageOf(sut).insert("A", 5, Action.EXECUTE, EmptySource.INSTANCE);

        // Assert
        assertThat(inserted).isZero();
    }

    @Test
    void Test_extracting_when_inactive() {
        // Arrange
        Storage<String> storage = new LimitedStorageImpl<>(100);
        sut.initializeNewStorage(storageRepository, storage, UUID.randomUUID());

        initializeStorageIntoNetwork();

        sut.setActivenessProvider(() -> false);

        // Act
        long extracted = storageOf(sut).extract("A", 5, Action.EXECUTE, EmptySource.INSTANCE);

        // Assert
        assertThat(extracted).isZero();
    }

    @Test
    void Test_inactiveness() {
        // Arrange
        Storage<String> storage = new LimitedStorageImpl<>(100);
        storage.insert("A", 50, Action.EXECUTE, EmptySource.INSTANCE);
        storage.insert("B", 50, Action.EXECUTE, EmptySource.INSTANCE);
        sut.initializeNewStorage(storageRepository, storage, UUID.randomUUID());

        initializeStorageIntoNetwork();

        // Act
        sut.onActiveChanged(false);

        // Assert
        assertThat(storageOf(sut).getAll()).isNotEmpty();
        assertThat(fakeStorageChannelOf(network).getAll()).isEmpty();
    }

    @Test
    void Test_activeness() {
        // Arrange
        Storage<String> storage = new LimitedStorageImpl<>(100);
        storage.insert("A", 50, Action.EXECUTE, EmptySource.INSTANCE);
        storage.insert("B", 50, Action.EXECUTE, EmptySource.INSTANCE);
        sut.initializeNewStorage(storageRepository, storage, UUID.randomUUID());

        initializeStorageIntoNetwork();

        // Act
        sut.onActiveChanged(true);

        // Assert
        assertThat(storageOf(sut).getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
                new ResourceAmount<>("A", 50),
                new ResourceAmount<>("B", 50)
        );
        assertThat(fakeStorageChannelOf(network).getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
                new ResourceAmount<>("A", 50),
                new ResourceAmount<>("B", 50)
        );
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void Test_setting_priority(boolean oneHasPriority) {
        // Arrange
        sut.initializeNewStorage(storageRepository, new LimitedStorageImpl<>(100), UUID.randomUUID());

        StorageNetworkNode<String> sut2 = new StorageNetworkNode<>(ENERGY_USAGE, StorageChannelTypes.FAKE);
        sut2.initializeNewStorage(storageRepository, new LimitedStorageImpl<>(100), UUID.randomUUID());

        initializeStorageIntoNetwork();
        initializeStorageIntoNetwork(sut2);

        if (oneHasPriority) {
            sut.setPriority(5);
            sut2.setPriority(2);
        } else {
            sut.setPriority(2);
            sut2.setPriority(5);
        }

        // Act
        fakeStorageChannelOf(network).insert("A", 1, Action.EXECUTE, EmptySource.INSTANCE);

        // Assert
        if (oneHasPriority) {
            assertThat(storageOf(sut).getAll()).isNotEmpty();
            assertThat(storageOf(sut2).getAll()).isEmpty();
        } else {
            assertThat(storageOf(sut).getAll()).isEmpty();
            assertThat(storageOf(sut2).getAll()).isNotEmpty();
        }
    }
}
