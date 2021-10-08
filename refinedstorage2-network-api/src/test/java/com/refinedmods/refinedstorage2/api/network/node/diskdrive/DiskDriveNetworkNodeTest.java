package com.refinedmods.refinedstorage2.api.network.node.diskdrive;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.core.filter.FilterMode;
import com.refinedmods.refinedstorage2.api.network.Network;
import com.refinedmods.refinedstorage2.api.network.node.container.FakeNetworkNodeContainer;
import com.refinedmods.refinedstorage2.api.resource.Rs2Stack;
import com.refinedmods.refinedstorage2.api.resource.item.Rs2ItemStack;
import com.refinedmods.refinedstorage2.api.resource.test.ItemStubs;
import com.refinedmods.refinedstorage2.api.storage.AccessMode;
import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.bulk.BulkStorage;
import com.refinedmods.refinedstorage2.api.storage.bulk.BulkStorageImpl;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelTypes;
import com.refinedmods.refinedstorage2.test.Rs2Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.verification.VerificationMode;

import static com.refinedmods.refinedstorage2.api.network.NetworkUtil.STORAGE_CHANNEL_TYPE_REGISTRY;
import static com.refinedmods.refinedstorage2.api.network.NetworkUtil.createWithInfiniteEnergyStorage;
import static com.refinedmods.refinedstorage2.api.network.NetworkUtil.itemStorageChannelOf;
import static com.refinedmods.refinedstorage2.api.resource.test.ItemStackAssertions.assertItemStack;
import static com.refinedmods.refinedstorage2.api.resource.test.ItemStackAssertions.assertItemStackListContents;
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
    private DiskDriveNetworkNode diskDrive;
    private FakeStorageProviderManager storageDiskProviderManager;
    private DiskDriveListener diskDriveListener;

    // TODO: Test with additional storage channel types

    @BeforeEach
    void setUp() {
        diskDriveListener = mock(DiskDriveListener.class);
        storageDiskProviderManager = new FakeStorageProviderManager();

        network = createWithInfiniteEnergyStorage();

        diskDrive = createDiskDriveContainer(network, storageDiskProviderManager, diskDriveListener).getNode();
    }

    private FakeNetworkNodeContainer<DiskDriveNetworkNode> createDiskDriveContainer(Network network, FakeStorageProviderManager storageDiskProviderManager, DiskDriveListener diskDriveListener) {
        DiskDriveNetworkNode diskDrive = new DiskDriveNetworkNode(storageDiskProviderManager, BASE_USAGE, USAGE_PER_DISK, diskDriveListener, STORAGE_CHANNEL_TYPE_REGISTRY);
        diskDrive.setNetwork(network);

        FakeNetworkNodeContainer<DiskDriveNetworkNode> container = new FakeNetworkNodeContainer<>(diskDrive);

        network.addContainer(container);

        return container;
    }

    private <T extends Rs2Stack> Storage<T> storageOf(DiskDriveNetworkNode diskDrive, StorageChannelType<T> type) {
        return diskDrive.getStorageForChannel(type).get();
    }

    private Storage<Rs2ItemStack> storageOf(DiskDriveNetworkNode diskDrive) {
        return storageOf(diskDrive, StorageChannelTypes.ITEM);
    }

    @Test
    void Test_initial_state() {
        // Arrange
        BulkStorage<Rs2ItemStack> disk = BulkStorageImpl.createItemStorageDisk(10);
        disk.insert(new Rs2ItemStack(ItemStubs.DIRT), 5, Action.EXECUTE);
        storageDiskProviderManager.setDiskInSlot(1, disk);

        // Act
        diskDrive.setActive(true);
        DiskDriveState states = diskDrive.createState();

        // Assert
        assertThat(diskDrive.getEnergyUsage()).isEqualTo(BASE_USAGE);
        assertThat(storageOf(diskDrive).getAll()).isEmpty();
        assertThat(storageOf(diskDrive).getStored()).isZero();
        assertThat(states.getStates())
                .hasSize(DiskDriveNetworkNode.DISK_COUNT)
                .allMatch(state -> state == StorageDiskState.NONE);
        assertItemStackListContents(itemStorageChannelOf(network).getAll());
    }

    @Test
    void Test_initialization() {
        // Arrange
        BulkStorage<Rs2ItemStack> disk = BulkStorageImpl.createItemStorageDisk(10);
        disk.insert(new Rs2ItemStack(ItemStubs.DIRT), 5, Action.EXECUTE);
        storageDiskProviderManager.setDiskInSlot(1, disk);

        // Act
        diskDrive.initialize(storageDiskProviderManager);

        // Assert
        assertThat(diskDrive.getEnergyUsage()).isEqualTo(BASE_USAGE + USAGE_PER_DISK);
        assertItemStackListContents(storageOf(diskDrive).getAll(), new Rs2ItemStack(ItemStubs.DIRT, 5));
        assertItemStackListContents(itemStorageChannelOf(network).getAll());
        assertThat(storageOf(diskDrive).getStored()).isEqualTo(5L);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void Test_disk_state(boolean inactive) {
        // Arrange
        BulkStorage<Rs2ItemStack> normalDisk = BulkStorageImpl.createItemStorageDisk(100);
        normalDisk.insert(new Rs2ItemStack(ItemStubs.DIRT), 74, Action.EXECUTE);

        BulkStorage<Rs2ItemStack> nearCapacityDisk = BulkStorageImpl.createItemStorageDisk(100);
        nearCapacityDisk.insert(new Rs2ItemStack(ItemStubs.DIRT), 75, Action.EXECUTE);

        BulkStorage<Rs2ItemStack> fullDisk = BulkStorageImpl.createItemStorageDisk(100);
        fullDisk.insert(new Rs2ItemStack(ItemStubs.DIRT), 100, Action.EXECUTE);

        storageDiskProviderManager.setDiskInSlot(1, UUID.randomUUID());
        storageDiskProviderManager.setDiskInSlot(3, normalDisk);
        storageDiskProviderManager.setDiskInSlot(5, nearCapacityDisk);
        storageDiskProviderManager.setDiskInSlot(7, fullDisk);

        if (inactive) {
            diskDrive.setActive(false);
        }

        // Act
        diskDrive.initialize(storageDiskProviderManager);

        DiskDriveState state = diskDrive.createState();

        // Assert
        assertThat(state.getState(0)).isEqualTo(StorageDiskState.NONE);
        assertThat(state.getState(1)).isEqualTo(StorageDiskState.NONE);
        assertThat(state.getState(2)).isEqualTo(StorageDiskState.NONE);
        assertThat(state.getState(3)).isEqualTo(inactive ? StorageDiskState.DISCONNECTED : StorageDiskState.NORMAL);
        assertThat(state.getState(4)).isEqualTo(StorageDiskState.NONE);
        assertThat(state.getState(5)).isEqualTo(inactive ? StorageDiskState.DISCONNECTED : StorageDiskState.NEAR_CAPACITY);
        assertThat(state.getState(6)).isEqualTo(StorageDiskState.NONE);
        assertThat(state.getState(7)).isEqualTo(inactive ? StorageDiskState.DISCONNECTED : StorageDiskState.FULL);
        assertThat(diskDrive.getEnergyUsage()).isEqualTo(BASE_USAGE + (USAGE_PER_DISK * 3));
    }

    @Test
    void Test_setting_disk_in_slot() {
        // Arrange
        diskDrive.initialize(storageDiskProviderManager);

        BulkStorage<Rs2ItemStack> disk = BulkStorageImpl.createItemStorageDisk(10);
        disk.insert(new Rs2ItemStack(ItemStubs.DIRT), 5, Action.EXECUTE);
        storageDiskProviderManager.setDiskInSlot(7, disk);

        // Act
        diskDrive.onDiskChanged(7);

        // Assert
        assertThat(diskDrive.getEnergyUsage()).isEqualTo(BASE_USAGE + USAGE_PER_DISK);
        assertItemStackListContents(storageOf(diskDrive).getAll(), new Rs2ItemStack(ItemStubs.DIRT, 5));
        assertItemStackListContents(itemStorageChannelOf(network).getAll(), new Rs2ItemStack(ItemStubs.DIRT, 5));
        assertThat(storageOf(diskDrive).getStored()).isEqualTo(5L);
    }

    @Test
    void Test_changing_disk_in_slot() {
        // Arrange
        BulkStorage<Rs2ItemStack> disk = BulkStorageImpl.createItemStorageDisk(10);
        disk.insert(new Rs2ItemStack(ItemStubs.DIRT), 5, Action.EXECUTE);
        storageDiskProviderManager.setDiskInSlot(7, disk);
        diskDrive.initialize(storageDiskProviderManager);

        // Act
        BulkStorage<Rs2ItemStack> disk2 = BulkStorageImpl.createItemStorageDisk(10);
        disk2.insert(new Rs2ItemStack(ItemStubs.GLASS), 2, Action.EXECUTE);
        storageDiskProviderManager.setDiskInSlot(7, disk2);
        diskDrive.onDiskChanged(7);

        // Assert
        assertThat(diskDrive.getEnergyUsage()).isEqualTo(BASE_USAGE + USAGE_PER_DISK);
        assertItemStackListContents(storageOf(diskDrive).getAll(), new Rs2ItemStack(ItemStubs.GLASS, 2));
        assertItemStackListContents(itemStorageChannelOf(network).getAll(), new Rs2ItemStack(ItemStubs.GLASS, 2));
        assertThat(storageOf(diskDrive).getStored()).isEqualTo(2L);
    }

    @Test
    void Test_removing_disk_in_slot() {
        // Arrange
        diskDrive.initialize(storageDiskProviderManager);

        BulkStorage<Rs2ItemStack> disk = BulkStorageImpl.createItemStorageDisk(10);
        disk.insert(new Rs2ItemStack(ItemStubs.DIRT), 5, Action.EXECUTE);
        storageDiskProviderManager.setDiskInSlot(7, disk);

        diskDrive.onDiskChanged(7);

        // Act
        storageDiskProviderManager.removeDiskInSlot(7);
        diskDrive.onDiskChanged(7);

        // Assert
        assertThat(diskDrive.getEnergyUsage()).isEqualTo(BASE_USAGE);
        assertItemStackListContents(storageOf(diskDrive).getAll());
        assertItemStackListContents(itemStorageChannelOf(network).getAll());
        assertThat(storageOf(diskDrive).getStored()).isZero();
    }

    @Test
    void Test_changing_disk_in_invalid_slot() {
        // Act
        diskDrive.onDiskChanged(-1);
        diskDrive.onDiskChanged(DiskDriveNetworkNode.DISK_COUNT);

        DiskDriveState states = diskDrive.createState();

        // Assert
        assertThat(states.getStates())
                .hasSize(DiskDriveNetworkNode.DISK_COUNT)
                .allMatch(state -> state == StorageDiskState.NONE);
    }

    @Test
    void Test_retrieving_stacks() {
        // Arrange
        diskDrive.initialize(storageDiskProviderManager);

        BulkStorage<Rs2ItemStack> bulkStorage1 = BulkStorageImpl.createItemStorageDisk(100);
        bulkStorage1.insert(new Rs2ItemStack(ItemStubs.DIRT), 50, Action.EXECUTE);
        bulkStorage1.insert(new Rs2ItemStack(ItemStubs.GLASS), 50, Action.EXECUTE);
        storageDiskProviderManager.setDiskInSlot(1, bulkStorage1);

        diskDrive.onDiskChanged(1);

        // Act
        Collection<Rs2ItemStack> stacks = storageOf(diskDrive).getAll();
        Collection<Rs2ItemStack> stacksInNetwork = itemStorageChannelOf(network).getAll();
        long storedInNetwork = itemStorageChannelOf(network).getStored();

        // Assert
        assertItemStackListContents(stacks, new Rs2ItemStack(ItemStubs.DIRT, 50), new Rs2ItemStack(ItemStubs.GLASS, 50));
        assertItemStackListContents(stacksInNetwork, new Rs2ItemStack(ItemStubs.DIRT, 50), new Rs2ItemStack(ItemStubs.GLASS, 50));
        assertThat(storageOf(diskDrive).getStored()).isEqualTo(100);
        assertThat(storedInNetwork).isEqualTo(100);
    }

    @Test
    void Test_retrieving_stacks_when_inactive() {
        // Arrange
        diskDrive.initialize(storageDiskProviderManager);

        BulkStorage<Rs2ItemStack> bulkStorage1 = BulkStorageImpl.createItemStorageDisk(100);
        bulkStorage1.insert(new Rs2ItemStack(ItemStubs.DIRT), 50, Action.EXECUTE);
        bulkStorage1.insert(new Rs2ItemStack(ItemStubs.GLASS), 50, Action.EXECUTE);
        storageDiskProviderManager.setDiskInSlot(1, bulkStorage1);

        diskDrive.setActive(false);
        diskDrive.onDiskChanged(1);

        // Act
        Collection<Rs2ItemStack> stacks = storageOf(diskDrive).getAll();
        Collection<Rs2ItemStack> stacksInNetwork = itemStorageChannelOf(network).getAll();
        long storedInNetwork = itemStorageChannelOf(network).getStored();

        // Assert
        assertItemStackListContents(stacks);
        assertItemStackListContents(stacksInNetwork);
        assertThat(storageOf(diskDrive).getStored()).isEqualTo(100L);
        assertThat(storedInNetwork).isEqualTo(100L);
    }

    @Test
    void Test_inserting() {
        // Arrange
        BulkStorage<Rs2ItemStack> bulkStorage1 = BulkStorageImpl.createItemStorageDisk(100);
        storageDiskProviderManager.setDiskInSlot(1, bulkStorage1);

        BulkStorage<Rs2ItemStack> bulkStorage2 = BulkStorageImpl.createItemStorageDisk(100);
        storageDiskProviderManager.setDiskInSlot(2, bulkStorage2);

        BulkStorage<Rs2ItemStack> bulkStorage3 = BulkStorageImpl.createItemStorageDisk(100);
        storageDiskProviderManager.setDiskInSlot(3, bulkStorage3);

        diskDrive.initialize(storageDiskProviderManager);

        // Act
        Optional<Rs2ItemStack> remainder1 = itemStorageChannelOf(network).insert(new Rs2ItemStack(ItemStubs.DIRT), 150, Action.EXECUTE);
        Optional<Rs2ItemStack> remainder2 = itemStorageChannelOf(network).insert(new Rs2ItemStack(ItemStubs.DIRT), 10, Action.EXECUTE);
        Optional<Rs2ItemStack> remainder3 = itemStorageChannelOf(network).insert(new Rs2ItemStack(ItemStubs.GLASS), 300, Action.EXECUTE);

        // Assert
        assertThat(remainder1).isEmpty();
        assertThat(remainder2).isEmpty();
        assertThat(remainder3).isPresent();
        assertItemStack(remainder3.get(), new Rs2ItemStack(ItemStubs.GLASS, 160));

        assertItemStackListContents(bulkStorage1.getAll(), new Rs2ItemStack(ItemStubs.DIRT, 100));
        assertItemStackListContents(bulkStorage2.getAll(), new Rs2ItemStack(ItemStubs.DIRT, 60), new Rs2ItemStack(ItemStubs.GLASS, 40));
        assertItemStackListContents(bulkStorage3.getAll(), new Rs2ItemStack(ItemStubs.GLASS, 100));
        assertItemStackListContents(itemStorageChannelOf(network).getAll(), new Rs2ItemStack(ItemStubs.DIRT, 160), new Rs2ItemStack(ItemStubs.GLASS, 140));

        assertThat(storageOf(diskDrive).getStored()).isEqualTo(150 + 10 + 140);
    }

    @Test
    void Test_extracting() {
        // Arrange
        BulkStorage<Rs2ItemStack> bulkStorage1 = BulkStorageImpl.createItemStorageDisk(100);
        bulkStorage1.insert(new Rs2ItemStack(ItemStubs.DIRT), 50, Action.EXECUTE);
        bulkStorage1.insert(new Rs2ItemStack(ItemStubs.GLASS), 50, Action.EXECUTE);
        storageDiskProviderManager.setDiskInSlot(1, bulkStorage1);

        BulkStorage<Rs2ItemStack> bulkStorage2 = BulkStorageImpl.createItemStorageDisk(100);
        bulkStorage2.insert(new Rs2ItemStack(ItemStubs.DIRT), 50, Action.EXECUTE);
        bulkStorage2.insert(new Rs2ItemStack(ItemStubs.GLASS), 50, Action.EXECUTE);
        storageDiskProviderManager.setDiskInSlot(2, bulkStorage2);

        BulkStorage<Rs2ItemStack> bulkStorage3 = BulkStorageImpl.createItemStorageDisk(100);
        bulkStorage3.insert(new Rs2ItemStack(ItemStubs.SPONGE), 10, Action.EXECUTE);
        storageDiskProviderManager.setDiskInSlot(3, bulkStorage3);

        diskDrive.initialize(storageDiskProviderManager);
        itemStorageChannelOf(network).invalidate();

        // Act
        Optional<Rs2ItemStack> extracted = itemStorageChannelOf(network).extract(new Rs2ItemStack(ItemStubs.DIRT), 85, Action.EXECUTE);

        // Assert
        assertThat(extracted).isPresent();
        assertItemStack(extracted.get(), new Rs2ItemStack(ItemStubs.DIRT, 85));

        assertItemStackListContents(bulkStorage1.getAll(), new Rs2ItemStack(ItemStubs.GLASS, 50));
        assertItemStackListContents(bulkStorage2.getAll(), new Rs2ItemStack(ItemStubs.GLASS, 50), new Rs2ItemStack(ItemStubs.DIRT, 15));
        assertItemStackListContents(bulkStorage3.getAll(), new Rs2ItemStack(ItemStubs.SPONGE, 10));
        assertItemStackListContents(
                itemStorageChannelOf(network).getAll(),
                new Rs2ItemStack(ItemStubs.GLASS, 100),
                new Rs2ItemStack(ItemStubs.DIRT, 15),
                new Rs2ItemStack(ItemStubs.SPONGE, 10)
        );

        assertThat(storageOf(diskDrive).getStored()).isEqualTo(125);
    }

    @Test
    void Test_inserting_with_filter() {
        // Arrange
        diskDrive.setFilterMode(FilterMode.BLOCK);
        diskDrive.setExactMode(false);
        diskDrive.setFilterTemplates(Arrays.asList(new Rs2ItemStack(ItemStubs.GLASS), new Rs2ItemStack(ItemStubs.STONE)));

        BulkStorage<Rs2ItemStack> bulkStorage = BulkStorageImpl.createItemStorageDisk(100);
        storageDiskProviderManager.setDiskInSlot(1, bulkStorage);

        diskDrive.initialize(storageDiskProviderManager);

        // Act
        Rs2ItemStack glassWithTag = new Rs2ItemStack(ItemStubs.GLASS, 12, "myTag");

        Optional<Rs2ItemStack> remainder1 = storageOf(diskDrive).insert(new Rs2ItemStack(ItemStubs.GLASS), 12, Action.EXECUTE);
        Optional<Rs2ItemStack> remainder2 = storageOf(diskDrive).insert(glassWithTag, 12, Action.EXECUTE);
        Optional<Rs2ItemStack> remainder3 = storageOf(diskDrive).insert(new Rs2ItemStack(ItemStubs.DIRT), 10, Action.EXECUTE);

        // Assert
        assertThat(remainder1).isPresent();
        assertItemStack(remainder1.get(), new Rs2ItemStack(ItemStubs.GLASS, 12));

        assertThat(remainder2).isPresent();
        assertItemStack(remainder2.get(), glassWithTag);

        assertThat(remainder3).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(AccessMode.class)
    void Test_inserting_with_access_mode(AccessMode accessMode) {
        // Arrange
        diskDrive.setAccessMode(accessMode);

        BulkStorage<Rs2ItemStack> bulkStorage = BulkStorageImpl.createItemStorageDisk(100);
        storageDiskProviderManager.setDiskInSlot(1, bulkStorage);

        diskDrive.initialize(storageDiskProviderManager);

        // Act
        Optional<Rs2ItemStack> remainder = storageOf(diskDrive).insert(new Rs2ItemStack(ItemStubs.DIRT), 5, Action.EXECUTE);

        // Assert
        switch (accessMode) {
            case INSERT_EXTRACT, INSERT -> assertThat(remainder).isEmpty();
            case EXTRACT -> {
                assertThat(remainder).isPresent();
                assertItemStack(remainder.get(), new Rs2ItemStack(ItemStubs.DIRT, 5));
            }
        }
    }

    @ParameterizedTest
    @EnumSource(AccessMode.class)
    void Test_extracting_with_access_mode(AccessMode accessMode) {
        // Arrange
        diskDrive.setAccessMode(accessMode);

        BulkStorage<Rs2ItemStack> bulkStorage = BulkStorageImpl.createItemStorageDisk(100);
        storageDiskProviderManager.setDiskInSlot(1, bulkStorage);

        diskDrive.initialize(storageDiskProviderManager);

        bulkStorage.insert(new Rs2ItemStack(ItemStubs.DIRT), 20, Action.EXECUTE);

        // Act
        Optional<Rs2ItemStack> extracted = storageOf(diskDrive).extract(new Rs2ItemStack(ItemStubs.DIRT), 5, Action.EXECUTE);

        // Assert
        switch (accessMode) {
            case INSERT_EXTRACT, EXTRACT -> {
                assertThat(extracted).isPresent();
                assertItemStack(extracted.get(), new Rs2ItemStack(ItemStubs.DIRT, 5));
            }
            case INSERT -> assertThat(extracted).isEmpty();
        }
    }

    @Test
    void Test_inserting_when_inactive() {
        // Arrange
        diskDrive.setActive(false);
        diskDrive.initialize(storageDiskProviderManager);

        BulkStorage<Rs2ItemStack> bulkStorage = BulkStorageImpl.createItemStorageDisk(100);
        storageDiskProviderManager.setDiskInSlot(1, bulkStorage);

        // Act
        Optional<Rs2ItemStack> remainder = storageOf(diskDrive).insert(new Rs2ItemStack(ItemStubs.DIRT), 5, Action.EXECUTE);

        // Assert
        assertThat(remainder).isPresent();
        assertItemStack(remainder.get(), new Rs2ItemStack(ItemStubs.DIRT, 5));
    }

    @Test
    void Test_extracting_when_inactive() {
        // Arrange
        diskDrive.setActive(false);

        BulkStorage<Rs2ItemStack> bulkStorage = BulkStorageImpl.createItemStorageDisk(100);
        storageDiskProviderManager.setDiskInSlot(1, bulkStorage);

        diskDrive.initialize(storageDiskProviderManager);

        bulkStorage.insert(new Rs2ItemStack(ItemStubs.DIRT), 20, Action.EXECUTE);

        // Act
        Optional<Rs2ItemStack> extracted = storageOf(diskDrive).extract(new Rs2ItemStack(ItemStubs.DIRT), 5, Action.EXECUTE);

        // Assert
        assertThat(extracted).isEmpty();
    }

    @Test
    void Test_disk_state_change_listener_should_not_be_called_when_not_necessary_on_extracting() {
        // Arrange
        BulkStorage<Rs2ItemStack> bulkStorage = BulkStorageImpl.createItemStorageDisk(100);
        bulkStorage.insert(new Rs2ItemStack(ItemStubs.DIRT), 76, Action.EXECUTE);
        storageDiskProviderManager.setDiskInSlot(1, bulkStorage);
        diskDrive.initialize(storageDiskProviderManager);

        // Act
        itemStorageChannelOf(network).extract(new Rs2ItemStack(ItemStubs.DIRT), 1, Action.EXECUTE);

        // Assert
        verify(diskDriveListener, never()).onDiskChanged();
    }

    @Test
    void Test_disk_state_change_listener_should_not_be_called_when_not_necessary_on_inserting() {
        // Arrange
        BulkStorage<Rs2ItemStack> bulkStorage = BulkStorageImpl.createItemStorageDisk(100);
        storageDiskProviderManager.setDiskInSlot(1, bulkStorage);
        diskDrive.initialize(storageDiskProviderManager);

        // Act
        itemStorageChannelOf(network).insert(new Rs2ItemStack(ItemStubs.DIRT), 74, Action.EXECUTE);

        // Assert
        verify(diskDriveListener, never()).onDiskChanged();
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void Test_disk_state_change_listener_should_be_called_when_necessary_on_extracting(Action action) {
        // Arrange
        BulkStorage<Rs2ItemStack> bulkStorage = BulkStorageImpl.createItemStorageDisk(100);
        bulkStorage.insert(new Rs2ItemStack(ItemStubs.DIRT), 75, Action.EXECUTE);
        storageDiskProviderManager.setDiskInSlot(1, bulkStorage);
        diskDrive.initialize(storageDiskProviderManager);

        // Act
        itemStorageChannelOf(network).extract(new Rs2ItemStack(ItemStubs.DIRT), 1, action);
        itemStorageChannelOf(network).extract(new Rs2ItemStack(ItemStubs.DIRT), 1, action);

        // Assert
        VerificationMode expectedTimes = action == Action.EXECUTE ? times(1) : never();

        verify(diskDriveListener, expectedTimes).onDiskChanged();
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void Test_disk_state_change_listener_should_be_called_when_necessary_on_inserting(Action action) {
        // Arrange
        BulkStorage<Rs2ItemStack> bulkStorage = BulkStorageImpl.createItemStorageDisk(100);
        storageDiskProviderManager.setDiskInSlot(1, bulkStorage);
        diskDrive.initialize(storageDiskProviderManager);
        storageOf(diskDrive).insert(new Rs2ItemStack(ItemStubs.DIRT), 74, Action.EXECUTE);

        // Act
        itemStorageChannelOf(network).insert(new Rs2ItemStack(ItemStubs.DIRT), 1, action);
        itemStorageChannelOf(network).insert(new Rs2ItemStack(ItemStubs.DIRT), 1, action);

        // Assert
        VerificationMode expectedTimes = action == Action.EXECUTE ? times(1) : never();

        verify(diskDriveListener, expectedTimes).onDiskChanged();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void Test_setting_priority(boolean oneHasPriority) {
        // Arrange
        BulkStorage<Rs2ItemStack> bulkStorage1 = BulkStorageImpl.createItemStorageDisk(100);
        storageDiskProviderManager.setDiskInSlot(1, bulkStorage1);
        diskDrive.initialize(storageDiskProviderManager);

        BulkStorage<Rs2ItemStack> bulkStorage2 = BulkStorageImpl.createItemStorageDisk(100);
        FakeStorageProviderManager storageDiskProviderManager2 = new FakeStorageProviderManager();
        storageDiskProviderManager2.setDiskInSlot(1, bulkStorage2);
        FakeNetworkNodeContainer<DiskDriveNetworkNode> diskDrive2 = createDiskDriveContainer(network, storageDiskProviderManager2, mock(DiskDriveListener.class));
        diskDrive2.getNode().initialize(storageDiskProviderManager2);

        if (oneHasPriority) {
            diskDrive.setPriority(5);
            diskDrive2.getNode().setPriority(2);
        } else {
            diskDrive.setPriority(2);
            diskDrive2.getNode().setPriority(5);
        }

        // Act
        itemStorageChannelOf(network).insert(new Rs2ItemStack(ItemStubs.DIRT), 1, Action.EXECUTE);

        // Assert
        if (oneHasPriority) {
            assertThat(storageOf(diskDrive).getAll()).isNotEmpty();
            assertThat(storageOf(diskDrive2.getNode()).getAll()).isEmpty();
        } else {
            assertThat(storageOf(diskDrive).getAll()).isEmpty();
            assertThat(storageOf(diskDrive2.getNode()).getAll()).isNotEmpty();
        }
    }
}
