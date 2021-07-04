package com.refinedmods.refinedstorage2.core.network.node.diskdrive;

import com.refinedmods.refinedstorage2.core.Rs2Test;
import com.refinedmods.refinedstorage2.core.item.ItemStubs;
import com.refinedmods.refinedstorage2.core.item.Rs2ItemStack;
import com.refinedmods.refinedstorage2.core.network.Network;
import com.refinedmods.refinedstorage2.core.network.NetworkUtil;
import com.refinedmods.refinedstorage2.core.network.component.ItemStorageNetworkComponent;
import com.refinedmods.refinedstorage2.core.network.node.RedstoneMode;
import com.refinedmods.refinedstorage2.core.network.node.container.FakeNetworkNodeContainer;
import com.refinedmods.refinedstorage2.core.storage.AccessMode;
import com.refinedmods.refinedstorage2.core.storage.disk.DiskState;
import com.refinedmods.refinedstorage2.core.storage.disk.ItemDiskStorage;
import com.refinedmods.refinedstorage2.core.storage.disk.StorageDisk;
import com.refinedmods.refinedstorage2.core.util.Action;
import com.refinedmods.refinedstorage2.core.util.FilterMode;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static com.refinedmods.refinedstorage2.core.util.ItemStackAssertions.assertItemStack;
import static com.refinedmods.refinedstorage2.core.util.ItemStackAssertions.assertItemStackListContents;
import static org.assertj.core.api.Assertions.assertThat;

@Rs2Test
class DiskDriveNetworkNodeTest {
    private static final long BASE_USAGE = 10;
    private static final long USAGE_PER_DISK = 3;

    private Network network;
    private DiskDriveNetworkNodeWrapper diskDrive;

    @BeforeEach
    void setUp() {
        network = NetworkUtil.createWithCreativeEnergySource();
        diskDrive = DiskDriveNetworkNodeWrapper.create(BASE_USAGE, USAGE_PER_DISK);
        diskDrive.setNetwork(network);
        network.addContainer(new FakeNetworkNodeContainer<>(diskDrive));
    }

    @Test
    void Test_initial_disk_state() {
        // Act
        DiskDriveState states = diskDrive.createState();

        // Assert
        assertThat(states.getStates())
                .hasSize(DiskDriveNetworkNode.DISK_COUNT)
                .allMatch(state -> state == DiskState.NONE);
    }

    @Test
    void Test_state_with_an_empty_disk() {
        // Arrange
        StorageDisk<Rs2ItemStack> storageDisk = new ItemDiskStorage(100);
        storageDisk.insert(new Rs2ItemStack(ItemStubs.DIRT), 74, Action.EXECUTE);

        diskDrive.getFakeStorageDiskProviderManager().setDisk(2, storageDisk);

        // Act
        DiskDriveState state = diskDrive.createState();

        // Assert
        assertThat(state.getState(0)).isEqualTo(DiskState.NONE);
        assertThat(state.getState(1)).isEqualTo(DiskState.NONE);
        assertThat(state.getState(2)).isEqualTo(DiskState.NORMAL);
        assertThat(state.getState(3)).isEqualTo(DiskState.NONE);
        assertThat(state.getState(4)).isEqualTo(DiskState.NONE);
        assertThat(state.getState(5)).isEqualTo(DiskState.NONE);
        assertThat(state.getState(6)).isEqualTo(DiskState.NONE);
        assertThat(state.getState(7)).isEqualTo(DiskState.NONE);
    }

    @Test
    void Test_state_with_a_full_disk() {
        // Arrange
        StorageDisk<Rs2ItemStack> storageDisk = new ItemDiskStorage(10);
        storageDisk.insert(new Rs2ItemStack(ItemStubs.DIRT), 10, Action.EXECUTE);

        diskDrive.getFakeStorageDiskProviderManager().setDisk(7, storageDisk);

        // Act
        DiskDriveState state = diskDrive.createState();

        // Assert
        assertThat(state.getState(0)).isEqualTo(DiskState.NONE);
        assertThat(state.getState(1)).isEqualTo(DiskState.NONE);
        assertThat(state.getState(2)).isEqualTo(DiskState.NONE);
        assertThat(state.getState(3)).isEqualTo(DiskState.NONE);
        assertThat(state.getState(4)).isEqualTo(DiskState.NONE);
        assertThat(state.getState(5)).isEqualTo(DiskState.NONE);
        assertThat(state.getState(6)).isEqualTo(DiskState.NONE);
        assertThat(state.getState(7)).isEqualTo(DiskState.FULL);
    }

