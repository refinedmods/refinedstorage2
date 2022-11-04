package com.refinedmods.refinedstorage2.api.network.node.iface;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.network.node.externalstorage.ExternalStorageNetworkNode;
import com.refinedmods.refinedstorage2.api.network.node.externalstorage.ExternalStorageProviderFactoryImpl;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.storage.EmptyActor;
import com.refinedmods.refinedstorage2.api.storage.InMemoryStorageImpl;
import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannel;
import com.refinedmods.refinedstorage2.network.test.AddNetworkNode;
import com.refinedmods.refinedstorage2.network.test.InjectNetworkStorageChannel;
import com.refinedmods.refinedstorage2.network.test.NetworkTest;
import com.refinedmods.refinedstorage2.network.test.SetupNetwork;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@NetworkTest
@SetupNetwork
public class PriorityInterfaceNetworkNodeTest {
    Storage<String> otherStorage;

    @AddNetworkNode
    InterfaceNetworkNode<String> first;
    InterfaceExportStateImpl firstState;
    @AddNetworkNode
    ExternalStorageNetworkNode firstConnection;

    @AddNetworkNode
    InterfaceNetworkNode<String> second;
    InterfaceExportStateImpl secondState;
    @AddNetworkNode
    ExternalStorageNetworkNode secondConnection;

    @BeforeEach
    void setUp(@InjectNetworkStorageChannel final StorageChannel<String> networkStorage) {
        firstState = new InterfaceExportStateImpl(2);
        firstState.setRequestedResource(1, "A", 10);
        first.setExportState(firstState);
        first.setTransferQuota(100);
        firstConnection.initialize(new ExternalStorageProviderFactoryImpl(
            new InterfaceExternalStorageProvider<>(() -> firstState)
        ));

        secondState = new InterfaceExportStateImpl(2);
        secondState.setRequestedResource(1, "A", 10);
        second.setExportState(secondState);
        second.setTransferQuota(100);
        secondConnection.initialize(new ExternalStorageProviderFactoryImpl(
            new InterfaceExternalStorageProvider<>(() -> secondState)
        ));

        otherStorage = new InMemoryStorageImpl<>();
        otherStorage.insert("A", 10, Action.EXECUTE, EmptyActor.INSTANCE);
        networkStorage.addSource(otherStorage);
    }

    @Test
    void shouldNotStealItemsFromEachOther(
        @InjectNetworkStorageChannel final StorageChannel<String> networkStorage
    ) {
        // Act & assert
        first.doWork();
        firstConnection.detectChanges();
        assertThat(otherStorage.getAll()).isEmpty();
        assertThat(firstState.getCurrentlyExportedResource(0)).isNull();
        assertThat(firstState.getCurrentlyExportedResource(1)).isEqualTo("A");
        assertThat(firstState.getCurrentlyExportedResourceAmount(1)).isEqualTo(10);
        assertThat(secondState.getCurrentlyExportedResource(0)).isNull();
        assertThat(secondState.getCurrentlyExportedResource(1)).isNull();
        assertThat(secondState.getCurrentlyExportedResourceAmount(1)).isZero();
        assertThat(networkStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount<>("A", 10)
        );

        second.doWork();
        assertThat(firstState.getCurrentlyExportedResource(0)).isNull();
        assertThat(firstState.getCurrentlyExportedResource(1)).isEqualTo("A");
        assertThat(firstState.getCurrentlyExportedResourceAmount(1)).isEqualTo(10);
        assertThat(secondState.getCurrentlyExportedResource(0)).isNull();
        assertThat(secondState.getCurrentlyExportedResource(1)).isNull();
        assertThat(secondState.getCurrentlyExportedResourceAmount(1)).isZero();
    }
}
