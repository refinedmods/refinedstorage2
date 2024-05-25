package com.refinedmods.refinedstorage2.network.test;

import com.refinedmods.refinedstorage2.api.network.impl.node.SimpleNetworkNode;
import com.refinedmods.refinedstorage2.api.network.impl.node.controller.ControllerNetworkNode;
import com.refinedmods.refinedstorage2.api.network.impl.node.detector.DetectorNetworkNode;
import com.refinedmods.refinedstorage2.api.network.impl.node.exporter.ExporterNetworkNode;
import com.refinedmods.refinedstorage2.api.network.impl.node.externalstorage.ExternalStorageNetworkNode;
import com.refinedmods.refinedstorage2.api.network.impl.node.grid.GridNetworkNode;
import com.refinedmods.refinedstorage2.api.network.impl.node.iface.InterfaceNetworkNode;
import com.refinedmods.refinedstorage2.api.network.impl.node.importer.ImporterNetworkNode;
import com.refinedmods.refinedstorage2.api.network.impl.node.relay.RelayInputNetworkNode;
import com.refinedmods.refinedstorage2.api.network.impl.node.relay.RelayOutputNetworkNode;
import com.refinedmods.refinedstorage2.api.network.impl.node.storage.StorageNetworkNode;
import com.refinedmods.refinedstorage2.api.network.impl.node.storagetransfer.StorageTransferNetworkNode;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@NetworkTest
@SetupNetwork
class NetworkNodeFactoryTest {
    @AddNetworkNode
    ControllerNetworkNode controller;
    @AddNetworkNode
    ExporterNetworkNode exporter;
    @AddNetworkNode
    GridNetworkNode grid;
    @AddNetworkNode
    ImporterNetworkNode importer;
    @AddNetworkNode
    SimpleNetworkNode simple;
    @AddNetworkNode
    StorageNetworkNode storage;
    @AddNetworkNode
    InterfaceNetworkNode interfaceNode;
    @AddNetworkNode
    ExternalStorageNetworkNode externalStorage;
    @AddNetworkNode
    DetectorNetworkNode detector;
    @AddNetworkNode
    RelayInputNetworkNode relayInput;
    @AddNetworkNode
    RelayOutputNetworkNode relayOutput;
    @AddNetworkNode
    StorageTransferNetworkNode storageTransfer;

    @Test
    void testInitialization() {
        // Assert
        assertThat(this).hasNoNullFieldsOrProperties();
    }
}