    @Test
    void Test_state_with_a_nearly_full_disk() {
        // Arrange
        StorageDisk<Rs2ItemStack> storageDisk = new ItemDiskStorage(100);
        storageDisk.insert(new Rs2ItemStack(ItemStubs.DIRT), 75, Action.EXECUTE);

        diskDrive.getFakeStorageDiskProviderManager().setDisk(3, storageDisk);

        // Act
        DiskDriveState state = diskDrive.createState();

        // Assert
        assertThat(state.getState(0)).isEqualTo(DiskState.NONE);
        assertThat(state.getState(1)).isEqualTo(DiskState.NONE);
        assertThat(state.getState(2)).isEqualTo(DiskState.NONE);
        assertThat(state.getState(3)).isEqualTo(DiskState.NEAR_CAPACITY);
        assertThat(state.getState(4)).isEqualTo(DiskState.NONE);
        assertThat(state.getState(5)).isEqualTo(DiskState.NONE);
        assertThat(state.getState(6)).isEqualTo(DiskState.NONE);
        assertThat(state.getState(7)).isEqualTo(DiskState.NONE);
    }


    @Test
    void Test_state_when_inactive() {
        // Arrange
        StorageDisk<Rs2ItemStack> storageDisk = new ItemDiskStorage(100);
        storageDisk.insert(new Rs2ItemStack(ItemStubs.DIRT), 75, Action.EXECUTE);

        diskDrive.getFakeStorageDiskProviderManager().setDisk(3, storageDisk);
        diskDrive.setRedstoneMode(RedstoneMode.HIGH);

        // Act
        DiskDriveState state = diskDrive.createState();

        // Assert
        assertThat(state.getState(0)).isEqualTo(DiskState.NONE);
        assertThat(state.getState(1)).isEqualTo(DiskState.NONE);
        assertThat(state.getState(2)).isEqualTo(DiskState.NONE);
        assertThat(state.getState(3)).isEqualTo(DiskState.DISCONNECTED);
        assertThat(state.getState(4)).isEqualTo(DiskState.NONE);
        assertThat(state.getState(5)).isEqualTo(DiskState.NONE);
        assertThat(state.getState(6)).isEqualTo(DiskState.NONE);
        assertThat(state.getState(7)).isEqualTo(DiskState.NONE);
    }

    @Test
    void Test_state_should_change_when_removing_disk() {
        // Arrange
        StorageDisk<Rs2ItemStack> storageDisk = new ItemDiskStorage(100);
        storageDisk.insert(new Rs2ItemStack(ItemStubs.DIRT), 74, Action.EXECUTE);

        // Act
        diskDrive.getFakeStorageDiskProviderManager().setDisk(2, storageDisk);
        diskDrive.getFakeStorageDiskProviderManager().setDisk(4, storageDisk);
        DiskDriveState stateBeforeRemoval = diskDrive.createState();

        diskDrive.getFakeStorageDiskProviderManager().removeDiskInSlot(2);
        DiskDriveState stateAfterRemoval = diskDrive.createState();

        // Assert
        assertThat(stateBeforeRemoval.getState(0)).isEqualTo(DiskState.NONE);
        assertThat(stateBeforeRemoval.getState(1)).isEqualTo(DiskState.NONE);
        assertThat(stateBeforeRemoval.getState(2)).isEqualTo(DiskState.NORMAL);
        assertThat(stateBeforeRemoval.getState(3)).isEqualTo(DiskState.NONE);
        assertThat(stateBeforeRemoval.getState(4)).isEqualTo(DiskState.NORMAL);
        assertThat(stateBeforeRemoval.getState(5)).isEqualTo(DiskState.NONE);
        assertThat(stateBeforeRemoval.getState(6)).isEqualTo(DiskState.NONE);
        assertThat(stateBeforeRemoval.getState(7)).isEqualTo(DiskState.NONE);

        assertThat(stateAfterRemoval.getState(0)).isEqualTo(DiskState.NONE);
        assertThat(stateAfterRemoval.getState(1)).isEqualTo(DiskState.NONE);
        assertThat(stateAfterRemoval.getState(2)).isEqualTo(DiskState.NONE);
        assertThat(stateAfterRemoval.getState(3)).isEqualTo(DiskState.NONE);
        assertThat(stateAfterRemoval.getState(4)).isEqualTo(DiskState.NORMAL);
        assertThat(stateAfterRemoval.getState(5)).isEqualTo(DiskState.NONE);
        assertThat(stateAfterRemoval.getState(6)).isEqualTo(DiskState.NONE);
        assertThat(stateAfterRemoval.getState(7)).isEqualTo(DiskState.NONE);
    }

