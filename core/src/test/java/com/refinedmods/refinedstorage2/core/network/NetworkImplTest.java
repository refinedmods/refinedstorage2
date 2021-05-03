package com.refinedmods.refinedstorage2.core.network;

import com.refinedmods.refinedstorage2.core.Rs2Test;
import com.refinedmods.refinedstorage2.core.adapter.FakeRs2World;
import com.refinedmods.refinedstorage2.core.item.ItemStubs;
import com.refinedmods.refinedstorage2.core.item.Rs2ItemStack;
import com.refinedmods.refinedstorage2.core.network.node.NetworkNodeReference;
import com.refinedmods.refinedstorage2.core.network.node.StubNetworkNodeReference;
import com.refinedmods.refinedstorage2.core.network.node.controller.ControllerNetworkNode;
import com.refinedmods.refinedstorage2.core.network.node.controller.ControllerType;
import com.refinedmods.refinedstorage2.core.network.node.diskdrive.DiskDriveNetworkNodeWrapper;
import com.refinedmods.refinedstorage2.core.storage.disk.ItemDiskStorage;
import com.refinedmods.refinedstorage2.core.util.Action;
import com.refinedmods.refinedstorage2.core.util.Position;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import static com.refinedmods.refinedstorage2.core.util.ItemStackAssertions.assertItemStack;
import static com.refinedmods.refinedstorage2.core.util.ItemStackAssertions.assertItemStackListContents;
import static org.assertj.core.api.Assertions.assertThat;

@Rs2Test
class NetworkImplTest {
    @Test
    void Test_node_change_should_rebuild_storage_sources() {
        // Arrange
        DiskDriveNetworkNodeWrapper diskDrive = DiskDriveNetworkNodeWrapper.create();

        Network network = NetworkBuilder.create()
                .nodeRef(new StubNetworkNodeReference(null))
                .node(diskDrive)
                .infiniteEnergy()
                .build();

        diskDrive.getFakeStorageDiskProviderManager().setDisk(0, new ItemDiskStorage(100));

        // Act
        network.onNodesChanged();

        Optional<Rs2ItemStack> remainderBeforeRemoval = network.getItemStorageChannel().insert(new Rs2ItemStack(ItemStubs.DIRT), 10, Action.EXECUTE);

        network.getNodeReferences().remove(new StubNetworkNodeReference(diskDrive));
        network.onNodesChanged();

        Optional<Rs2ItemStack> remainderAfterRemoval = network.getItemStorageChannel().insert(new Rs2ItemStack(ItemStubs.DIRT), 10, Action.EXECUTE);

        // Assert
        assertThat(remainderBeforeRemoval).isEmpty();
        assertThat(remainderAfterRemoval).isPresent();
        assertItemStackListContents(diskDrive.getStacks(), new Rs2ItemStack(ItemStubs.DIRT, 10));
    }

    @Test
    void Test_node_change_should_rebuild_energy_storage_sources() {
        // Arrange
        NetworkNodeReference badRef = new StubNetworkNodeReference(null);
        NetworkNodeReference controllerRef = new StubNetworkNodeReference(new ControllerNetworkNode(new FakeRs2World(), Position.ORIGIN, null, 5, ControllerType.NORMAL));

        Network network = new NetworkImpl(UUID.randomUUID());
        network.getNodeReferences().add(badRef);
        network.getNodeReferences().add(controllerRef);

        // Act
        network.onNodesChanged();

        long capacityBeforeRemove = network.getEnergyStorage().getCapacity();

        network.getNodeReferences().remove(controllerRef);
        network.onNodesChanged();

        long capacityAfterRemove = network.getEnergyStorage().getCapacity();

        // Assert
        assertThat(capacityBeforeRemove).isEqualTo(5);
        assertThat(capacityAfterRemove).isZero();
    }

