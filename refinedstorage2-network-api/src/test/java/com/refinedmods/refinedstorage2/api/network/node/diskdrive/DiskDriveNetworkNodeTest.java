package com.refinedmods.refinedstorage2.api.network.node.diskdrive;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.network.Network;
import com.refinedmods.refinedstorage2.api.network.NetworkUtil;
import com.refinedmods.refinedstorage2.api.network.test.StorageChannelTypes;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.storage.AccessMode;
import com.refinedmods.refinedstorage2.api.storage.CappedStorage;
import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;
import com.refinedmods.refinedstorage2.test.Rs2Test;

import java.util.Collection;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.verification.VerificationMode;

import static com.refinedmods.refinedstorage2.api.network.NetworkUtil.STORAGE_CHANNEL_TYPE_REGISTRY;
import static com.refinedmods.refinedstorage2.api.network.NetworkUtil.create;
import static com.refinedmods.refinedstorage2.api.network.NetworkUtil.fakeStorageChannelOf;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@Rs2Test
class DiskDriveNetworkNodeTest {
    private static final long BASE_USAGE = 10;
    private static final long USAGE_PER_DISK = 3;

    private Network network;
    private DiskDriveNetworkNode sut;
    private FakeStorageProviderRepository storageProviderRepository;
    private DiskDriveListener diskDriveListener;

    // TODO: Test with additional storage channel types

    @BeforeEach
    void setUp() {
        diskDriveListener = mock(DiskDriveListener.class);
        storageProviderRepository = new FakeStorageProviderRepository();

        network = create();

        sut = createDiskDrive(network, storageProviderRepository, diskDriveListener);
    }

    private DiskDriveNetworkNode createDiskDrive(Network network, FakeStorageProviderRepository storageDiskProviderManager, DiskDriveListener diskDriveListener) {
        DiskDriveNetworkNode diskDrive = new DiskDriveNetworkNode(BASE_USAGE, USAGE_PER_DISK, STORAGE_CHANNEL_TYPE_REGISTRY);
        diskDrive.setNetwork(network);
        diskDrive.setDiskProvider(storageDiskProviderManager);
        diskDrive.setListener(diskDriveListener);
        network.addContainer(() -> diskDrive);
        return diskDrive;
    }

    private <T> Storage<T> storageOf(DiskDriveNetworkNode diskDrive, StorageChannelType<T> type) {
        return diskDrive.getStorageForChannel(type).get();
    }

    private Storage<String> storageOf(DiskDriveNetworkNode diskDrive) {
        return storageOf(diskDrive, StorageChannelTypes.FAKE);
    }

    @Test
    void Test_initial_state() {
        // Arrange
        Storage<String> storage = new CappedStorage<>(10);
        storage.insert("A", 5, Action.EXECUTE);
        storageProviderRepository.setInSlot(1, storage);

        // Act
        DiskDriveState states = sut.createState();

        // Assert
        assertThat(sut.getEnergyUsage()).isEqualTo(BASE_USAGE);
        assertThat(storageOf(sut).getAll()).isEmpty();
        assertThat(storageOf(sut).getStored()).isZero();
        assertThat(states.getStates())
                .hasSize(DiskDriveNetworkNode.DISK_COUNT)
                .allMatch(state -> state == StorageDiskState.NONE);
        assertThat(fakeStorageChannelOf(network).getAll()).isEmpty();
    }

