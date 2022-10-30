package com.refinedmods.refinedstorage2.network.test;

import com.refinedmods.refinedstorage2.api.network.node.SimpleNetworkNode;
import com.refinedmods.refinedstorage2.api.network.node.controller.ControllerNetworkNode;
import com.refinedmods.refinedstorage2.api.network.node.diskdrive.DiskDriveNetworkNode;
import com.refinedmods.refinedstorage2.api.network.node.exporter.ExporterNetworkNode;
import com.refinedmods.refinedstorage2.api.network.node.externalstorage.ExternalStorageNetworkNode;
import com.refinedmods.refinedstorage2.api.network.node.grid.GridNetworkNode;
import com.refinedmods.refinedstorage2.api.network.node.iface.InterfaceNetworkNode;
import com.refinedmods.refinedstorage2.api.network.node.importer.ImporterNetworkNode;
import com.refinedmods.refinedstorage2.api.network.node.storage.StorageNetworkNode;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@NetworkTest
@SetupNetwork
class NetworkNodeFactoryTest {
    @AddNetworkNode
    ControllerNetworkNode controller;
    @AddNetworkNode
    DiskDriveNetworkNode diskDrive;
    @AddNetworkNode
    ExporterNetworkNode exporter;
    @AddNetworkNode
    GridNetworkNode<String> grid;
    @AddNetworkNode
    ImporterNetworkNode importer;
    @AddNetworkNode
    SimpleNetworkNode simple;
    @AddNetworkNode
    StorageNetworkNode<String> storage;
    @AddNetworkNode
    InterfaceNetworkNode<String> interfaceNode;
    @AddNetworkNode
    ExternalStorageNetworkNode externalStorage;

    @Test
    void testInitialization() {
        // Assert
        assertThat(controller).isNotNull();
        assertThat(diskDrive).isNotNull();
        assertThat(exporter).isNotNull();
        assertThat(grid).isNotNull();
        assertThat(importer).isNotNull();
        assertThat(simple).isNotNull();
        assertThat(storage).isNotNull();
        assertThat(interfaceNode).isNotNull();
        assertThat(externalStorage).isNotNull();
    }
}