    @Test
    void Test_state_with_a_disk_that_does_not_exist() {
        // Arrange
        diskDrive.getFakeStorageDiskProviderManager().setDisk(3, UUID.randomUUID());

        // Act
        diskDrive.onDiskChanged(3);

        DiskDriveState states = diskDrive.createState();

        // Assert
        assertThat(states.getStates())
                .hasSize(DiskDriveNetworkNode.DISK_COUNT)
                .allMatch(state -> state == DiskState.NONE);
    }

    @Test
    void Test_state_when_changing_an_invalid_slot() {
        // Act
        diskDrive.onDiskChanged(-1);
        diskDrive.onDiskChanged(DiskDriveNetworkNode.DISK_COUNT);

        DiskDriveState states = diskDrive.createState();

        // Assert
        assertThat(states.getStates())
                .hasSize(DiskDriveNetworkNode.DISK_COUNT)
                .allMatch(state -> state == DiskState.NONE);
    }

    @Test
    void Test_inserting() {
        // Arrange
        StorageDisk<Rs2ItemStack> storageDisk1 = new ItemDiskStorage(100);
        diskDrive.getFakeStorageDiskProviderManager().setDisk(1, storageDisk1);

        StorageDisk<Rs2ItemStack> storageDisk2 = new ItemDiskStorage(100);
        diskDrive.getFakeStorageDiskProviderManager().setDisk(2, storageDisk2);

        StorageDisk<Rs2ItemStack> storageDisk3 = new ItemDiskStorage(100);
        diskDrive.getFakeStorageDiskProviderManager().setDisk(3, storageDisk3);

        // Act
        Optional<Rs2ItemStack> remainder1 = diskDrive.insert(new Rs2ItemStack(ItemStubs.DIRT), 150, Action.EXECUTE);
        Optional<Rs2ItemStack> remainder2 = diskDrive.insert(new Rs2ItemStack(ItemStubs.DIRT), 10, Action.EXECUTE);
        Optional<Rs2ItemStack> remainder3 = diskDrive.insert(new Rs2ItemStack(ItemStubs.GLASS), 300, Action.EXECUTE);

        // Assert
        assertThat(remainder1).isEmpty();
        assertThat(remainder2).isEmpty();
        assertThat(remainder3).isPresent();
        assertItemStack(remainder3.get(), new Rs2ItemStack(ItemStubs.GLASS, 160));

        assertItemStackListContents(storageDisk1.getStacks(), new Rs2ItemStack(ItemStubs.DIRT, 100));
        assertItemStackListContents(storageDisk2.getStacks(), new Rs2ItemStack(ItemStubs.DIRT, 60), new Rs2ItemStack(ItemStubs.GLASS, 40));
        assertItemStackListContents(storageDisk3.getStacks(), new Rs2ItemStack(ItemStubs.GLASS, 100));

        assertThat(diskDrive.getStored()).isEqualTo(150 + 10 + 140);
    }

