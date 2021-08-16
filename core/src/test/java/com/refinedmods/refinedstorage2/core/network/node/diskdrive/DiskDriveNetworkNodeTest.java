package com.refinedmods.refinedstorage2.core.network.node.diskdrive;

import com.refinedmods.refinedstorage2.core.Rs2Test;
import com.refinedmods.refinedstorage2.core.item.ItemStubs;
import com.refinedmods.refinedstorage2.core.item.Rs2ItemStack;
import com.refinedmods.refinedstorage2.core.network.Network;
import com.refinedmods.refinedstorage2.core.network.NetworkUtil;
import com.refinedmods.refinedstorage2.core.network.node.RedstoneMode;
import com.refinedmods.refinedstorage2.core.network.node.container.FakeNetworkNodeContainer;
import com.refinedmods.refinedstorage2.core.storage.AccessMode;
import com.refinedmods.refinedstorage2.core.storage.disk.DiskState;
import com.refinedmods.refinedstorage2.core.storage.disk.ItemDiskStorage;
import com.refinedmods.refinedstorage2.core.storage.disk.StorageDisk;
import com.refinedmods.refinedstorage2.core.util.Action;
import com.refinedmods.refinedstorage2.core.util.FilterMode;
import com.refinedmods.refinedstorage2.core.util.Position;

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

