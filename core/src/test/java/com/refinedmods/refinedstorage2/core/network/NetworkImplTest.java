package com.refinedmods.refinedstorage2.core.network;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.core.Position;
import com.refinedmods.refinedstorage2.api.stack.item.Rs2ItemStack;
import com.refinedmods.refinedstorage2.api.stack.test.ItemStubs;
import com.refinedmods.refinedstorage2.core.network.node.container.FakeNetworkNodeContainer;
import com.refinedmods.refinedstorage2.core.network.node.controller.ControllerNetworkNode;
import com.refinedmods.refinedstorage2.core.network.node.controller.ControllerType;
import com.refinedmods.refinedstorage2.core.network.node.diskdrive.DiskDriveListener;
import com.refinedmods.refinedstorage2.core.network.node.diskdrive.DiskDriveNetworkNode;
import com.refinedmods.refinedstorage2.core.network.node.diskdrive.FakeStorageDiskProviderManager;
import com.refinedmods.refinedstorage2.core.storage.disk.ItemDiskStorage;
import com.refinedmods.refinedstorage2.test.Rs2Test;

import org.junit.jupiter.api.Test;

import static com.refinedmods.refinedstorage2.core.network.NetworkUtil.NETWORK_COMPONENT_REGISTRY;
import static com.refinedmods.refinedstorage2.core.network.NetworkUtil.STORAGE_CHANNEL_TYPE_REGISTRY;
import static com.refinedmods.refinedstorage2.core.network.NetworkUtil.itemStorageChannelOf;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@Rs2Test
class NetworkImplTest {
    @Test
    void Test_should_build_network_correctly() {
        // Arrange
        Network network = new NetworkImpl(NETWORK_COMPONENT_REGISTRY);

        FakeStorageDiskProviderManager fakeStorageDiskProviderManager = new FakeStorageDiskProviderManager();
        ItemDiskStorage disk = new ItemDiskStorage(10);
        disk.insert(new Rs2ItemStack(ItemStubs.DIRT), 10, Action.EXECUTE);
        fakeStorageDiskProviderManager.setDiskInSlot(1, disk);
        DiskDriveNetworkNode diskDrive = new DiskDriveNetworkNode(Position.ORIGIN, fakeStorageDiskProviderManager, 0, 0, mock(DiskDriveListener.class), STORAGE_CHANNEL_TYPE_REGISTRY);
        diskDrive.setNetwork(network);
        diskDrive.initialize(fakeStorageDiskProviderManager);
        FakeNetworkNodeContainer<DiskDriveNetworkNode> diskDriveContainer = FakeNetworkNodeContainer.createForFakeWorld(diskDrive);

        ControllerNetworkNode controllerNetworkNode = new ControllerNetworkNode(Position.ORIGIN, 100, 100, ControllerType.NORMAL);
        controllerNetworkNode.setNetwork(network);
        FakeNetworkNodeContainer<ControllerNetworkNode> controllerContainer = FakeNetworkNodeContainer.createForFakeWorld(controllerNetworkNode);

        // Act
        network.addContainer(controllerContainer);
        network.addContainer(diskDriveContainer);

        // Assert
        assertThat(itemStorageChannelOf(network).getStacks()).isNotEmpty();
    }
}
