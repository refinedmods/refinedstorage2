package com.refinedmods.refinedstorage2.api.network.node.diskdrive;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.core.filter.FilterMode;
import com.refinedmods.refinedstorage2.api.network.test.extension.AddDiskDrive;
import com.refinedmods.refinedstorage2.api.network.test.extension.InjectNetworkStorageChannel;
import com.refinedmods.refinedstorage2.api.network.test.extension.NetworkTestExtension;
import com.refinedmods.refinedstorage2.api.network.test.extension.SetupNetwork;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.storage.AccessMode;
import com.refinedmods.refinedstorage2.api.storage.EmptySource;
import com.refinedmods.refinedstorage2.api.storage.InMemoryStorageImpl;
import com.refinedmods.refinedstorage2.api.storage.Source;
import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannel;
import com.refinedmods.refinedstorage2.api.storage.limited.LimitedStorageImpl;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedStorageImpl;
import com.refinedmods.refinedstorage2.test.Rs2Test;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.verification.VerificationMode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@Rs2Test
@ExtendWith(NetworkTestExtension.class)
@SetupNetwork
class DiskDriveNetworkNodeTest {
    private static final long BASE_USAGE = 10;
    private static final long USAGE_PER_DISK = 3;

    @AddDiskDrive(baseEnergyUsage = BASE_USAGE, energyUsagePerDisk = USAGE_PER_DISK)
    DiskDriveNetworkNode sut;

    FakeStorageProviderRepository storageProviderRepository;
    DiskDriveListener diskDriveListener;

    @BeforeEach
    void setUp() {
        diskDriveListener = mock(DiskDriveListener.class);
        storageProviderRepository = new FakeStorageProviderRepository();
        sut.setDiskProvider(storageProviderRepository);
        sut.setListener(diskDriveListener);
    }

    @Test
    void Test_initialization(@InjectNetworkStorageChannel StorageChannel<String> networkStorage) {
        // Arrange
        Storage<String> storage = new LimitedStorageImpl<>(10);
        storage.insert("A", 5, Action.EXECUTE, EmptySource.INSTANCE);
        storageProviderRepository.setInSlot(1, storage);

        // Act
        sut.initialize(storageProviderRepository);

        // Assert
        assertThat(sut.getEnergyUsage()).isEqualTo(BASE_USAGE + USAGE_PER_DISK);
        assertThat(networkStorage.getAll()).isEmpty();
        assertThat(networkStorage.getStored()).isZero();
    }

