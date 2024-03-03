package com.refinedmods.refinedstorage2.api.network.impl.node.iface.externalstorage;

import com.refinedmods.refinedstorage2.api.network.impl.node.externalstorage.ExternalStorageNetworkNode;
import com.refinedmods.refinedstorage2.api.network.impl.node.externalstorage.ExternalStorageProviderFactoryImpl;
import com.refinedmods.refinedstorage2.api.network.impl.node.iface.InterfaceExportStateImpl;
import com.refinedmods.refinedstorage2.api.network.impl.node.iface.InterfaceNetworkNode;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.storage.ResourceTemplate;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannel;
import com.refinedmods.refinedstorage2.network.test.AddNetworkNode;
import com.refinedmods.refinedstorage2.network.test.InjectNetworkStorageChannel;
import com.refinedmods.refinedstorage2.network.test.NetworkTest;
import com.refinedmods.refinedstorage2.network.test.NetworkTestFixtures;
import com.refinedmods.refinedstorage2.network.test.SetupNetwork;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.refinedmods.refinedstorage2.network.test.TestResourceKey.B;
import static org.assertj.core.api.Assertions.assertThat;

@NetworkTest
@SetupNetwork
class SelfIoInterfaceExternalStorageProviderImplTest {
    @AddNetworkNode
    InterfaceNetworkNode iface;
    InterfaceExportStateImpl exportState;
    @AddNetworkNode
    ExternalStorageNetworkNode connection;

    @BeforeEach
    void setUp() {
        exportState = new InterfaceExportStateImpl(2);
        iface.setExportState(exportState);
        iface.setTransferQuotaProvider(resource -> 100);
        connection.initialize(new ExternalStorageProviderFactoryImpl(new InterfaceExternalStorageProviderImpl(
            iface,
            NetworkTestFixtures.STORAGE_CHANNEL_TYPE
        )));
    }

    // We don't allow self-insertions and self-extractions for the same reasons mentioned in
    // IoLoopInterfaceExternalStorageProviderImplTest.
    @Test
    void shouldNotAllowSelfInsertionOrSelfExtraction(
        @InjectNetworkStorageChannel final StorageChannel networkStorage
    ) {
        // Arrange
        // this would try to do a self-insert as it's an unwanted resource.
        exportState.setCurrentlyExported(0, B, 15);
        // this would try to do a self-extract because we have the resource.
        exportState.setRequestedResource(1, B, 1);

        // Act
        iface.doWork();
        connection.detectChanges();

        // Assert
        assertThat(exportState.getExportedResource(0)).usingRecursiveComparison().isEqualTo(
            new ResourceTemplate(B, NetworkTestFixtures.STORAGE_CHANNEL_TYPE)
        );
        assertThat(exportState.getExportedAmount(0)).isEqualTo(15);

        assertThat(exportState.getExportedResource(1)).isNull();
        assertThat(exportState.getExportedAmount(1)).isZero();

        assertThat(networkStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(B, 15)
        );
    }
}
