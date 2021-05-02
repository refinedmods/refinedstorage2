package com.refinedmods.refinedstorage2.core.network;

import com.refinedmods.refinedstorage2.core.Rs2Test;
import com.refinedmods.refinedstorage2.core.adapter.FakeRs2World;
import com.refinedmods.refinedstorage2.core.item.ItemStubs;
import com.refinedmods.refinedstorage2.core.item.Rs2ItemStack;
import com.refinedmods.refinedstorage2.core.network.node.NetworkNodeReference;
import com.refinedmods.refinedstorage2.core.network.node.StubNetworkNodeReference;
import com.refinedmods.refinedstorage2.core.network.node.controller.ControllerNetworkNode;
import com.refinedmods.refinedstorage2.core.network.node.diskdrive.DiskDriveNetworkNodeWrapper;
import com.refinedmods.refinedstorage2.core.storage.disk.ItemDiskStorage;
import com.refinedmods.refinedstorage2.core.util.Action;
import com.refinedmods.refinedstorage2.core.util.Position;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

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
        NetworkNodeReference controllerRef = new StubNetworkNodeReference(new ControllerNetworkNode(new FakeRs2World(), Position.ORIGIN, null, 5));

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
}
