package com.refinedmods.refinedstorage2.api.network.component;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.network.Network;
import com.refinedmods.refinedstorage2.api.network.NetworkUtil;
import com.refinedmods.refinedstorage2.api.network.node.container.NetworkNodeContainer;
import com.refinedmods.refinedstorage2.api.network.node.diskdrive.DiskDriveListener;
import com.refinedmods.refinedstorage2.api.network.node.diskdrive.DiskDriveNetworkNode;
import com.refinedmods.refinedstorage2.api.network.node.diskdrive.FakeStorageProviderRepository;
import com.refinedmods.refinedstorage2.api.network.node.storage.StorageNetworkNode;
import com.refinedmods.refinedstorage2.api.network.test.StorageChannelTypes;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.storage.EmptySource;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannel;
import com.refinedmods.refinedstorage2.api.storage.limited.LimitedStorageImpl;
import com.refinedmods.refinedstorage2.test.Rs2Test;

import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.refinedmods.refinedstorage2.api.network.NetworkUtil.STORAGE_CHANNEL_TYPE_REGISTRY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@Rs2Test
class StorageNetworkComponentTest {
    private StorageNetworkComponent sut;

    private DiskDriveNetworkNode diskDrive;
    private NetworkNodeContainer diskDriveContainer;

    private StorageNetworkNode<String> storage;
    private NetworkNodeContainer storageContainer;

    @BeforeEach
    void setUp() {
        Network network = NetworkUtil.create();
        sut = network.getComponent(StorageNetworkComponent.class);

        FakeStorageProviderRepository storageProviderRepository = new FakeStorageProviderRepository();
        storageProviderRepository.setInSlot(0, new LimitedStorageImpl<>(100));

        diskDrive = new DiskDriveNetworkNode(0, 0, STORAGE_CHANNEL_TYPE_REGISTRY);
        diskDrive.setNetwork(network);
        diskDrive.setListener(mock(DiskDriveListener.class));
        diskDrive.setDiskProvider(storageProviderRepository);
        diskDrive.initialize(storageProviderRepository);
        diskDrive.onActiveChanged(true);
        diskDriveContainer = () -> diskDrive;

        storage = new StorageNetworkNode<>(0, StorageChannelTypes.FAKE);
        storage.setNetwork(network);
        storage.initializeNewStorage(storageProviderRepository, new LimitedStorageImpl<>(100), UUID.randomUUID());
        storage.onActiveChanged(true);
        storageContainer = () -> storage;
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
        long insertedPre = storageChannel.insert("A", 10, Action.EXECUTE, EmptySource.INSTANCE);
        sut.onContainerAdded(diskDriveContainer);
        long insertedPost = storageChannel.insert("A", 10, Action.EXECUTE, EmptySource.INSTANCE);

        // Assert
        assertThat(insertedPre).isZero();
        assertThat(insertedPost).isEqualTo(10);
        assertThat(storageChannel.getAll()).isNotEmpty();
    }

    @Test
    void Test_removing_storage_source_container() {
        // Arrange
        StorageChannel<String> storageChannel = sut.getStorageChannel(StorageChannelTypes.FAKE);

        sut.onContainerAdded(diskDriveContainer);
        sut.onContainerAdded(storageContainer);

        // Ensure that we fill our 2 containers.
        storageChannel.insert("A", 200, Action.EXECUTE, EmptySource.INSTANCE);

        // Act
        Collection<ResourceAmount<String>> resourcesPre = new HashSet<>(storageChannel.getAll());
        sut.onContainerRemoved(diskDriveContainer);
        sut.onContainerRemoved(storageContainer);
        Collection<ResourceAmount<String>> resourcesPost = storageChannel.getAll();

        // Assert
        assertThat(resourcesPre).isNotEmpty();
        assertThat(resourcesPost).isEmpty();
    }
}