    @Test
    void Test_initialization() {
        // Arrange
        Storage<String> storage = new CappedStorage<>(10);
        storage.insert("A", 5, Action.EXECUTE);
        storageProviderRepository.setInSlot(1, storage);

        // Act
        sut.initialize(storageProviderRepository);

        // Assert
        assertThat(sut.getEnergyUsage()).isEqualTo(BASE_USAGE + USAGE_PER_DISK);
        assertThat(storageOf(sut).getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount<>("A", 5)
        );
        assertThat(fakeStorageChannelOf(network).getAll()).isEmpty();
        assertThat(storageOf(sut).getStored()).isEqualTo(5L);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void Test_disk_state(boolean inactive) {
        // Arrange
        Storage<String> normalStorage = new CappedStorage<>(100);
        normalStorage.insert("A", 74, Action.EXECUTE);

        Storage<String> nearCapacityStorage = new CappedStorage<>(100);
        nearCapacityStorage.insert("A", 75, Action.EXECUTE);

        Storage<String> fullStorage = new CappedStorage<>(100);
        fullStorage.insert("A", 100, Action.EXECUTE);

        storageProviderRepository.setInSlot(1, UUID.randomUUID());
        storageProviderRepository.setInSlot(3, normalStorage);
        storageProviderRepository.setInSlot(5, nearCapacityStorage);
        storageProviderRepository.setInSlot(7, fullStorage);

        if (inactive) {
            NetworkUtil.makeNodeInactive(sut);
        }

        // Act
        sut.initialize(storageProviderRepository);

        DiskDriveState state = sut.createState();

        // Assert
        assertThat(state.getState(0)).isEqualTo(StorageDiskState.NONE);
        assertThat(state.getState(1)).isEqualTo(StorageDiskState.NONE);
        assertThat(state.getState(2)).isEqualTo(StorageDiskState.NONE);
        assertThat(state.getState(3)).isEqualTo(inactive ? StorageDiskState.DISCONNECTED : StorageDiskState.NORMAL);
        assertThat(state.getState(4)).isEqualTo(StorageDiskState.NONE);
        assertThat(state.getState(5)).isEqualTo(inactive ? StorageDiskState.DISCONNECTED : StorageDiskState.NEAR_CAPACITY);
        assertThat(state.getState(6)).isEqualTo(StorageDiskState.NONE);
        assertThat(state.getState(7)).isEqualTo(inactive ? StorageDiskState.DISCONNECTED : StorageDiskState.FULL);
        assertThat(sut.getEnergyUsage()).isEqualTo(BASE_USAGE + (USAGE_PER_DISK * 3));
    }

    @Test
    void Test_setting_disk_in_slot() {
        // Arrange
        sut.initialize(storageProviderRepository);

        Storage<String> storage = new CappedStorage<>(10);
        storage.insert("A", 5, Action.EXECUTE);
        storageProviderRepository.setInSlot(7, storage);

        // Act
        sut.onDiskChanged(7);

        // Assert
        assertThat(sut.getEnergyUsage()).isEqualTo(BASE_USAGE + USAGE_PER_DISK);
        assertThat(storageOf(sut).getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount<>("A", 5)
        );
        assertThat(fakeStorageChannelOf(network).getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount<>("A", 5)
        );
        assertThat(storageOf(sut).getStored()).isEqualTo(5L);
    }

    @Test
    void Test_changing_disk_in_slot() {
        // Arrange
        Storage<String> storage1 = new CappedStorage<>(10);
        storage1.insert("A", 5, Action.EXECUTE);
        storageProviderRepository.setInSlot(7, storage1);
        sut.initialize(storageProviderRepository);

        // Act
        Storage<String> storage2 = new CappedStorage<>(10);
        storage2.insert("B", 2, Action.EXECUTE);
        storageProviderRepository.setInSlot(7, storage2);
        sut.onDiskChanged(7);

        // Assert
        assertThat(sut.getEnergyUsage()).isEqualTo(BASE_USAGE + USAGE_PER_DISK);
        assertThat(storageOf(sut).getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount<>("B", 2)
        );
        assertThat(fakeStorageChannelOf(network).getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount<>("B", 2)
        );
        assertThat(storageOf(sut).getStored()).isEqualTo(2L);
    }

    @Test
    void Test_removing_disk_in_slot() {
        // Arrange
        sut.initialize(storageProviderRepository);

        Storage<String> storage = new CappedStorage<>(10);
        storage.insert("A", 5, Action.EXECUTE);
        storageProviderRepository.setInSlot(7, storage);

        sut.onDiskChanged(7);

        // Act
        storageProviderRepository.removeInSlot(7);
        sut.onDiskChanged(7);

        // Assert
        assertThat(sut.getEnergyUsage()).isEqualTo(BASE_USAGE);
        assertThat(storageOf(sut).getAll()).isEmpty();
        assertThat(fakeStorageChannelOf(network).getAll()).isEmpty();
        assertThat(storageOf(sut).getStored()).isZero();
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
    void Test_retrieving_resources() {
        // Arrange
        sut.initialize(storageProviderRepository);

        Storage<String> storage = new CappedStorage<>(100);
        storage.insert("A", 50, Action.EXECUTE);
        storage.insert("B", 50, Action.EXECUTE);
        storageProviderRepository.setInSlot(1, storage);

        sut.onDiskChanged(1);

        // Act
        Collection<ResourceAmount<String>> resources = storageOf(sut).getAll();
        Collection<ResourceAmount<String>> resourcesInNetwork = fakeStorageChannelOf(network).getAll();
        long storedInNetwork = fakeStorageChannelOf(network).getStored();

        // Assert
        assertThat(resources).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
                new ResourceAmount<>("A", 50),
                new ResourceAmount<>("B", 50)
        );
        assertThat(resourcesInNetwork).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
                new ResourceAmount<>("A", 50),
                new ResourceAmount<>("B", 50)
        );
        assertThat(storageOf(sut).getStored()).isEqualTo(100);
        assertThat(storedInNetwork).isEqualTo(100);
    }

