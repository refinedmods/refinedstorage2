package com.refinedmods.refinedstorage2.network.test;

import com.refinedmods.refinedstorage2.api.network.node.SimpleNetworkNode;
import com.refinedmods.refinedstorage2.api.network.node.controller.ControllerNetworkNode;
import com.refinedmods.refinedstorage2.api.network.node.diskdrive.DiskDriveNetworkNode;
import com.refinedmods.refinedstorage2.api.network.node.exporter.ExporterNetworkNode;
import com.refinedmods.refinedstorage2.api.network.node.grid.GridNetworkNode;
import com.refinedmods.refinedstorage2.api.network.node.iface.InterfaceNetworkNode;
import com.refinedmods.refinedstorage2.api.network.node.importer.ImporterNetworkNode;
import com.refinedmods.refinedstorage2.api.network.node.storage.StorageNetworkNode;
import com.refinedmods.refinedstorage2.network.test.nodefactory.ControllerNetworkNodeFactory;
import com.refinedmods.refinedstorage2.network.test.nodefactory.DiskDriveNetworkNodeFactory;
import com.refinedmods.refinedstorage2.network.test.nodefactory.ExporterNetworkNodeFactory;
import com.refinedmods.refinedstorage2.network.test.nodefactory.GridNetworkNodeFactory;
import com.refinedmods.refinedstorage2.network.test.nodefactory.ImporterNetworkNodeFactory;
import com.refinedmods.refinedstorage2.network.test.nodefactory.InterfaceNetworkNodeFactory;
import com.refinedmods.refinedstorage2.network.test.nodefactory.SimpleNetworkNodeFactory;
import com.refinedmods.refinedstorage2.network.test.nodefactory.StorageNetworkNodeFactory;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.extension.ExtendWith;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@ExtendWith(NetworkTestExtension.class)
@RegisterNetworkNode(value = ControllerNetworkNodeFactory.class, clazz = ControllerNetworkNode.class)
@RegisterNetworkNode(value = DiskDriveNetworkNodeFactory.class, clazz = DiskDriveNetworkNode.class)
@RegisterNetworkNode(value = ExporterNetworkNodeFactory.class, clazz = ExporterNetworkNode.class)
@RegisterNetworkNode(value = GridNetworkNodeFactory.class, clazz = GridNetworkNode.class)
@RegisterNetworkNode(value = ImporterNetworkNodeFactory.class, clazz = ImporterNetworkNode.class)
@RegisterNetworkNode(value = SimpleNetworkNodeFactory.class, clazz = SimpleNetworkNode.class)
@RegisterNetworkNode(value = StorageNetworkNodeFactory.class, clazz = StorageNetworkNode.class)
@RegisterNetworkNode(value = InterfaceNetworkNodeFactory.class, clazz = InterfaceNetworkNode.class)
public @interface NetworkTest {
}
