package com.refinedmods.refinedstorage2.core.network.component;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.core.Position;
import com.refinedmods.refinedstorage2.api.stack.item.Rs2ItemStack;
import com.refinedmods.refinedstorage2.api.stack.test.ItemStubs;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelTypes;
import com.refinedmods.refinedstorage2.api.storage.disk.ItemDiskStorage;
import com.refinedmods.refinedstorage2.core.network.NetworkUtil;
import com.refinedmods.refinedstorage2.core.network.node.container.FakeNetworkNodeContainer;
import com.refinedmods.refinedstorage2.core.network.node.container.NetworkNodeContainer;
import com.refinedmods.refinedstorage2.core.network.node.diskdrive.DiskDriveListener;
import com.refinedmods.refinedstorage2.core.network.node.diskdrive.DiskDriveNetworkNode;
import com.refinedmods.refinedstorage2.core.network.node.diskdrive.FakeStorageDiskProviderManager;
import com.refinedmods.refinedstorage2.test.Rs2Test;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.refinedmods.refinedstorage2.api.stack.test.ItemStackAssertions.assertItemStackListContents;
import static com.refinedmods.refinedstorage2.core.network.NetworkUtil.STORAGE_CHANNEL_TYPE_REGISTRY;
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

        FakeStorageDiskProviderManager fakeStorageDiskProviderManager = new FakeStorageDiskProviderManager();
        fakeStorageDiskProviderManager.setDiskInSlot(0, new ItemDiskStorage(100));

        diskDrive = new DiskDriveNetworkNode(Position.ORIGIN, fakeStorageDiskProviderManager, 0, 0, mock(DiskDriveListener.class), STORAGE_CHANNEL_TYPE_REGISTRY);
        diskDrive.setNetwork(NetworkUtil.createWithCreativeEnergySource());
        diskDrive.initialize(fakeStorageDiskProviderManager);

        diskDriveContainer = FakeNetworkNodeContainer.createForFakeWorld(diskDrive);

        sut.onContainerAdded(diskDriveContainer);
    }

    // TODO: Expand these tests
    @Test
    void Test_adding_node_should_invalidate_storage() {
        // Act
        Optional<Rs2ItemStack> remainderBeforeRemoval = sut.getStorageChannel(StorageChannelTypes.ITEM).insert(new Rs2ItemStack(ItemStubs.DIRT), 10, Action.EXECUTE);
        sut.onContainerRemoved(diskDriveContainer);
        Optional<Rs2ItemStack> remainderAfterRemoval = sut.getStorageChannel(StorageChannelTypes.ITEM).insert(new Rs2ItemStack(ItemStubs.DIRT), 10, Action.EXECUTE);

        // Assert
        assertThat(remainderBeforeRemoval).isEmpty();
        assertThat(remainderAfterRemoval).isPresent();
        assertItemStackListContents(diskDrive.getStorageForChannel(StorageChannelTypes.ITEM).get().getStacks(), new Rs2ItemStack(ItemStubs.DIRT, 10));
    }
}
