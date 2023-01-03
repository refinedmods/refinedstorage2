package com.refinedmods.refinedstorage2.api.network.impl.component;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.network.component.StorageNetworkComponent;
import com.refinedmods.refinedstorage2.api.network.impl.NetworkImpl;
import com.refinedmods.refinedstorage2.api.network.impl.node.diskdrive.DiskDriveListener;
import com.refinedmods.refinedstorage2.api.network.impl.node.diskdrive.DiskDriveNetworkNode;
import com.refinedmods.refinedstorage2.api.network.impl.node.diskdrive.StorageDiskProviderImpl;
import com.refinedmods.refinedstorage2.api.network.node.container.NetworkNodeContainer;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.storage.EmptyActor;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannel;
import com.refinedmods.refinedstorage2.api.storage.limited.LimitedStorageImpl;
import com.refinedmods.refinedstorage2.network.test.NetworkTestFixtures;

import java.util.Collection;
import java.util.HashSet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class StorageNetworkComponentImplTest {
    private StorageNetworkComponent sut;

    private DiskDriveNetworkNode storage1;
    private NetworkNodeContainer storage1Container;

    private DiskDriveNetworkNode storage2;
    private NetworkNodeContainer storage2Container;

    @BeforeEach
    void setUp() {
        sut = new StorageNetworkComponentImpl(NetworkTestFixtures.STORAGE_CHANNEL_TYPE_REGISTRY);

        final StorageDiskProviderImpl diskProvider1 = new StorageDiskProviderImpl();
        diskProvider1.setInSlot(0, new LimitedStorageImpl<>(100));
        storage1 = new DiskDriveNetworkNode(0, 0, NetworkTestFixtures.STORAGE_CHANNEL_TYPE_REGISTRY, 9);
        storage1.setNetwork(new NetworkImpl(NetworkTestFixtures.NETWORK_COMPONENT_MAP_FACTORY));
        storage1.setListener(mock(DiskDriveListener.class));
        storage1.setDiskProvider(diskProvider1);
        storage1.setActive(true);
        storage1Container = () -> storage1;

        final StorageDiskProviderImpl diskProvider2 = new StorageDiskProviderImpl();
        diskProvider2.setInSlot(0, new LimitedStorageImpl<>(100));
        storage2 = new DiskDriveNetworkNode(0, 0, NetworkTestFixtures.STORAGE_CHANNEL_TYPE_REGISTRY, 9);
        storage2.setNetwork(new NetworkImpl(NetworkTestFixtures.NETWORK_COMPONENT_MAP_FACTORY));
        storage2.setListener(mock(DiskDriveListener.class));
        storage2.setDiskProvider(diskProvider2);
        storage2.setActive(true);
        storage2Container = () -> storage2;
    }

    @Test
    void testInitialState() {
        // Act
        final Collection<ResourceAmount<String>> resources = sut
            .getStorageChannel(NetworkTestFixtures.STORAGE_CHANNEL_TYPE)
            .getAll();

        // Assert
        assertThat(resources).isEmpty();
    }

    @Test
    void shouldAddStorageSourceContainer() {
        // Arrange
        final StorageChannel<String> storageChannel = sut.getStorageChannel(NetworkTestFixtures.STORAGE_CHANNEL_TYPE);

        // Act
        final long insertedPre = storageChannel.insert("A", 10, Action.EXECUTE, EmptyActor.INSTANCE);
        sut.onContainerAdded(storage1Container);
        final long insertedPost = storageChannel.insert("A", 10, Action.EXECUTE, EmptyActor.INSTANCE);

        // Assert
        assertThat(insertedPre).isZero();
        assertThat(insertedPost).isEqualTo(10);
        assertThat(storageChannel.getAll()).isNotEmpty();
    }

    @Test
    void shouldRemoveStorageSourceContainer() {
        // Arrange
        final StorageChannel<String> storageChannel = sut.getStorageChannel(NetworkTestFixtures.STORAGE_CHANNEL_TYPE);

        sut.onContainerAdded(storage1Container);
        sut.onContainerAdded(storage2Container);

        // Ensure that we fill our 2 containers.
        storageChannel.insert("A", 200, Action.EXECUTE, EmptyActor.INSTANCE);

        // Act
        final Collection<ResourceAmount<String>> resourcesPre = new HashSet<>(storageChannel.getAll());
        sut.onContainerRemoved(storage1Container);
        sut.onContainerRemoved(storage2Container);
        final Collection<ResourceAmount<String>> resourcesPost = storageChannel.getAll();

        // Assert
        assertThat(resourcesPre).isNotEmpty();
        assertThat(resourcesPost).isEmpty();
    }
}