    @Test
    void Test_receiving_energy() {
        // Arrange
        ControllerNetworkNode controller = new ControllerNetworkNode(new FakeRs2World(), Position.ORIGIN, null, 20, ControllerType.NORMAL);
        NetworkNodeReference controllerRef = new StubNetworkNodeReference(controller);

        Network network = new NetworkImpl(UUID.randomUUID());
        network.getNodeReferences().add(controllerRef);
        network.onNodesChanged();

        // Act
        long remainder = network.getEnergyStorage().receive(10, Action.EXECUTE);

        // Assert
        assertThat(remainder).isZero();
        assertThat(network.getEnergyStorage().getStored()).isEqualTo(10);
        assertThat(network.getEnergyStorage().getCapacity()).isEqualTo(20);
    }

    @Test
    void Test_receiving_energy_when_node_is_gone() {
        // Arrange
        ControllerNetworkNode controller = new ControllerNetworkNode(new FakeRs2World(), Position.ORIGIN, null, 20, ControllerType.NORMAL);
        StubNetworkNodeReference controllerRef = new StubNetworkNodeReference(controller);

        Network network = new NetworkImpl(UUID.randomUUID());
        network.getNodeReferences().add(controllerRef);
        network.onNodesChanged();

        // Act
        controllerRef.setNode(null);

        long remainder = network.getEnergyStorage().receive(10, Action.EXECUTE);

        // Assert
        assertThat(remainder).isEqualTo(10);
        assertThat(network.getEnergyStorage().getStored()).isZero();
        assertThat(network.getEnergyStorage().getCapacity()).isZero();
    }

    @Test
    void Test_extracting_energy() {
        // Arrange
        ControllerNetworkNode controller = new ControllerNetworkNode(new FakeRs2World(), Position.ORIGIN, null, 20, ControllerType.NORMAL);
        NetworkNodeReference controllerRef = new StubNetworkNodeReference(controller);

        Network network = new NetworkImpl(UUID.randomUUID());
        network.getNodeReferences().add(controllerRef);
        network.onNodesChanged();

        network.getEnergyStorage().receive(15, Action.EXECUTE);

        // Act
        long extracted = network.getEnergyStorage().extract(5, Action.EXECUTE);

        // Assert
        assertThat(extracted).isEqualTo(5);
        assertThat(network.getEnergyStorage().getStored()).isEqualTo(10);
        assertThat(network.getEnergyStorage().getCapacity()).isEqualTo(20);
    }

    @Test
    void Test_extracting_energy_when_node_is_gone() {
        // Arrange
        ControllerNetworkNode controller = new ControllerNetworkNode(new FakeRs2World(), Position.ORIGIN, null, 20, ControllerType.NORMAL);
        StubNetworkNodeReference controllerRef = new StubNetworkNodeReference(controller);

        Network network = new NetworkImpl(UUID.randomUUID());
        network.getNodeReferences().add(controllerRef);
        network.onNodesChanged();

        network.getEnergyStorage().receive(15, Action.EXECUTE);

        // Act
        controllerRef.setNode(null);

        long extracted = network.getEnergyStorage().extract(5, Action.EXECUTE);

        // Assert
        assertThat(extracted).isZero();
        assertThat(network.getEnergyStorage().getStored()).isZero();
        assertThat(network.getEnergyStorage().getCapacity()).isZero();
    }

    @Test
    void Test_inserting_items() {
        // Arrange
        DiskDriveNetworkNodeWrapper diskDrive = DiskDriveNetworkNodeWrapper.create();
        StubNetworkNodeReference diskDriveRef = new StubNetworkNodeReference(diskDrive);

        Network network = NetworkBuilder.create().infiniteEnergy().nodeRef(diskDriveRef).build();

        diskDrive.getFakeStorageDiskProviderManager().setDisk(0, new ItemDiskStorage(10));

        // Act
        Optional<Rs2ItemStack> remainder = network.getItemStorageChannel().insert(new Rs2ItemStack(ItemStubs.DIRT), 4, Action.EXECUTE);

        // Assert
        assertThat(remainder).isEmpty();
        assertItemStackListContents(network.getItemStorageChannel().getStacks(), new Rs2ItemStack(ItemStubs.DIRT, 4));
        assertThat(network.getItemStorageChannel().getStored()).isEqualTo(4);
    }

