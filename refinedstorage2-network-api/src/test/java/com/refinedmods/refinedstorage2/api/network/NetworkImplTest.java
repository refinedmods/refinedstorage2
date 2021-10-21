package com.refinedmods.refinedstorage2.api.network;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.network.node.container.FakeNetworkNodeContainer;
import com.refinedmods.refinedstorage2.api.network.node.controller.ControllerListener;
import com.refinedmods.refinedstorage2.api.network.node.controller.ControllerNetworkNode;
import com.refinedmods.refinedstorage2.api.network.node.controller.ControllerType;
import com.refinedmods.refinedstorage2.api.network.node.diskdrive.DiskDriveListener;
import com.refinedmods.refinedstorage2.api.network.node.diskdrive.DiskDriveNetworkNode;
import com.refinedmods.refinedstorage2.api.network.node.diskdrive.FakeStorageProviderRepository;
import com.refinedmods.refinedstorage2.api.storage.bulk.BulkStorage;
import com.refinedmods.refinedstorage2.api.storage.bulk.BulkStorageImpl;
import com.refinedmods.refinedstorage2.test.Rs2Test;

import org.junit.jupiter.api.Test;

import static com.refinedmods.refinedstorage2.api.network.NetworkUtil.NETWORK_COMPONENT_REGISTRY;
import static com.refinedmods.refinedstorage2.api.network.NetworkUtil.STORAGE_CHANNEL_TYPE_REGISTRY;
import static com.refinedmods.refinedstorage2.api.network.NetworkUtil.fakeStorageChannelOf;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@Rs2Test
class NetworkImplTest {
    @Test
    void Test_should_build_network_correctly() {
        // Arrange
        Network network = new NetworkImpl(NETWORK_COMPONENT_REGISTRY);

        FakeStorageProviderRepository fakeStorageProviderManager = new FakeStorageProviderRepository();
        BulkStorage<String> storage = new BulkStorageImpl<>(10);
        storage.insert("A", 10, Action.EXECUTE);
        fakeStorageProviderManager.setInSlot(1, storage);
        DiskDriveNetworkNode diskDrive = new DiskDriveNetworkNode(fakeStorageProviderManager, 0, 0, mock(DiskDriveListener.class), STORAGE_CHANNEL_TYPE_REGISTRY);
        diskDrive.setNetwork(network);
        diskDrive.initialize(fakeStorageProviderManager);
        FakeNetworkNodeContainer<DiskDriveNetworkNode> diskDriveContainer = new FakeNetworkNodeContainer<>(diskDrive);

        ControllerNetworkNode controllerNetworkNode = new ControllerNetworkNode(100, 100, ControllerType.NORMAL, mock(ControllerListener.class));
        controllerNetworkNode.setNetwork(network);
        FakeNetworkNodeContainer<ControllerNetworkNode> controllerContainer = new FakeNetworkNodeContainer<>(controllerNetworkNode);

        // Act
        network.addContainer(controllerContainer);
        network.addContainer(diskDriveContainer);

        // Assert
        assertThat(fakeStorageChannelOf(network).getAll()).isNotEmpty();
    }
}
