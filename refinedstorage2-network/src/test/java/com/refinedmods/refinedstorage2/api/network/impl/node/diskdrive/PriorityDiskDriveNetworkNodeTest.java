package com.refinedmods.refinedstorage2.api.network.impl.node.diskdrive;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.storage.EmptyActor;
import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannel;
import com.refinedmods.refinedstorage2.api.storage.limited.LimitedStorageImpl;
import com.refinedmods.refinedstorage2.network.test.AddNetworkNode;
import com.refinedmods.refinedstorage2.network.test.InjectNetworkStorageChannel;
import com.refinedmods.refinedstorage2.network.test.NetworkTest;
import com.refinedmods.refinedstorage2.network.test.SetupNetwork;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

@NetworkTest
@SetupNetwork
class PriorityDiskDriveNetworkNodeTest {
    @AddNetworkNode
    DiskDriveNetworkNode a;

    @AddNetworkNode
    DiskDriveNetworkNode b;

    StorageDiskProviderImpl provider;

    @BeforeEach
    void setUp() {
        provider = new StorageDiskProviderImpl();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldRespectPriority(
        final boolean diskDriveAHasPriority,
        @InjectNetworkStorageChannel final StorageChannel<String> networkStorage
    ) {
        // Arrange
        final Storage<String> storage1 = new LimitedStorageImpl<>(100);
        final StorageDiskProviderImpl provider1 = new StorageDiskProviderImpl();
        provider1.setInSlot(1, storage1);
        a.setDiskProvider(provider1);
        a.setActive(true);

        final Storage<String> storage2 = new LimitedStorageImpl<>(100);
        final StorageDiskProviderImpl provider2 = new StorageDiskProviderImpl();
        provider2.setInSlot(1, storage2);
        b.setDiskProvider(provider2);
        b.setActive(true);

        if (diskDriveAHasPriority) {
            a.setPriority(5);
            b.setPriority(2);
        } else {
            a.setPriority(2);
            b.setPriority(5);
        }

        // Act
        networkStorage.insert("A", 1, Action.EXECUTE, EmptyActor.INSTANCE);

        // Assert
        if (diskDriveAHasPriority) {
            assertThat(storage1.getAll()).isNotEmpty();
            assertThat(storage2.getAll()).isEmpty();
        } else {
            assertThat(storage1.getAll()).isEmpty();
            assertThat(storage2.getAll()).isNotEmpty();
        }
    }
}
