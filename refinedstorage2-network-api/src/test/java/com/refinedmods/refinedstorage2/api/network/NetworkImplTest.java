package com.refinedmods.refinedstorage2.api.network;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.network.energy.EnergyStorageImpl;
import com.refinedmods.refinedstorage2.api.network.node.container.NetworkNodeContainer;
import com.refinedmods.refinedstorage2.api.network.node.controller.ControllerNetworkNode;
import com.refinedmods.refinedstorage2.api.network.node.diskdrive.DiskDriveListener;
import com.refinedmods.refinedstorage2.api.network.node.diskdrive.DiskDriveNetworkNode;
import com.refinedmods.refinedstorage2.api.network.node.diskdrive.FakeStorageProviderRepository;
import com.refinedmods.refinedstorage2.api.storage.CappedStorage;
import com.refinedmods.refinedstorage2.api.storage.Storage;
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

        FakeStorageProviderRepository storageProviderRepository = new FakeStorageProviderRepository();
        Storage<String> storage = new CappedStorage<>(10);
        storage.insert("A", 10, Action.EXECUTE);
        storageProviderRepository.setInSlot(1, storage);
        DiskDriveNetworkNode diskDrive = new DiskDriveNetworkNode(0, 0, STORAGE_CHANNEL_TYPE_REGISTRY);
        diskDrive.setNetwork(network);
        diskDrive.setDiskProvider(storageProviderRepository);
        diskDrive.initialize(storageProviderRepository);
        diskDrive.setListener(mock(DiskDriveListener.class));
        NetworkNodeContainer diskDriveContainer = () -> diskDrive;

        ControllerNetworkNode controllerNetworkNode = new ControllerNetworkNode();
        controllerNetworkNode.setNetwork(network);
        controllerNetworkNode.setEnergyStorage(new EnergyStorageImpl(1000));
        NetworkNodeContainer controllerContainer = () -> controllerNetworkNode;

        // Act
        network.addContainer(controllerContainer);
        network.addContainer(diskDriveContainer);

        // Assert
        assertThat(fakeStorageChannelOf(network).getAll()).isNotEmpty();
    }
}
