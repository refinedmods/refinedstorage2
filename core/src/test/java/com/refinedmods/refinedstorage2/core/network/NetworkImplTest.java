package com.refinedmods.refinedstorage2.core.network;

import java.util.Optional;
import java.util.UUID;

import com.refinedmods.refinedstorage2.core.RefinedStorage2Test;
import com.refinedmods.refinedstorage2.core.adapter.FakeWorld;
import com.refinedmods.refinedstorage2.core.network.node.NetworkNodeReference;
import com.refinedmods.refinedstorage2.core.network.node.StubNetworkNodeReference;
import com.refinedmods.refinedstorage2.core.network.node.diskdrive.DiskDriveNetworkNode;
import com.refinedmods.refinedstorage2.core.network.node.diskdrive.FakeStorageDiskProviderManager;
import com.refinedmods.refinedstorage2.core.storage.disk.ItemDiskStorage;
import com.refinedmods.refinedstorage2.core.util.Action;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.Test;

import static com.refinedmods.refinedstorage2.core.util.ItemStackAssertions.assertItemStackListContents;
import static org.assertj.core.api.Assertions.assertThat;

@RefinedStorage2Test
class NetworkImplTest {
    @Test
    void Test_node_change_should_rebuild_storage_sources() {
        // Arrange
        FakeStorageDiskProviderManager diskProviderManager = new FakeStorageDiskProviderManager();

        NetworkNodeReference badRef = new StubNetworkNodeReference(null);

        DiskDriveNetworkNode diskDrive = new DiskDriveNetworkNode(new FakeWorld(), BlockPos.ORIGIN, null, diskProviderManager, diskProviderManager);
        diskProviderManager.setDiskDrive(diskDrive);
        diskProviderManager.setDisk(0, new ItemDiskStorage(100));
        NetworkNodeReference diskDriveRef = new StubNetworkNodeReference(diskDrive);

        Network network = new NetworkImpl(UUID.randomUUID());
        network.getNodeReferences().add(badRef);
        network.getNodeReferences().add(diskDriveRef);

        // Act
        network.onNodesChanged();

        Optional<ItemStack> remainder1 = network.getItemStorageChannel().insert(new ItemStack(Items.DIRT), 10, Action.EXECUTE);

        network.getNodeReferences().remove(diskDriveRef);
        network.onNodesChanged();

        Optional<ItemStack> remainder2 = network.getItemStorageChannel().insert(new ItemStack(Items.DIRT), 10, Action.EXECUTE);

        // Assert
        assertThat(remainder1).isEmpty();
        assertThat(remainder2).isPresent();
        assertItemStackListContents(diskDrive.getStacks(), new ItemStack(Items.DIRT, 10));
    }
}