    @Test
    void Test_retrieving_resources_when_inactive() {
        // Arrange
        sut.initialize(storageProviderRepository);

        Storage<String> storage = new CappedStorage<>(100);
        storage.insert("A", 50, Action.EXECUTE);
        storage.insert("B", 50, Action.EXECUTE);
        storageProviderRepository.setInSlot(1, storage);

        NetworkUtil.makeNodeInactive(sut);
        sut.onDiskChanged(1);

        // Act
        Collection<ResourceAmount<String>> resources = storageOf(sut).getAll();
        Collection<ResourceAmount<String>> resourcesInNetwork = fakeStorageChannelOf(network).getAll();
        long storedInNetwork = fakeStorageChannelOf(network).getStored();

        // Assert
        assertThat(resources).isEmpty();
        assertThat(resourcesInNetwork).isEmpty();
        assertThat(storageOf(sut).getStored()).isEqualTo(100L);
        assertThat(storedInNetwork).isEqualTo(100L);
    }

    @Test
    void Test_inserting() {
        // Arrange
        Storage<String> storage1 = new CappedStorage<>(100);
        storageProviderRepository.setInSlot(1, storage1);

        Storage<String> storage2 = new CappedStorage<>(100);
        storageProviderRepository.setInSlot(2, storage2);

        Storage<String> storage3 = new CappedStorage<>(100);
        storageProviderRepository.setInSlot(3, storage3);

        sut.initialize(storageProviderRepository);

        // Act
        long remainder1 = fakeStorageChannelOf(network).insert("A", 150, Action.EXECUTE);
        long remainder2 = fakeStorageChannelOf(network).insert("A", 10, Action.EXECUTE);
        long remainder3 = fakeStorageChannelOf(network).insert("B", 300, Action.EXECUTE);

        // Assert
        assertThat(remainder1).isZero();
        assertThat(remainder2).isZero();
        assertThat(remainder3).isEqualTo(160);

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
        assertThat(fakeStorageChannelOf(network).getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
                new ResourceAmount<>("B", 140),
                new ResourceAmount<>("A", 160)
        );

        assertThat(storageOf(sut).getStored()).isEqualTo(150 + 10 + 140);
    }