    @Test
    void Test_extracting() {
        // Arrange
        StorageDisk<Rs2ItemStack> storageDisk1 = new ItemDiskStorage(100);
        StorageDisk<Rs2ItemStack> storageDisk2 = new ItemDiskStorage(100);
        StorageDisk<Rs2ItemStack> storageDisk3 = new ItemDiskStorage(100);

        storageDisk1.insert(new Rs2ItemStack(ItemStubs.DIRT), 50, Action.EXECUTE);
        storageDisk1.insert(new Rs2ItemStack(ItemStubs.GLASS), 50, Action.EXECUTE);
        diskDrive.getFakeStorageDiskProviderManager().setDisk(1, storageDisk1);

        storageDisk2.insert(new Rs2ItemStack(ItemStubs.DIRT), 50, Action.EXECUTE);
        storageDisk2.insert(new Rs2ItemStack(ItemStubs.GLASS), 50, Action.EXECUTE);
        diskDrive.getFakeStorageDiskProviderManager().setDisk(2, storageDisk2);

        storageDisk3.insert(new Rs2ItemStack(ItemStubs.SPONGE), 10, Action.EXECUTE);
        diskDrive.getFakeStorageDiskProviderManager().setDisk(3, storageDisk3);

        // Act & assert
        Optional<Rs2ItemStack> stack1 = diskDrive.extract(new Rs2ItemStack(ItemStubs.DIRT), 85, Action.EXECUTE);

        assertThat(stack1).isPresent();
        assertItemStack(stack1.get(), new Rs2ItemStack(ItemStubs.DIRT, 85));

        assertItemStackListContents(storageDisk1.getStacks(), new Rs2ItemStack(ItemStubs.GLASS, 50));
        assertItemStackListContents(storageDisk2.getStacks(), new Rs2ItemStack(ItemStubs.GLASS, 50), new Rs2ItemStack(ItemStubs.DIRT, 15));
        assertItemStackListContents(storageDisk3.getStacks(), new Rs2ItemStack(ItemStubs.SPONGE, 10));

        assertThat(diskDrive.getStored()).isEqualTo(125);
    }

    @Test
    void Test_getting_stacks() {
        // Arrange
        StorageDisk<Rs2ItemStack> storageDisk1 = new ItemDiskStorage(100);
        StorageDisk<Rs2ItemStack> storageDisk2 = new ItemDiskStorage(100);
        StorageDisk<Rs2ItemStack> storageDisk3 = new ItemDiskStorage(100);

        storageDisk1.insert(new Rs2ItemStack(ItemStubs.DIRT), 50, Action.EXECUTE);
        storageDisk1.insert(new Rs2ItemStack(ItemStubs.GLASS), 50, Action.EXECUTE);
        diskDrive.getFakeStorageDiskProviderManager().setDisk(1, storageDisk1);

        storageDisk2.insert(new Rs2ItemStack(ItemStubs.DIRT), 50, Action.EXECUTE);
        storageDisk2.insert(new Rs2ItemStack(ItemStubs.GLASS), 50, Action.EXECUTE);
        diskDrive.getFakeStorageDiskProviderManager().setDisk(2, storageDisk2);

        storageDisk3.insert(new Rs2ItemStack(ItemStubs.SPONGE), 10, Action.EXECUTE);
        diskDrive.getFakeStorageDiskProviderManager().setDisk(3, storageDisk3);

        // Act
        Collection<Rs2ItemStack> stacks = diskDrive.getStacks();

        // Assert
        assertItemStackListContents(stacks, new Rs2ItemStack(ItemStubs.DIRT, 100), new Rs2ItemStack(ItemStubs.GLASS, 100), new Rs2ItemStack(ItemStubs.SPONGE, 10));
        assertThat(diskDrive.getStored()).isEqualTo(210);
    }

    @Test
    void Test_storage_should_change_when_removing_a_disk() {
        // Arrange
        StorageDisk<Rs2ItemStack> storageDisk1 = new ItemDiskStorage(100);
        diskDrive.getFakeStorageDiskProviderManager().setDisk(1, storageDisk1);

        StorageDisk<Rs2ItemStack> storageDisk2 = new ItemDiskStorage(100);
        diskDrive.getFakeStorageDiskProviderManager().setDisk(2, storageDisk2);

        // Act
        Optional<Rs2ItemStack> remainderBeforeRemovingDisk = diskDrive.insert(new Rs2ItemStack(ItemStubs.DIRT), 105, Action.SIMULATE);
        diskDrive.getFakeStorageDiskProviderManager().removeDiskInSlot(2);
        Optional<Rs2ItemStack> remainderAfterRemovingDisk = diskDrive.insert(new Rs2ItemStack(ItemStubs.DIRT), 105, Action.EXECUTE);

        // Assert
        assertThat(remainderBeforeRemovingDisk).isEmpty();

        assertThat(remainderAfterRemovingDisk).isPresent();
        assertItemStack(remainderAfterRemovingDisk.get(), new Rs2ItemStack(ItemStubs.DIRT, 5));
    }