    @Test
    void Test_initial_state(@InjectNetworkStorageChannel StorageChannel<String> networkStorage) {
        // Arrange
        Storage<String> storage = new LimitedStorageImpl<>(10);
        storage.insert("A", 5, Action.EXECUTE, EmptySource.INSTANCE);
        storageProviderRepository.setInSlot(1, storage);

        // Act
        DiskDriveState states = sut.createState();

        // Assert
        assertThat(sut.getEnergyUsage()).isEqualTo(BASE_USAGE);
        assertThat(sut.getFilterMode()).isEqualTo(FilterMode.BLOCK);
        assertThat(networkStorage.getAll()).isEmpty();
        assertThat(networkStorage.getStored()).isZero();
        assertThat(states.getStates())
                .hasSize(DiskDriveNetworkNode.DISK_COUNT)
                .allMatch(state -> state == StorageDiskState.NONE);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void Test_disk_state(boolean active) {
        // Arrange
        Storage<String> normalStorage = new LimitedStorageImpl<>(100);
        normalStorage.insert("A", 74, Action.EXECUTE, EmptySource.INSTANCE);

        Storage<String> nearCapacityStorage = new LimitedStorageImpl<>(100);
        nearCapacityStorage.insert("A", 75, Action.EXECUTE, EmptySource.INSTANCE);

        Storage<String> fullStorage = new LimitedStorageImpl<>(100);
        fullStorage.insert("A", 100, Action.EXECUTE, EmptySource.INSTANCE);

        Storage<String> unlimitedStorage = new InMemoryStorageImpl<>();

        storageProviderRepository.setInSlot(1, UUID.randomUUID());
        storageProviderRepository.setInSlot(2, unlimitedStorage);
        storageProviderRepository.setInSlot(3, normalStorage);
        storageProviderRepository.setInSlot(5, nearCapacityStorage);
        storageProviderRepository.setInSlot(7, fullStorage);

        // Act
        sut.initialize(storageProviderRepository);
        sut.setActivenessProvider(() -> active);
        if (active) {
            sut.onActiveChanged(true);
        }

        DiskDriveState state = sut.createState();

        // Assert
        assertThat(state.getState(0)).isEqualTo(StorageDiskState.NONE);
        assertThat(state.getState(1)).isEqualTo(StorageDiskState.NONE);
        assertThat(state.getState(2)).isEqualTo(active ? StorageDiskState.NORMAL : StorageDiskState.DISCONNECTED);
        assertThat(state.getState(3)).isEqualTo(active ? StorageDiskState.NORMAL : StorageDiskState.DISCONNECTED);
        assertThat(state.getState(4)).isEqualTo(StorageDiskState.NONE);
        assertThat(state.getState(5)).isEqualTo(active ? StorageDiskState.NEAR_CAPACITY : StorageDiskState.DISCONNECTED);
        assertThat(state.getState(6)).isEqualTo(StorageDiskState.NONE);
        assertThat(state.getState(7)).isEqualTo(active ? StorageDiskState.FULL : StorageDiskState.DISCONNECTED);
        assertThat(sut.getEnergyUsage()).isEqualTo(BASE_USAGE + (USAGE_PER_DISK * 4));
    }

    @Test
    void Test_setting_disk_in_slot(@InjectNetworkStorageChannel StorageChannel<String> networkStorage) {
        // Arrange
        initializeDiskDriveAndActivate();

        Storage<String> storage = new LimitedStorageImpl<>(10);
        storage.insert("A", 5, Action.EXECUTE, EmptySource.INSTANCE);
        storageProviderRepository.setInSlot(7, storage);

        // Act
        sut.onDiskChanged(7);

        // Assert
        assertThat(sut.getEnergyUsage()).isEqualTo(BASE_USAGE + USAGE_PER_DISK);
        assertThat(networkStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount<>("A", 5)
        );
    }

    @Test
    void Test_changing_disk_in_slot(@InjectNetworkStorageChannel StorageChannel<String> networkStorage) {
        // Arrange
        Storage<String> originalStorage = new LimitedStorageImpl<>(10);
        originalStorage.insert("A", 5, Action.EXECUTE, EmptySource.INSTANCE);
        storageProviderRepository.setInSlot(7, originalStorage);
        initializeDiskDriveAndActivate();

        Storage<String> replacedStorage = new LimitedStorageImpl<>(10);
        replacedStorage.insert("B", 2, Action.EXECUTE, EmptySource.INSTANCE);
        storageProviderRepository.setInSlot(7, replacedStorage);

        // Act
        Collection<ResourceAmount<String>> preDiskChanging = new HashSet<>(networkStorage.getAll());
        sut.onDiskChanged(7);
        Collection<ResourceAmount<String>> postDiskChanging = networkStorage.getAll();

        // Assert
        assertThat(sut.getEnergyUsage()).isEqualTo(BASE_USAGE + USAGE_PER_DISK);
        assertThat(preDiskChanging).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount<>("A", 5)
        );
        assertThat(postDiskChanging).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount<>("B", 2)
        );
        assertThat(networkStorage.getStored()).isEqualTo(2L);
    }

    @Test
    void Test_removing_disk_in_slot(@InjectNetworkStorageChannel StorageChannel<String> networkStorage) {
        // Arrange
        Storage<String> storage = new LimitedStorageImpl<>(10);
        storage.insert("A", 5, Action.EXECUTE, EmptySource.INSTANCE);
        storageProviderRepository.setInSlot(7, storage);
        initializeDiskDriveAndActivate();

        storageProviderRepository.removeInSlot(7);

        // Act
        Collection<ResourceAmount<String>> preDiskRemoval = new HashSet<>(networkStorage.getAll());
        sut.onDiskChanged(7);
        Collection<ResourceAmount<String>> postDiskRemoval = networkStorage.getAll();

        // Assert
        assertThat(sut.getEnergyUsage()).isEqualTo(BASE_USAGE);
        assertThat(preDiskRemoval).isNotEmpty();
        assertThat(postDiskRemoval).isEmpty();
        assertThat(networkStorage.getStored()).isZero();
    }

    @Test
    void Test_changing_disk_in_invalid_slot() {
        // Act
        sut.onDiskChanged(-1);
        sut.onDiskChanged(DiskDriveNetworkNode.DISK_COUNT);

        DiskDriveState states = sut.createState();

        // Assert
        assertThat(states.getStates())
                .hasSize(DiskDriveNetworkNode.DISK_COUNT)
                .allMatch(state -> state == StorageDiskState.NONE);
    }

    @Test
    void Test_changing_disk_when_inactive(@InjectNetworkStorageChannel StorageChannel<String> networkStorage) {
        // Arrange
        Storage<String> storage = new LimitedStorageImpl<>(100);
        storage.insert("A", 50, Action.EXECUTE, EmptySource.INSTANCE);
        storage.insert("B", 50, Action.EXECUTE, EmptySource.INSTANCE);
        storageProviderRepository.setInSlot(1, storage);
        initializeDiskDriveAndActivate();

        // Act
        Collection<ResourceAmount<String>> preInactiveness = new HashSet<>(networkStorage.getAll());
        sut.onActiveChanged(false);
        sut.setActivenessProvider(() -> false);
        sut.onDiskChanged(1);
        Collection<ResourceAmount<String>> postInactiveness = networkStorage.getAll();

        // Assert
        assertThat(preInactiveness).isNotEmpty();
        assertThat(postInactiveness).isEmpty();
        assertThat(networkStorage.getStored()).isZero();
    }

    @Test
    void Test_retrieving_resources(@InjectNetworkStorageChannel StorageChannel<String> networkStorage) {
        // Arrange
        Storage<String> storage = new LimitedStorageImpl<>(100);
        storage.insert("A", 50, Action.EXECUTE, EmptySource.INSTANCE);
        storage.insert("B", 50, Action.EXECUTE, EmptySource.INSTANCE);
        storageProviderRepository.setInSlot(1, storage);
        initializeDiskDriveAndActivate();

        // Act
        Collection<ResourceAmount<String>> resources = networkStorage.getAll();
        long stored = networkStorage.getStored();

        // Assert
        assertThat(resources).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
                new ResourceAmount<>("A", 50),
                new ResourceAmount<>("B", 50)
        );
        assertThat(stored).isEqualTo(100);
    }

    @Test
    void Test_inserting(@InjectNetworkStorageChannel StorageChannel<String> networkStorage) {
        // Arrange
        Storage<String> storage1 = new LimitedStorageImpl<>(100);
        storageProviderRepository.setInSlot(1, storage1);

        Storage<String> storage2 = new LimitedStorageImpl<>(100);
        storageProviderRepository.setInSlot(2, storage2);

        Storage<String> storage3 = new LimitedStorageImpl<>(100);
        storageProviderRepository.setInSlot(3, storage3);

        initializeDiskDriveAndActivate();

        // Act
        long inserted1 = networkStorage.insert("A", 150, Action.EXECUTE, EmptySource.INSTANCE);
        long inserted2 = networkStorage.insert("A", 10, Action.EXECUTE, EmptySource.INSTANCE);
        long inserted3 = networkStorage.insert("B", 300, Action.EXECUTE, EmptySource.INSTANCE);

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
    void Test_extracting(@InjectNetworkStorageChannel StorageChannel<String> networkStorage) {
        // Arrange
        Storage<String> storage1 = new LimitedStorageImpl<>(100);
        storage1.insert("A", 50, Action.EXECUTE, EmptySource.INSTANCE);
        storage1.insert("B", 50, Action.EXECUTE, EmptySource.INSTANCE);
        storageProviderRepository.setInSlot(1, storage1);

        Storage<String> storage2 = new LimitedStorageImpl<>(100);
        storage2.insert("A", 50, Action.EXECUTE, EmptySource.INSTANCE);
        storage2.insert("B", 50, Action.EXECUTE, EmptySource.INSTANCE);
        storageProviderRepository.setInSlot(2, storage2);

        Storage<String> storage3 = new LimitedStorageImpl<>(100);
        storage3.insert("C", 10, Action.EXECUTE, EmptySource.INSTANCE);
        storageProviderRepository.setInSlot(3, storage3);

        initializeDiskDriveAndActivate();

        // Act
        long extracted = networkStorage.extract("A", 85, Action.EXECUTE, EmptySource.INSTANCE);

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
    void Test_inserting_with_allowlist_filter(@InjectNetworkStorageChannel StorageChannel<String> networkStorage) {
        // Arrange
        sut.setFilterMode(FilterMode.ALLOW);
        sut.setFilterTemplates(Set.of("A", "B"));

        Storage<String> storage = new LimitedStorageImpl<>(100);
        storageProviderRepository.setInSlot(1, storage);
        initializeDiskDriveAndActivate();

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
        storageProviderRepository.setInSlot(1, storage);
        initializeDiskDriveAndActivate();

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
        storageProviderRepository.setInSlot(1, storage);
        initializeDiskDriveAndActivate();

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
        storageProviderRepository.setInSlot(1, storage);
        initializeDiskDriveAndActivate();

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
        storageProviderRepository.setInSlot(1, storage);
        initializeDiskDriveAndActivate();

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
        storageProviderRepository.setInSlot(1, storage);
        initializeDiskDriveAndActivate();

        storage.insert("A", 20, Action.EXECUTE, EmptySource.INSTANCE);

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
        storageProviderRepository.setInSlot(1, storage);
        initializeDiskDriveAndActivate();

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
        storage.insert("A", 20, Action.EXECUTE, EmptySource.INSTANCE);
        storageProviderRepository.setInSlot(1, storage);
        initializeDiskDriveAndActivate();

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
        storageProviderRepository.setInSlot(1, storage);
        initializeDiskDriveAndActivate();

        // Act
        Collection<ResourceAmount<String>> preInactiveness = new HashSet<>(networkStorage.getAll());
        sut.onActiveChanged(false);
        Collection<ResourceAmount<String>> postInactiveness = networkStorage.getAll();

        // Assert
        assertThat(preInactiveness).isNotEmpty();
        assertThat(postInactiveness).isEmpty();
    }

    @Test
    void Test_activeness(@InjectNetworkStorageChannel StorageChannel<String> networkStorage) {
        // Arrange
        Storage<String> storage = new LimitedStorageImpl<>(100);
        storage.insert("A", 50, Action.EXECUTE, EmptySource.INSTANCE);
        storage.insert("B", 50, Action.EXECUTE, EmptySource.INSTANCE);
        storageProviderRepository.setInSlot(1, storage);
        sut.initialize(storageProviderRepository);

        // Act
        sut.onActiveChanged(true);

        // Assert
        assertThat(networkStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
                new ResourceAmount<>("A", 50),
                new ResourceAmount<>("B", 50)
        );
    }

    @Test
    void Test_disk_state_change_listener_should_not_be_called_when_not_necessary_on_extracting(@InjectNetworkStorageChannel StorageChannel<String> networkStorage) {
        // Arrange
        Storage<String> storage = new LimitedStorageImpl<>(100);
        storage.insert("A", 76, Action.EXECUTE, EmptySource.INSTANCE);
        storageProviderRepository.setInSlot(1, storage);
        initializeDiskDriveAndActivate();

        // Act
        networkStorage.extract("A", 1, Action.EXECUTE, EmptySource.INSTANCE);

        // Assert
        verify(diskDriveListener, never()).onDiskChanged();
    }

    @Test
    void Test_disk_state_change_listener_should_not_be_called_when_not_necessary_on_inserting(@InjectNetworkStorageChannel StorageChannel<String> networkStorage) {
        // Arrange
        Storage<String> storage = new LimitedStorageImpl<>(100);
        storageProviderRepository.setInSlot(1, storage);
        initializeDiskDriveAndActivate();

        // Act
        networkStorage.insert("A", 74, Action.EXECUTE, EmptySource.INSTANCE);

        // Assert
        verify(diskDriveListener, never()).onDiskChanged();
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void Test_disk_state_change_listener_should_be_called_when_necessary_on_extracting(Action action, @InjectNetworkStorageChannel StorageChannel<String> networkStorage) {
        // Arrange
        Storage<String> storage = new LimitedStorageImpl<>(100);
        storage.insert("A", 75, Action.EXECUTE, EmptySource.INSTANCE);
        storageProviderRepository.setInSlot(1, storage);
        initializeDiskDriveAndActivate();

        // Act
        networkStorage.extract("A", 1, action, EmptySource.INSTANCE);
        networkStorage.extract("A", 1, action, EmptySource.INSTANCE);

        // Assert
        VerificationMode expectedTimes = action == Action.EXECUTE ? times(1) : never();
        verify(diskDriveListener, expectedTimes).onDiskChanged();
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void Test_disk_state_change_listener_should_be_called_when_necessary_on_inserting(Action action, @InjectNetworkStorageChannel StorageChannel<String> networkStorage) {
        // Arrange
        Storage<String> storage = new LimitedStorageImpl<>(100);
        storage.insert("A", 74, Action.EXECUTE, EmptySource.INSTANCE);
        storageProviderRepository.setInSlot(1, storage);
        initializeDiskDriveAndActivate();

        // Act
        networkStorage.insert("A", 1, action, EmptySource.INSTANCE);
        networkStorage.insert("A", 1, action, EmptySource.INSTANCE);

        // Assert
        VerificationMode expectedTimes = action == Action.EXECUTE ? times(1) : never();
        verify(diskDriveListener, expectedTimes).onDiskChanged();
    }

    @Test
    void Test_tracking_changes(@InjectNetworkStorageChannel StorageChannel<String> networkStorage) {
        // Arrange
        Storage<String> storage = new TrackedStorageImpl<>(new LimitedStorageImpl<>(100), () -> 0L);
        storageProviderRepository.setInSlot(1, storage);
        initializeDiskDriveAndActivate();

        // Act
        long inserted = networkStorage.insert("A", 10, Action.EXECUTE, CustomSource1.INSTANCE);

        // Assert
        assertThat(inserted).isEqualTo(10);
        assertThat(networkStorage.findTrackedResourceBySourceType("A", CustomSource1.class)).isNotEmpty();
    }

    // TODO: remove all these custom sources.
    private static class CustomSource1 implements Source {
        private static final Source INSTANCE = new CustomSource1();

        @Override
        public String getName() {
            return "Custom1";
        }
    }

    @Nested
    class PriorityTest {
        @AddDiskDrive
        DiskDriveNetworkNode otherDiskDrive;

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        void Test_setting_priority(boolean oneHasPriority, @InjectNetworkStorageChannel StorageChannel<String> networkStorage) {
            // Arrange
            Storage<String> storage1 = new LimitedStorageImpl<>(100);
            storageProviderRepository.setInSlot(1, storage1);
            initializeDiskDriveAndActivate();

            Storage<String> storage2 = new LimitedStorageImpl<>(100);
            FakeStorageProviderRepository storageProviderManager2 = new FakeStorageProviderRepository();
            storageProviderManager2.setInSlot(1, storage2);
            otherDiskDrive.setDiskProvider(storageProviderManager2);
            otherDiskDrive.initialize(storageProviderManager2);
            otherDiskDrive.onActiveChanged(true);

            if (oneHasPriority) {
                sut.setPriority(5);
                otherDiskDrive.setPriority(2);
            } else {
                sut.setPriority(2);
                otherDiskDrive.setPriority(5);
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

    private void initializeDiskDriveAndActivate() {
        sut.initialize(storageProviderRepository);
        sut.onActiveChanged(true);
    }
}
