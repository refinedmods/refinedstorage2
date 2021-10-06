package com.refinedmods.refinedstorage2.api.network;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.network.node.container.FakeNetworkNodeContainer;
import com.refinedmods.refinedstorage2.api.network.node.controller.ControllerListener;
import com.refinedmods.refinedstorage2.api.network.node.controller.ControllerNetworkNode;
import com.refinedmods.refinedstorage2.api.network.node.controller.ControllerType;
import com.refinedmods.refinedstorage2.api.network.node.diskdrive.DiskDriveListener;
import com.refinedmods.refinedstorage2.api.network.node.diskdrive.DiskDriveNetworkNode;
import com.refinedmods.refinedstorage2.api.network.node.diskdrive.FakeStorageDiskProviderManager;
import com.refinedmods.refinedstorage2.api.resource.item.Rs2ItemStack;
import com.refinedmods.refinedstorage2.api.resource.test.ItemStubs;
import com.refinedmods.refinedstorage2.api.storage.disk.StorageDisk;
import com.refinedmods.refinedstorage2.api.storage.disk.StorageDiskImpl;
import com.refinedmods.refinedstorage2.test.Rs2Test;

import org.junit.jupiter.api.Test;

import static com.refinedmods.refinedstorage2.api.network.NetworkUtil.NETWORK_COMPONENT_REGISTRY;
import static com.refinedmods.refinedstorage2.api.network.NetworkUtil.STORAGE_CHANNEL_TYPE_REGISTRY;
import static com.refinedmods.refinedstorage2.api.network.NetworkUtil.itemStorageChannelOf;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@Rs2Test
class NetworkImplTest {
    @Test
    void Test_should_build_network_correctly() {
        // Arrange
        Network network = new NetworkImpl(NETWORK_COMPONENT_REGISTRY);

        FakeStorageDiskProviderManager fakeStorageDiskProviderManager = new FakeStorageDiskProviderManager();
        StorageDisk<Rs2ItemStack> disk = StorageDiskImpl.createItemStorageDisk(10);
        disk.insert(new Rs2ItemStack(ItemStubs.DIRT), 10, Action.EXECUTE);
        fakeStorageDiskProviderManager.setDiskInSlot(1, disk);
        DiskDriveNetworkNode diskDrive = new DiskDriveNetworkNode(fakeStorageDiskProviderManager, 0, 0, mock(DiskDriveListener.class), STORAGE_CHANNEL_TYPE_REGISTRY);
        diskDrive.setNetwork(network);
        diskDrive.initialize(fakeStorageDiskProviderManager);
        FakeNetworkNodeContainer<DiskDriveNetworkNode> diskDriveContainer = new FakeNetworkNodeContainer<>(diskDrive);

        ControllerNetworkNode controllerNetworkNode = new ControllerNetworkNode(100, 100, ControllerType.NORMAL, mock(ControllerListener.class));
        controllerNetworkNode.setNetwork(network);
        FakeNetworkNodeContainer<ControllerNetworkNode> controllerContainer = new FakeNetworkNodeContainer<>(controllerNetworkNode);

        // Act
        network.addContainer(controllerContainer);
        network.addContainer(diskDriveContainer);

        // Assert
        assertThat(itemStorageChannelOf(network).getAll()).isNotEmpty();
    }
}
