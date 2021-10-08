package com.refinedmods.refinedstorage2.api.network.component;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.network.node.container.FakeNetworkNodeContainer;
import com.refinedmods.refinedstorage2.api.network.node.container.NetworkNodeContainer;
import com.refinedmods.refinedstorage2.api.network.node.diskdrive.DiskDriveListener;
import com.refinedmods.refinedstorage2.api.network.node.diskdrive.DiskDriveNetworkNode;
import com.refinedmods.refinedstorage2.api.network.node.diskdrive.FakeStorageProviderManager;
import com.refinedmods.refinedstorage2.api.resource.item.Rs2ItemStack;
import com.refinedmods.refinedstorage2.api.resource.test.ItemStubs;
import com.refinedmods.refinedstorage2.api.storage.bulk.BulkStorageImpl;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannel;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelTypes;
import com.refinedmods.refinedstorage2.test.Rs2Test;

import java.util.Collection;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.refinedmods.refinedstorage2.api.network.NetworkUtil.STORAGE_CHANNEL_TYPE_REGISTRY;
import static com.refinedmods.refinedstorage2.api.network.NetworkUtil.createWithInfiniteEnergyStorage;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@Rs2Test
class StorageNetworkComponentTest {
    private StorageNetworkComponent sut;
    private DiskDriveNetworkNode diskDrive;
    private NetworkNodeContainer<DiskDriveNetworkNode> diskDriveContainer;

    @BeforeEach
    void setUp() {
        sut = new StorageNetworkComponent(STORAGE_CHANNEL_TYPE_REGISTRY);

        FakeStorageProviderManager fakeStorageDiskProviderManager = new FakeStorageProviderManager();
        fakeStorageDiskProviderManager.setDiskInSlot(0, BulkStorageImpl.createItemStorageDisk(100));

        diskDrive = new DiskDriveNetworkNode(fakeStorageDiskProviderManager, 0, 0, mock(DiskDriveListener.class), STORAGE_CHANNEL_TYPE_REGISTRY);
        diskDrive.setNetwork(createWithInfiniteEnergyStorage());
        diskDrive.initialize(fakeStorageDiskProviderManager);

        diskDriveContainer = new FakeNetworkNodeContainer<>(diskDrive);
    }

    @Test
    void Test_initial_state() {
        // Act
        Collection<Rs2ItemStack> stacks = sut.getStorageChannel(StorageChannelTypes.ITEM).getAll();

        // Assert
        assertThat(stacks).isEmpty();
    }

    @Test
    void Test_adding_storage_source_container() {
        // Arrange
        StorageChannel<Rs2ItemStack> storageChannel = sut.getStorageChannel(StorageChannelTypes.ITEM);

        // Act
        sut.onContainerAdded(diskDriveContainer);

        Optional<Rs2ItemStack> remainder = storageChannel.insert(new Rs2ItemStack(ItemStubs.DIRT), 10, Action.EXECUTE);

        // Assert
        assertThat(remainder).isEmpty();
        assertThat(storageChannel.getAll()).isNotEmpty();
    }

    @Test
    void Test_removing_storage_source_container() {
        // Arrange
        diskDrive.getStorageForChannel(StorageChannelTypes.ITEM).get().insert(new Rs2ItemStack(ItemStubs.DIRT), 10, Action.EXECUTE);

        StorageChannel<Rs2ItemStack> storageChannel = sut.getStorageChannel(StorageChannelTypes.ITEM);

        sut.onContainerAdded(diskDriveContainer);

        Collection<Rs2ItemStack> stacksBeforeRemoval = storageChannel.getAll();

        // Act
        sut.onContainerRemoved(diskDriveContainer);

        // Assert
        assertThat(stacksBeforeRemoval).isNotEmpty();
        assertThat(storageChannel.getAll()).isEmpty();
    }
}