    @Test
    void Test_extracting() {
        // Arrange
        Storage<String> storage1 = new CappedStorage<>(100);
        storage1.insert("A", 50, Action.EXECUTE);
        storage1.insert("B", 50, Action.EXECUTE);
        storageProviderRepository.setInSlot(1, storage1);

        Storage<String> storage2 = new CappedStorage<>(100);
        storage2.insert("A", 50, Action.EXECUTE);
        storage2.insert("B", 50, Action.EXECUTE);
        storageProviderRepository.setInSlot(2, storage2);

        Storage<String> storage3 = new CappedStorage<>(100);
        storage3.insert("C", 10, Action.EXECUTE);
        storageProviderRepository.setInSlot(3, storage3);

        sut.initialize(storageProviderRepository);
        fakeStorageChannelOf(network).invalidate();

        // Act
        long extracted = fakeStorageChannelOf(network).extract("A", 85, Action.EXECUTE);

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
        assertThat(fakeStorageChannelOf(network).getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
                new ResourceAmount<>("B", 100),
                new ResourceAmount<>("A", 15),
                new ResourceAmount<>("C", 10)
        );

        assertThat(storageOf(sut).getStored()).isEqualTo(125);
    }

    /* TODO @Test
    void Test_inserting_with_filter() {
        // Arrange
        diskDrive.setFilterMode(FilterMode.BLOCK);
        diskDrive.setExactMode(false);
        diskDrive.setFilterTemplates(Arrays.asList("B", new Rs2ItemStack(ItemStubs.STONE)));

        Storage<String> bulkStorage = new BulkStorageImpl<>(100);
        storageDiskProviderManager.setDiskInSlot(1, bulkStorage);

        diskDrive.initialize(storageDiskProviderManager);

        // Act
        Rs2ItemStack glassWithTag = new Rs2ItemStack(ItemStubs.GLASS, 12, "myTag");

        Optional<Rs2ItemStack> remainder1 = storageOf(diskDrive).insert("B", 12, Action.EXECUTE);
        Optional<Rs2ItemStack> remainder2 = storageOf(diskDrive).insert(glassWithTag, 12, Action.EXECUTE);
        Optional<Rs2ItemStack> remainder3 = storageOf(diskDrive).insert("A", 10, Action.EXECUTE);

        // Assert
        assertThat(remainder1).isPresent();
        assertItemStack(remainder1.get(), new Rs2ItemStack(ItemStubs.GLASS, 12));

        assertThat(remainder2).isPresent();
        assertItemStack(remainder2.get(), glassWithTag);

        assertThat(remainder3).isEmpty();
    }*/

    @ParameterizedTest
    @EnumSource(AccessMode.class)
    void Test_inserting_with_access_mode(AccessMode accessMode) {
        // Arrange
        sut.setAccessMode(accessMode);

        Storage<String> storage = new CappedStorage<>(100);
        storageProviderRepository.setInSlot(1, storage);

        sut.initialize(storageProviderRepository);

        // Act
        long remainder = storageOf(sut).insert("A", 5, Action.EXECUTE);

        // Assert
        switch (accessMode) {
            case INSERT_EXTRACT, INSERT -> assertThat(remainder).isZero();
            case EXTRACT -> assertThat(remainder).isEqualTo(5);
        }
    }

    @ParameterizedTest
    @EnumSource(AccessMode.class)
    void Test_extracting_with_access_mode(AccessMode accessMode) {
        // Arrange
        sut.setAccessMode(accessMode);

        Storage<String> storage = new CappedStorage<>(100);
        storageProviderRepository.setInSlot(1, storage);

        sut.initialize(storageProviderRepository);

        storage.insert("A", 20, Action.EXECUTE);

        // Act
        long extracted = storageOf(sut).extract("A", 5, Action.EXECUTE);

        // Assert
        switch (accessMode) {
            case INSERT_EXTRACT, EXTRACT -> assertThat(extracted).isEqualTo(5);
            case INSERT -> assertThat(extracted).isZero();
        }
    }

    @Test
    void Test_inserting_when_inactive() {
        // Arrange
        NetworkUtil.makeNodeInactive(sut);
        sut.initialize(storageProviderRepository);

        Storage<String> storage = new CappedStorage<>(100);
        storageProviderRepository.setInSlot(1, storage);

        // Act
        long remainder = storageOf(sut).insert("A", 5, Action.EXECUTE);

        // Assert
        assertThat(remainder).isEqualTo(5);
    }