    @Test
    void Test_storage_should_change_when_inserting_a_disk() {
        // Arrange
        StorageDisk<Rs2ItemStack> storageDisk1 = new ItemDiskStorage(100);
        diskDrive.getFakeStorageDiskProviderManager().setDisk(1, storageDisk1);

        StorageDisk<Rs2ItemStack> storageDisk2 = new ItemDiskStorage(100);

        // Act
        Optional<Rs2ItemStack> remainderBeforeInsertingDisk = diskDrive.insert(new Rs2ItemStack(ItemStubs.DIRT), 105, Action.SIMULATE);
        diskDrive.getFakeStorageDiskProviderManager().setDisk(2, storageDisk2);
        Optional<Rs2ItemStack> remainderAfterInsertingDisk = diskDrive.insert(new Rs2ItemStack(ItemStubs.DIRT), 105, Action.EXECUTE);

        // Assert
        assertThat(remainderBeforeInsertingDisk).isPresent();
        assertItemStack(remainderBeforeInsertingDisk.get(), new Rs2ItemStack(ItemStubs.DIRT, 5));

        assertThat(remainderAfterInsertingDisk).isEmpty();
    }

    @Test
    void Test_inserting_with_filter() {
        // Arrange
        diskDrive.setFilterMode(FilterMode.BLOCK);
        diskDrive.setExactMode(false);
        diskDrive.setFilterTemplates(Arrays.asList(new Rs2ItemStack(ItemStubs.GLASS), new Rs2ItemStack(ItemStubs.STONE)));

        StorageDisk<Rs2ItemStack> storageDisk = new ItemDiskStorage(100);
        diskDrive.getFakeStorageDiskProviderManager().setDisk(1, storageDisk);

        // Act
        Rs2ItemStack glassWithTag = new Rs2ItemStack(ItemStubs.GLASS, 12, "myTag");

        Optional<Rs2ItemStack> remainder1 = diskDrive.insert(new Rs2ItemStack(ItemStubs.GLASS), 12, Action.EXECUTE);
        Optional<Rs2ItemStack> remainder2 = diskDrive.insert(glassWithTag, 12, Action.EXECUTE);
        Optional<Rs2ItemStack> remainder3 = diskDrive.insert(new Rs2ItemStack(ItemStubs.DIRT), 10, Action.EXECUTE);

        // Assert
        assertThat(remainder1).isPresent();
        assertItemStack(remainder1.get(), new Rs2ItemStack(ItemStubs.GLASS, 12));

        assertThat(remainder2).isPresent();
        assertItemStack(remainder2.get(), glassWithTag);

        assertThat(remainder3).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(AccessMode.class)
    void Test_inserting_with_access_mode(AccessMode accessMode) {
        // Arrange
        diskDrive.setAccessMode(accessMode);

        StorageDisk<Rs2ItemStack> storageDisk = new ItemDiskStorage(100);
        diskDrive.getFakeStorageDiskProviderManager().setDisk(1, storageDisk);

        // Act
        Optional<Rs2ItemStack> remainder = diskDrive.insert(new Rs2ItemStack(ItemStubs.DIRT), 5, Action.EXECUTE);

        // Assert
        switch (accessMode) {
            case INSERT_EXTRACT:
            case INSERT:
                assertThat(remainder).isEmpty();
                break;
            case EXTRACT:
                assertThat(remainder).isPresent();
                assertItemStack(remainder.get(), new Rs2ItemStack(ItemStubs.DIRT, 5));
                break;
        }
    }

    @ParameterizedTest
    @EnumSource(AccessMode.class)
    void Test_extracting_with_access_mode(AccessMode accessMode) {
        // Arrange
        diskDrive.setAccessMode(accessMode);

        StorageDisk<Rs2ItemStack> storageDisk = new ItemDiskStorage(100);
        diskDrive.getFakeStorageDiskProviderManager().setDisk(1, storageDisk);

        storageDisk.insert(new Rs2ItemStack(ItemStubs.DIRT), 20, Action.EXECUTE);

        // Act
        Optional<Rs2ItemStack> extracted = diskDrive.extract(new Rs2ItemStack(ItemStubs.DIRT), 5, Action.EXECUTE);

        // Assert
        switch (accessMode) {
            case INSERT_EXTRACT:
            case EXTRACT:
                assertThat(extracted).isPresent();
                assertItemStack(extracted.get(), new Rs2ItemStack(ItemStubs.DIRT, 5));
                break;
            case INSERT:
                assertThat(extracted).isEmpty();
                break;
        }
    }

    @Test
    void Test_inserting_when_inactive() {
        // Arrange
        diskDrive.setRedstoneMode(RedstoneMode.HIGH);

        StorageDisk<Rs2ItemStack> storageDisk = new ItemDiskStorage(100);
        diskDrive.getFakeStorageDiskProviderManager().setDisk(1, storageDisk);

        // Act
        Optional<Rs2ItemStack> remainder = diskDrive.insert(new Rs2ItemStack(ItemStubs.DIRT), 5, Action.EXECUTE);

        // Assert
        assertThat(remainder).isPresent();
        assertItemStack(remainder.get(), new Rs2ItemStack(ItemStubs.DIRT, 5));
    }

    @Test
    void Test_extracting_when_inactive() {
        // Arrange
        diskDrive.setRedstoneMode(RedstoneMode.HIGH);

        StorageDisk<Rs2ItemStack> storageDisk = new ItemDiskStorage(100);
        diskDrive.getFakeStorageDiskProviderManager().setDisk(1, storageDisk);

        storageDisk.insert(new Rs2ItemStack(ItemStubs.DIRT), 20, Action.EXECUTE);

        // Act
        Optional<Rs2ItemStack> extracted = diskDrive.extract(new Rs2ItemStack(ItemStubs.DIRT), 5, Action.EXECUTE);

        // Assert
        assertThat(extracted).isEmpty();
    }

    @Test
    void Test_getting_stacks_when_inactive() {
        // Arrange
        StorageDisk<Rs2ItemStack> storageDisk = new ItemDiskStorage(100);
        diskDrive.getFakeStorageDiskProviderManager().setDisk(1, storageDisk);

        diskDrive.insert(new Rs2ItemStack(ItemStubs.DIRT), 20, Action.EXECUTE);

        // Act
        Collection<Rs2ItemStack> stacksBeforeInactive = diskDrive.getStacks();

        diskDrive.setRedstoneMode(RedstoneMode.HIGH);

        Collection<Rs2ItemStack> stacksAfterInactive = diskDrive.getStacks();

        // Assert
        assertThat(stacksBeforeInactive).isNotEmpty();
        assertThat(stacksAfterInactive).isEmpty();
    }

    @Test
    void Test_changing_to_inactive_should_omit_items_from_storage_channel() {
        // Arrange
        StorageDisk<Rs2ItemStack> disk = new ItemDiskStorage(10);
        disk.insert(new Rs2ItemStack(ItemStubs.DIRT), 10, Action.EXECUTE);

        diskDrive.getFakeStorageDiskProviderManager().setDisk(0, disk);

        network.getComponent(ItemStorageNetworkComponent.class).invalidate();

        diskDrive.setRedstoneMode(RedstoneMode.HIGH);

        // Act
        Collection<Rs2ItemStack> stacksBeforeStateChange = network.getComponent(ItemStorageNetworkComponent.class).getStorageChannel().getStacks();
        diskDrive.onActiveChanged(false);
        Collection<Rs2ItemStack> stacksAfterStateChange = network.getComponent(ItemStorageNetworkComponent.class).getStorageChannel().getStacks();

        // Assert
        assertThat(stacksBeforeStateChange).isNotEmpty();
        assertThat(stacksAfterStateChange).isEmpty();
    }

    @Test
    void Test_calculating_energy_usage() {
        // Arrange
        diskDrive.getFakeStorageDiskProviderManager().setDisk(0, new ItemDiskStorage(1));
        diskDrive.getFakeStorageDiskProviderManager().setDisk(1, new ItemDiskStorage(1));
        diskDrive.getFakeStorageDiskProviderManager().removeDiskInSlot(1);
        diskDrive.getFakeStorageDiskProviderManager().setDisk(2, new ItemDiskStorage(1));

        // Act
        long energyUsage = diskDrive.getEnergyUsage();

        // Assert
        assertThat(energyUsage).isEqualTo(BASE_USAGE + (USAGE_PER_DISK * 2));
    }
}
