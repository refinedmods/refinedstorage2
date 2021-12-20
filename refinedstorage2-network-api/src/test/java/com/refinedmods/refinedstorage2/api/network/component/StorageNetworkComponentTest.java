package com.refinedmods.refinedstorage2.api.network.component;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.network.node.container.NetworkNodeContainer;
import com.refinedmods.refinedstorage2.api.network.node.diskdrive.DiskDriveListener;
import com.refinedmods.refinedstorage2.api.network.node.diskdrive.DiskDriveNetworkNode;
import com.refinedmods.refinedstorage2.api.network.node.diskdrive.FakeStorageProviderRepository;
import com.refinedmods.refinedstorage2.api.network.test.StorageChannelTypes;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.storage.CappedStorage;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannel;
import com.refinedmods.refinedstorage2.test.Rs2Test;

import java.util.Collection;

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
    private NetworkNodeContainer diskDriveContainer;

    @BeforeEach
    void setUp() {
        sut = new StorageNetworkComponent(STORAGE_CHANNEL_TYPE_REGISTRY);

        FakeStorageProviderRepository storageProviderRepository = new FakeStorageProviderRepository();
        storageProviderRepository.setInSlot(0, new CappedStorage<>(100));

        diskDrive = new DiskDriveNetworkNode(0, 0, STORAGE_CHANNEL_TYPE_REGISTRY);
        diskDrive.setNetwork(createWithInfiniteEnergyStorage());
        diskDrive.setListener(mock(DiskDriveListener.class));
        diskDrive.setDiskProvider(storageProviderRepository);
        diskDrive.initialize(storageProviderRepository);

        diskDriveContainer = () -> diskDrive;
    }

    @Test
    void Test_initial_state() {
        // Act
        Collection<ResourceAmount<String>> resources = sut.getStorageChannel(StorageChannelTypes.FAKE).getAll();

        // Assert
        assertThat(resources).isEmpty();
    }

    @Test
    void Test_adding_storage_source_container() {
        // Arrange
        StorageChannel<String> storageChannel = sut.getStorageChannel(StorageChannelTypes.FAKE);

        // Act
        sut.onContainerAdded(diskDriveContainer);

        long remainder = storageChannel.insert("A", 10, Action.EXECUTE);

        // Assert
        assertThat(remainder).isZero();
        assertThat(storageChannel.getAll()).isNotEmpty();
    }

    @Test
    void Test_removing_storage_source_container() {
        // Arrange
        diskDrive.getStorageForChannel(StorageChannelTypes.FAKE).get().insert("A", 10, Action.EXECUTE);

        StorageChannel<String> storageChannel = sut.getStorageChannel(StorageChannelTypes.FAKE);

        sut.onContainerAdded(diskDriveContainer);

        Collection<ResourceAmount<String>> resourcesBeforeRemoval = storageChannel.getAll();

        // Act
        sut.onContainerRemoved(diskDriveContainer);

        // Assert
        assertThat(resourcesBeforeRemoval).isNotEmpty();
        assertThat(storageChannel.getAll()).isEmpty();
    }
}
