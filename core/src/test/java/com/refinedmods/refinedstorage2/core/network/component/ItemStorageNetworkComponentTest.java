package com.refinedmods.refinedstorage2.core.network.component;

import com.refinedmods.refinedstorage2.core.Rs2Test;
import com.refinedmods.refinedstorage2.core.item.ItemStubs;
import com.refinedmods.refinedstorage2.core.item.Rs2ItemStack;
import com.refinedmods.refinedstorage2.core.network.NetworkUtil;
import com.refinedmods.refinedstorage2.core.network.host.FakeNetworkNodeHost;
import com.refinedmods.refinedstorage2.core.network.host.NetworkNodeHost;
import com.refinedmods.refinedstorage2.core.network.node.diskdrive.DiskDriveNetworkNodeWrapper;
import com.refinedmods.refinedstorage2.core.storage.disk.ItemDiskStorage;
import com.refinedmods.refinedstorage2.core.util.Action;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.refinedmods.refinedstorage2.core.util.ItemStackAssertions.assertItemStack;
import static com.refinedmods.refinedstorage2.core.util.ItemStackAssertions.assertItemStackListContents;
import static org.assertj.core.api.Assertions.assertThat;

@Rs2Test
class ItemStorageNetworkComponentTest {
    private ItemStorageNetworkComponent sut;
    private DiskDriveNetworkNodeWrapper diskDrive;
    private NetworkNodeHost<DiskDriveNetworkNodeWrapper> diskDriveHost;

    @BeforeEach
    void setUp() {
        sut = new ItemStorageNetworkComponent();

        diskDrive = DiskDriveNetworkNodeWrapper.create();
        diskDrive.getFakeStorageDiskProviderManager().setDisk(0, new ItemDiskStorage(100));
        diskDrive.setNetwork(NetworkUtil.createWithCreativeEnergySource());

        diskDriveHost = new FakeNetworkNodeHost<>(diskDrive);
    }

    @Test
    void Test_adding_node_should_invalidate_storage() {
        // Arrange
        sut.onHostAdded(diskDriveHost);

        // Act
        Optional<Rs2ItemStack> remainderBeforeRemoval = sut.getStorageChannel().insert(new Rs2ItemStack(ItemStubs.DIRT), 10, Action.EXECUTE);
        sut.onHostRemoved(diskDriveHost);
        Optional<Rs2ItemStack> remainderAfterRemoval = sut.getStorageChannel().insert(new Rs2ItemStack(ItemStubs.DIRT), 10, Action.EXECUTE);

        // Assert
        assertThat(remainderBeforeRemoval).isEmpty();
        assertThat(remainderAfterRemoval).isPresent();
        assertItemStackListContents(diskDrive.getStacks(), new Rs2ItemStack(ItemStubs.DIRT, 10));
    }

    @Test
    void Test_inserting_items() {
        // Arrange
        sut.onHostAdded(diskDriveHost);

        // Act
        Optional<Rs2ItemStack> remainder = sut.getStorageChannel().insert(new Rs2ItemStack(ItemStubs.DIRT), 4, Action.EXECUTE);

        // Assert
        assertThat(remainder).isEmpty();
        assertItemStackListContents(sut.getStorageChannel().getStacks(), new Rs2ItemStack(ItemStubs.DIRT, 4));
        assertThat(sut.getStorageChannel().getStored()).isEqualTo(4);
    }

    @Test
    void Test_extracting_items() {
        // Arrange
        sut.onHostAdded(diskDriveHost);

        sut.getStorageChannel().insert(new Rs2ItemStack(ItemStubs.DIRT), 10, Action.EXECUTE);

        // Act
        Optional<Rs2ItemStack> extracted = sut.getStorageChannel().extract(new Rs2ItemStack(ItemStubs.DIRT), 4, Action.EXECUTE);

        // Assert
        assertThat(extracted).isPresent();
        assertItemStack(extracted.get(), new Rs2ItemStack(ItemStubs.DIRT, 4));
        assertItemStackListContents(sut.getStorageChannel().getStacks(), new Rs2ItemStack(ItemStubs.DIRT, 6));
        assertThat(sut.getStorageChannel().getStored()).isEqualTo(6);
    }
}
