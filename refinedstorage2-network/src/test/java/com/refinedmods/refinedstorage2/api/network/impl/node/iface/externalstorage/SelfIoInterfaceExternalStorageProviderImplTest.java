package com.refinedmods.refinedstorage2.api.network.impl.node.iface.externalstorage;

import com.refinedmods.refinedstorage2.api.network.impl.node.externalstorage.ExternalStorageNetworkNode;
import com.refinedmods.refinedstorage2.api.network.impl.node.externalstorage.ExternalStorageProviderFactoryImpl;
import com.refinedmods.refinedstorage2.api.network.impl.node.iface.InterfaceExportStateImpl;
import com.refinedmods.refinedstorage2.api.network.impl.node.iface.InterfaceNetworkNode;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
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
class SelfIoInterfaceExternalStorageProviderImplTest {
    @AddNetworkNode
    InterfaceNetworkNode<String> iface;
    InterfaceExportStateImpl exportState;
    @AddNetworkNode
    ExternalStorageNetworkNode connection;

    @BeforeEach
    void setUp() {
        exportState = new InterfaceExportStateImpl(2);
        iface.setExportState(exportState);
        iface.setTransferQuota(100);
        connection.initialize(new ExternalStorageProviderFactoryImpl(
            new InterfaceExternalStorageProviderImpl<>(iface)
        ));
    }

    // We don't allow self-insertions and self-extractions for the same reasons mentioned in
    // IoLoopInterfaceExternalStorageProviderImplTest.
    @Test
    void shouldNotAllowSelfInsertionOrSelfExtraction(
        @InjectNetworkStorageChannel final StorageChannel<String> networkStorage
    ) {
        // Arrange
        // this would try to do a self-insert as it's an unwanted resource.
        exportState.setCurrentlyExported(0, "B", 15);
        // this would try to do a self-extract because we have the resource.
        exportState.setRequestedResource(1, "B", 1);

        // Act
        iface.doWork();
        connection.detectChanges();

        // Assert
        assertThat(exportState.getCurrentlyExportedResource(0)).isEqualTo("B");
        assertThat(exportState.getCurrentlyExportedResourceAmount(0)).isEqualTo(15);

        assertThat(exportState.getCurrentlyExportedResource(1)).isNull();
        assertThat(exportState.getCurrentlyExportedResourceAmount(1)).isZero();

        assertThat(networkStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount<>("B", 15)
        );
    }
}