import static com.refinedmods.refinedstorage2.core.network.NetworkUtil.itemStorageChannelOf;
import static com.refinedmods.refinedstorage2.core.network.NetworkUtil.itemStorageOf;
import static com.refinedmods.refinedstorage2.core.util.ItemStackAssertions.assertItemStack;
import static com.refinedmods.refinedstorage2.core.util.ItemStackAssertions.assertItemStackListContents;
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
    private FakeStorageDiskProviderManager storageDiskProviderManager;
    private DiskDriveListener diskDriveListener;

    @BeforeEach
    void setUp() {
        diskDriveListener = mock(DiskDriveListener.class);
        storageDiskProviderManager = new FakeStorageDiskProviderManager();

        network = NetworkUtil.createWithCreativeEnergySource();

        diskDrive = createDiskDriveContainer(network, storageDiskProviderManager, diskDriveListener).getNode();
    }

    private FakeNetworkNodeContainer<DiskDriveNetworkNode> createDiskDriveContainer(Network network, FakeStorageDiskProviderManager storageDiskProviderManager, DiskDriveListener diskDriveListener) {
        DiskDriveNetworkNode diskDrive = new DiskDriveNetworkNode(Position.ORIGIN, storageDiskProviderManager, BASE_USAGE, USAGE_PER_DISK, diskDriveListener);
        diskDrive.setNetwork(network);

        FakeNetworkNodeContainer<DiskDriveNetworkNode> container = FakeNetworkNodeContainer.createForFakeWorld(diskDrive);

        network.addContainer(container);

        return container;
    }

    @Test
    void Test_initial_state() {
        // Arrange
        ItemDiskStorage disk = new ItemDiskStorage(10);
        disk.insert(new Rs2ItemStack(ItemStubs.DIRT), 5, Action.EXECUTE);
        storageDiskProviderManager.setDiskInSlot(1, disk);

        // Act
        DiskDriveState states = diskDrive.createState();

        // Assert
        assertThat(diskDrive.getEnergyUsage()).isEqualTo(BASE_USAGE);
        assertThat(diskDrive.getStacks()).isEmpty();
        assertThat(diskDrive.getStored()).isZero();
        assertThat(states.getStates())
                .hasSize(DiskDriveNetworkNode.DISK_COUNT)
                .allMatch(state -> state == DiskState.NONE);
        assertItemStackListContents(itemStorageChannelOf(network).getStacks());
    }

    @Test
    void Test_initialization() {
        // Arrange
        ItemDiskStorage disk = new ItemDiskStorage(10);
        disk.insert(new Rs2ItemStack(ItemStubs.DIRT), 5, Action.EXECUTE);
        storageDiskProviderManager.setDiskInSlot(1, disk);

        // Act
        diskDrive.initialize(storageDiskProviderManager);

        // Assert
        assertThat(diskDrive.getEnergyUsage()).isEqualTo(BASE_USAGE + USAGE_PER_DISK);
        assertItemStackListContents(diskDrive.getStacks(), new Rs2ItemStack(ItemStubs.DIRT, 5));
        assertItemStackListContents(itemStorageChannelOf(network).getStacks());
        assertThat(diskDrive.getStored()).isEqualTo(5L);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void Test_disk_state(boolean inactive) {
        // Arrange
        StorageDisk<Rs2ItemStack> normalDisk = new ItemDiskStorage(100);
        normalDisk.insert(new Rs2ItemStack(ItemStubs.DIRT), 74, Action.EXECUTE);

        StorageDisk<Rs2ItemStack> nearCapacityDisk = new ItemDiskStorage(100);
        nearCapacityDisk.insert(new Rs2ItemStack(ItemStubs.DIRT), 75, Action.EXECUTE);

        StorageDisk<Rs2ItemStack> fullDisk = new ItemDiskStorage(100);
        fullDisk.insert(new Rs2ItemStack(ItemStubs.DIRT), 100, Action.EXECUTE);

        storageDiskProviderManager.setDiskInSlot(1, UUID.randomUUID());
        storageDiskProviderManager.setDiskInSlot(3, normalDisk);
        storageDiskProviderManager.setDiskInSlot(5, nearCapacityDisk);
        storageDiskProviderManager.setDiskInSlot(7, fullDisk);

        if (inactive) {
            diskDrive.setRedstoneMode(RedstoneMode.HIGH);
        }

        // Act
        diskDrive.initialize(storageDiskProviderManager);

        DiskDriveState state = diskDrive.createState();

        // Assert
        assertThat(state.getState(0)).isEqualTo(DiskState.NONE);
        assertThat(state.getState(1)).isEqualTo(DiskState.NONE);
        assertThat(state.getState(2)).isEqualTo(DiskState.NONE);
        assertThat(state.getState(3)).isEqualTo(inactive ? DiskState.DISCONNECTED : DiskState.NORMAL);
        assertThat(state.getState(4)).isEqualTo(DiskState.NONE);
        assertThat(state.getState(5)).isEqualTo(inactive ? DiskState.DISCONNECTED : DiskState.NEAR_CAPACITY);
        assertThat(state.getState(6)).isEqualTo(DiskState.NONE);
        assertThat(state.getState(7)).isEqualTo(inactive ? DiskState.DISCONNECTED : DiskState.FULL);
        assertThat(diskDrive.getEnergyUsage()).isEqualTo(BASE_USAGE + (USAGE_PER_DISK * 3));
    }

    @Test
    void Test_setting_disk_in_slot() {
        // Arrange
        diskDrive.initialize(storageDiskProviderManager);

        ItemDiskStorage disk = new ItemDiskStorage(10);
        disk.insert(new Rs2ItemStack(ItemStubs.DIRT), 5, Action.EXECUTE);
        storageDiskProviderManager.setDiskInSlot(7, disk);

        // Act
        diskDrive.onDiskChanged(7);

        // Assert
        assertThat(diskDrive.getEnergyUsage()).isEqualTo(BASE_USAGE + USAGE_PER_DISK);
        assertItemStackListContents(diskDrive.getStacks(), new Rs2ItemStack(ItemStubs.DIRT, 5));
        assertItemStackListContents(itemStorageChannelOf(network).getStacks(), new Rs2ItemStack(ItemStubs.DIRT, 5));
        assertThat(diskDrive.getStored()).isEqualTo(5L);
    }

    @Test
    void Test_removing_disk_in_slot() {
        // Arrange
        diskDrive.initialize(storageDiskProviderManager);

        ItemDiskStorage disk = new ItemDiskStorage(10);
        disk.insert(new Rs2ItemStack(ItemStubs.DIRT), 5, Action.EXECUTE);
        storageDiskProviderManager.setDiskInSlot(7, disk);

        diskDrive.onDiskChanged(7);

        // Act
        storageDiskProviderManager.removeDiskInSlot(7);
        diskDrive.onDiskChanged(7);

        // Assert
        assertThat(diskDrive.getEnergyUsage()).isEqualTo(BASE_USAGE);
        assertItemStackListContents(diskDrive.getStacks());
        assertItemStackListContents(itemStorageChannelOf(network).getStacks());
        assertThat(diskDrive.getStored()).isZero();
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
                .allMatch(state -> state == DiskState.NONE);
    }

    @Test
    void Test_retrieving_stacks() {
        // Arrange
        diskDrive.initialize(storageDiskProviderManager);

        StorageDisk<Rs2ItemStack> storageDisk1 = new ItemDiskStorage(100);
        storageDisk1.insert(new Rs2ItemStack(ItemStubs.DIRT), 50, Action.EXECUTE);
        storageDisk1.insert(new Rs2ItemStack(ItemStubs.GLASS), 50, Action.EXECUTE);
        storageDiskProviderManager.setDiskInSlot(1, storageDisk1);

        diskDrive.onDiskChanged(1);

        // Act
        Collection<Rs2ItemStack> stacks = diskDrive.getStacks();
        Collection<Rs2ItemStack> stacksInNetwork = itemStorageChannelOf(network).getStacks();
        long storedInNetwork = itemStorageChannelOf(network).getStored();

        // Assert
        assertItemStackListContents(stacks, new Rs2ItemStack(ItemStubs.DIRT, 50), new Rs2ItemStack(ItemStubs.GLASS, 50));
        assertItemStackListContents(stacksInNetwork, new Rs2ItemStack(ItemStubs.DIRT, 50), new Rs2ItemStack(ItemStubs.GLASS, 50));
        assertThat(diskDrive.getStored()).isEqualTo(100);
        assertThat(storedInNetwork).isEqualTo(100);
    }

    @Test
    void Test_retrieving_stacks_when_inactive() {
        // Arrange
        diskDrive.initialize(storageDiskProviderManager);

        StorageDisk<Rs2ItemStack> storageDisk1 = new ItemDiskStorage(100);
        storageDisk1.insert(new Rs2ItemStack(ItemStubs.DIRT), 50, Action.EXECUTE);
        storageDisk1.insert(new Rs2ItemStack(ItemStubs.GLASS), 50, Action.EXECUTE);
        storageDiskProviderManager.setDiskInSlot(1, storageDisk1);

        diskDrive.setRedstoneMode(RedstoneMode.HIGH);
        diskDrive.onDiskChanged(1);

        // Act
        Collection<Rs2ItemStack> stacks = diskDrive.getStacks();
        Collection<Rs2ItemStack> stacksInNetwork = itemStorageChannelOf(network).getStacks();
        long storedInNetwork = itemStorageChannelOf(network).getStored();

        // Assert
        assertItemStackListContents(stacks);
        assertItemStackListContents(stacksInNetwork);
        assertThat(diskDrive.getStored()).isEqualTo(100L);
        assertThat(storedInNetwork).isEqualTo(100L);
    }

    @Test
    void Test_inserting() {
        // Arrange
        StorageDisk<Rs2ItemStack> storageDisk1 = new ItemDiskStorage(100);
        storageDiskProviderManager.setDiskInSlot(1, storageDisk1);

        StorageDisk<Rs2ItemStack> storageDisk2 = new ItemDiskStorage(100);
        storageDiskProviderManager.setDiskInSlot(2, storageDisk2);

        StorageDisk<Rs2ItemStack> storageDisk3 = new ItemDiskStorage(100);
        storageDiskProviderManager.setDiskInSlot(3, storageDisk3);

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

        assertItemStackListContents(storageDisk1.getStacks(), new Rs2ItemStack(ItemStubs.DIRT, 100));
        assertItemStackListContents(storageDisk2.getStacks(), new Rs2ItemStack(ItemStubs.DIRT, 60), new Rs2ItemStack(ItemStubs.GLASS, 40));
        assertItemStackListContents(storageDisk3.getStacks(), new Rs2ItemStack(ItemStubs.GLASS, 100));
        assertItemStackListContents(itemStorageChannelOf(network).getStacks(), new Rs2ItemStack(ItemStubs.DIRT, 160), new Rs2ItemStack(ItemStubs.GLASS, 140));

        assertThat(diskDrive.getStored()).isEqualTo(150 + 10 + 140);
    }

    @Test
    void Test_extracting() {
        // Arrange
        StorageDisk<Rs2ItemStack> storageDisk1 = new ItemDiskStorage(100);
        storageDisk1.insert(new Rs2ItemStack(ItemStubs.DIRT), 50, Action.EXECUTE);
        storageDisk1.insert(new Rs2ItemStack(ItemStubs.GLASS), 50, Action.EXECUTE);
        storageDiskProviderManager.setDiskInSlot(1, storageDisk1);

        StorageDisk<Rs2ItemStack> storageDisk2 = new ItemDiskStorage(100);
        storageDisk2.insert(new Rs2ItemStack(ItemStubs.DIRT), 50, Action.EXECUTE);
        storageDisk2.insert(new Rs2ItemStack(ItemStubs.GLASS), 50, Action.EXECUTE);
        storageDiskProviderManager.setDiskInSlot(2, storageDisk2);

        StorageDisk<Rs2ItemStack> storageDisk3 = new ItemDiskStorage(100);
        storageDisk3.insert(new Rs2ItemStack(ItemStubs.SPONGE), 10, Action.EXECUTE);
        storageDiskProviderManager.setDiskInSlot(3, storageDisk3);

        diskDrive.initialize(storageDiskProviderManager);
        itemStorageOf(network).invalidate();

        // Act
        Optional<Rs2ItemStack> extracted = itemStorageChannelOf(network).extract(new Rs2ItemStack(ItemStubs.DIRT), 85, Action.EXECUTE);

        // Assert
        assertThat(extracted).isPresent();
        assertItemStack(extracted.get(), new Rs2ItemStack(ItemStubs.DIRT, 85));

        assertItemStackListContents(storageDisk1.getStacks(), new Rs2ItemStack(ItemStubs.GLASS, 50));
        assertItemStackListContents(storageDisk2.getStacks(), new Rs2ItemStack(ItemStubs.GLASS, 50), new Rs2ItemStack(ItemStubs.DIRT, 15));
        assertItemStackListContents(storageDisk3.getStacks(), new Rs2ItemStack(ItemStubs.SPONGE, 10));
        assertItemStackListContents(
                itemStorageChannelOf(network).getStacks(),
                new Rs2ItemStack(ItemStubs.GLASS, 100),
                new Rs2ItemStack(ItemStubs.DIRT, 15),
                new Rs2ItemStack(ItemStubs.SPONGE, 10)
        );

        assertThat(diskDrive.getStored()).isEqualTo(125);
    }

    @Test
    void Test_inserting_with_filter() {
        // Arrange
        diskDrive.setFilterMode(FilterMode.BLOCK);
        diskDrive.setExactMode(false);
        diskDrive.setFilterTemplates(Arrays.asList(new Rs2ItemStack(ItemStubs.GLASS), new Rs2ItemStack(ItemStubs.STONE)));

        StorageDisk<Rs2ItemStack> storageDisk = new ItemDiskStorage(100);
        storageDiskProviderManager.setDiskInSlot(1, storageDisk);

        diskDrive.initialize(storageDiskProviderManager);

        // Act
        Rs2ItemStack glassWithTag = new Rs2ItemStack(ItemStubs.GLASS, 12, "myTag");

        Optional<Rs2ItemStack> remainder1 = diskDrive.insert(new Rs2ItemStack(ItemStubs.GLASS), 12, Action.EXECUTE);
        Optional<Rs2ItemStack> remainder2 = diskDrive.insert(glassWithTag, 12, Action.EXECUTE);
        Optional<Rs2ItemStack> remainder3 = diskDrive.insert(new Rs2ItemStack(ItemStubs.DIRT), 10, Action.EXECUTE);

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

        StorageDisk<Rs2ItemStack> storageDisk = new ItemDiskStorage(100);
        storageDiskProviderManager.setDiskInSlot(1, storageDisk);

        diskDrive.initialize(storageDiskProviderManager);

        // Act
        Optional<Rs2ItemStack> remainder = diskDrive.insert(new Rs2ItemStack(ItemStubs.DIRT), 5, Action.EXECUTE);

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

        StorageDisk<Rs2ItemStack> storageDisk = new ItemDiskStorage(100);
        storageDiskProviderManager.setDiskInSlot(1, storageDisk);

        diskDrive.initialize(storageDiskProviderManager);

        storageDisk.insert(new Rs2ItemStack(ItemStubs.DIRT), 20, Action.EXECUTE);

        // Act
        Optional<Rs2ItemStack> extracted = diskDrive.extract(new Rs2ItemStack(ItemStubs.DIRT), 5, Action.EXECUTE);

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
        diskDrive.setRedstoneMode(RedstoneMode.HIGH);
        diskDrive.initialize(storageDiskProviderManager);

        StorageDisk<Rs2ItemStack> storageDisk = new ItemDiskStorage(100);
        storageDiskProviderManager.setDiskInSlot(1, storageDisk);

        // Act
        Optional<Rs2ItemStack> remainder = diskDrive.insert(new Rs2ItemStack(ItemStubs.DIRT), 5, Action.EXECUTE);

        // Assert
        assertThat(remainder).isPresent();
        assertItemStack(remainder.get(), new Rs2ItemStack(ItemStubs.DIRT, 5));
    }

    @Test
    void Test_extracting_when_inactive() {
        // Arrange
        diskDrive.setRedstoneMode(RedstoneMode.HIGH);

        StorageDisk<Rs2ItemStack> storageDisk = new ItemDiskStorage(100);
        storageDiskProviderManager.setDiskInSlot(1, storageDisk);

        diskDrive.initialize(storageDiskProviderManager);

        storageDisk.insert(new Rs2ItemStack(ItemStubs.DIRT), 20, Action.EXECUTE);

        // Act
        Optional<Rs2ItemStack> extracted = diskDrive.extract(new Rs2ItemStack(ItemStubs.DIRT), 5, Action.EXECUTE);

        // Assert
        assertThat(extracted).isEmpty();
    }

    @Test
    void Test_disk_state_change_listener_should_not_be_called_when_not_necessary_on_extracting() {
        // Arrange
        StorageDisk<Rs2ItemStack> storageDisk = new ItemDiskStorage(100);
        storageDisk.insert(new Rs2ItemStack(ItemStubs.DIRT), 76, Action.EXECUTE);
        storageDiskProviderManager.setDiskInSlot(1, storageDisk);
        diskDrive.initialize(storageDiskProviderManager);

        // Act
        itemStorageChannelOf(network).extract(new Rs2ItemStack(ItemStubs.DIRT), 1, Action.EXECUTE);

        // Assert
        verify(diskDriveListener, never()).onDiskChanged();
    }

    @Test
    void Test_disk_state_change_listener_should_not_be_called_when_not_necessary_on_inserting() {
        // Arrange
        StorageDisk<Rs2ItemStack> storageDisk = new ItemDiskStorage(100);
        storageDiskProviderManager.setDiskInSlot(1, storageDisk);
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
        StorageDisk<Rs2ItemStack> storageDisk = new ItemDiskStorage(100);
        storageDisk.insert(new Rs2ItemStack(ItemStubs.DIRT), 75, Action.EXECUTE);
        storageDiskProviderManager.setDiskInSlot(1, storageDisk);
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
        StorageDisk<Rs2ItemStack> storageDisk = new ItemDiskStorage(100);
        storageDiskProviderManager.setDiskInSlot(1, storageDisk);
        diskDrive.initialize(storageDiskProviderManager);
        diskDrive.insert(new Rs2ItemStack(ItemStubs.DIRT), 74, Action.EXECUTE);

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
        StorageDisk<Rs2ItemStack> storageDisk1 = new ItemDiskStorage(100);
        storageDiskProviderManager.setDiskInSlot(1, storageDisk1);
        diskDrive.initialize(storageDiskProviderManager);

        StorageDisk<Rs2ItemStack> storageDisk2 = new ItemDiskStorage(100);
        FakeStorageDiskProviderManager storageDiskProviderManager2 = new FakeStorageDiskProviderManager();
        storageDiskProviderManager2.setDiskInSlot(1, storageDisk2);
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
            assertThat(diskDrive.getStacks()).isNotEmpty();
            assertThat(diskDrive2.getNode().getStacks()).isEmpty();
        } else {
            assertThat(diskDrive.getStacks()).isEmpty();
            assertThat(diskDrive2.getNode().getStacks()).isNotEmpty();
        }
    }
}