    @Test
    void Test_inserting_items_when_node_is_gone() {
        // Arrange
        DiskDriveNetworkNodeWrapper diskDrive = DiskDriveNetworkNodeWrapper.create();
        StubNetworkNodeReference diskDriveRef = new StubNetworkNodeReference(diskDrive);

        Network network = NetworkBuilder.create().infiniteEnergy().nodeRef(diskDriveRef).build();

        diskDrive.getFakeStorageDiskProviderManager().setDisk(0, new ItemDiskStorage(10));

        // Act
        diskDriveRef.setNode(null);

        Optional<Rs2ItemStack> remainder = network.getItemStorageChannel().insert(new Rs2ItemStack(ItemStubs.DIRT), 4, Action.EXECUTE);

        // Assert
        assertThat(remainder).isPresent();
        assertItemStack(remainder.get(), new Rs2ItemStack(ItemStubs.DIRT, 4));
        assertItemStackListContents(network.getItemStorageChannel().getStacks());
        assertThat(network.getItemStorageChannel().getStored()).isZero();
    }

    @Test
    void Test_extracting_items() {
        // Arrange
        DiskDriveNetworkNodeWrapper diskDrive = DiskDriveNetworkNodeWrapper.create();
        StubNetworkNodeReference diskDriveRef = new StubNetworkNodeReference(diskDrive);

        Network network = NetworkBuilder.create().infiniteEnergy().nodeRef(diskDriveRef).build();

        diskDrive.getFakeStorageDiskProviderManager().setDisk(0, new ItemDiskStorage(10));

        network.getItemStorageChannel().insert(new Rs2ItemStack(ItemStubs.DIRT), 10, Action.EXECUTE);

        // Act
        Optional<Rs2ItemStack> extracted = network.getItemStorageChannel().extract(new Rs2ItemStack(ItemStubs.DIRT), 4, Action.EXECUTE);

        // Assert
        assertThat(extracted).isPresent();
        assertItemStack(extracted.get(), new Rs2ItemStack(ItemStubs.DIRT, 4));
        assertItemStackListContents(network.getItemStorageChannel().getStacks(), new Rs2ItemStack(ItemStubs.DIRT, 6));
        assertThat(network.getItemStorageChannel().getStored()).isEqualTo(6);
    }

    @Test
    void Test_extracting_items_when_node_is_gone() {
        // Arrange
        DiskDriveNetworkNodeWrapper diskDrive = DiskDriveNetworkNodeWrapper.create();
        StubNetworkNodeReference diskDriveRef = new StubNetworkNodeReference(diskDrive);

        Network network = NetworkBuilder.create().infiniteEnergy().nodeRef(diskDriveRef).build();

        diskDrive.getFakeStorageDiskProviderManager().setDisk(0, new ItemDiskStorage(10));

        network.getItemStorageChannel().insert(new Rs2ItemStack(ItemStubs.DIRT), 10, Action.EXECUTE);

        // Act
        diskDriveRef.setNode(null);

        Optional<Rs2ItemStack> extracted = network.getItemStorageChannel().extract(new Rs2ItemStack(ItemStubs.DIRT), 4, Action.EXECUTE);

        // Assert
        assertThat(extracted).isEmpty();
        // Even though the node is gone, we still expect "ghost" items since they are cached.
        assertItemStackListContents(network.getItemStorageChannel().getStacks(), new Rs2ItemStack(ItemStubs.DIRT, 10));
        assertThat(network.getItemStorageChannel().getStored()).isZero();
    }
}