    @Test
    void Test_extracting_when_inactive() {
        // Arrange
        NetworkUtil.makeNodeInactive(sut);

        Storage<String> storage = new CappedStorage<>(100);
        storageProviderRepository.setInSlot(1, storage);

        sut.initialize(storageProviderRepository);

        storage.insert("A", 20, Action.EXECUTE);

        // Act
        long extracted = storageOf(sut).extract("A", 5, Action.EXECUTE);

        // Assert
        assertThat(extracted).isZero();
    }

    @Test
    void Test_disk_state_change_listener_should_not_be_called_when_not_necessary_on_extracting() {
        // Arrange
        Storage<String> storage = new CappedStorage<>(100);
        storage.insert("A", 76, Action.EXECUTE);
        storageProviderRepository.setInSlot(1, storage);
        sut.initialize(storageProviderRepository);

        // Act
        fakeStorageChannelOf(network).extract("A", 1, Action.EXECUTE);

        // Assert
        verify(diskDriveListener, never()).onDiskChanged();
    }

    @Test
    void Test_disk_state_change_listener_should_not_be_called_when_not_necessary_on_inserting() {
        // Arrange
        Storage<String> storage = new CappedStorage<>(100);
        storageProviderRepository.setInSlot(1, storage);
        sut.initialize(storageProviderRepository);

        // Act
        fakeStorageChannelOf(network).insert("A", 74, Action.EXECUTE);

        // Assert
        verify(diskDriveListener, never()).onDiskChanged();
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void Test_disk_state_change_listener_should_be_called_when_necessary_on_extracting(Action action) {
        // Arrange
        Storage<String> storage = new CappedStorage<>(100);
        storage.insert("A", 75, Action.EXECUTE);
        storageProviderRepository.setInSlot(1, storage);
        sut.initialize(storageProviderRepository);

        // Act
        fakeStorageChannelOf(network).extract("A", 1, action);
        fakeStorageChannelOf(network).extract("A", 1, action);

        // Assert
        VerificationMode expectedTimes = action == Action.EXECUTE ? times(1) : never();

        verify(diskDriveListener, expectedTimes).onDiskChanged();
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void Test_disk_state_change_listener_should_be_called_when_necessary_on_inserting(Action action) {
        // Arrange
        Storage<String> storage = new CappedStorage<>(100);
        storageProviderRepository.setInSlot(1, storage);
        sut.initialize(storageProviderRepository);
        storageOf(sut).insert("A", 74, Action.EXECUTE);

        // Act
        fakeStorageChannelOf(network).insert("A", 1, action);
        fakeStorageChannelOf(network).insert("A", 1, action);

        // Assert
        VerificationMode expectedTimes = action == Action.EXECUTE ? times(1) : never();

        verify(diskDriveListener, expectedTimes).onDiskChanged();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void Test_setting_priority(boolean oneHasPriority) {
        // Arrange
        Storage<String> storage1 = new CappedStorage<>(100);
        storageProviderRepository.setInSlot(1, storage1);
        sut.initialize(storageProviderRepository);

        Storage<String> storage2 = new CappedStorage<>(100);
        FakeStorageProviderRepository storageProviderManager2 = new FakeStorageProviderRepository();
        storageProviderManager2.setInSlot(1, storage2);
        DiskDriveNetworkNode diskDrive2 = createDiskDrive(network, storageProviderManager2, mock(DiskDriveListener.class));
        diskDrive2.initialize(storageProviderManager2);

        if (oneHasPriority) {
            sut.setPriority(5);
            diskDrive2.setPriority(2);
        } else {
            sut.setPriority(2);
            diskDrive2.setPriority(5);
        }

        // Act
        fakeStorageChannelOf(network).insert("A", 1, Action.EXECUTE);

        // Assert
        if (oneHasPriority) {
            assertThat(storageOf(sut).getAll()).isNotEmpty();
            assertThat(storageOf(diskDrive2).getAll()).isEmpty();
        } else {
            assertThat(storageOf(sut).getAll()).isEmpty();
            assertThat(storageOf(diskDrive2).getAll()).isNotEmpty();
        }
    }
}
