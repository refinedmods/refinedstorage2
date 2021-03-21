package com.refinedmods.refinedstorage2.core.network.node.diskdrive;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

import com.refinedmods.refinedstorage2.core.RefinedStorage2Test;
import com.refinedmods.refinedstorage2.core.adapter.FakeWorld;
import com.refinedmods.refinedstorage2.core.network.node.NetworkNodeReference;
import com.refinedmods.refinedstorage2.core.storage.disk.DiskState;
import com.refinedmods.refinedstorage2.core.storage.disk.ItemDiskStorage;
import com.refinedmods.refinedstorage2.core.storage.disk.StorageDisk;
import com.refinedmods.refinedstorage2.core.util.Action;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.refinedmods.refinedstorage2.core.util.ItemStackAssertions.assertItemStack;
import static com.refinedmods.refinedstorage2.core.util.ItemStackAssertions.assertItemStackListContents;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@RefinedStorage2Test
class DiskDriveNetworkNodeTest {
    private DiskDriveNetworkNode diskDrive;
    private FakeStorageDiskProviderManager diskProviderManager;

    @BeforeEach
    void setUp() {
        diskProviderManager = new FakeStorageDiskProviderManager();

        diskDrive = new DiskDriveNetworkNode(
            new FakeWorld(),
            BlockPos.ORIGIN,
            mock(NetworkNodeReference.class),
            diskProviderManager,
            diskProviderManager
        );

        diskProviderManager.setDiskDrive(diskDrive);
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
        StorageDisk<ItemStack> storageDisk = new ItemDiskStorage(100);
        storageDisk.insert(new ItemStack(Items.DIRT), 74, Action.EXECUTE);

        // Act
        diskProviderManager.setDisk(2, storageDisk);

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
        StorageDisk<ItemStack> storageDisk = new ItemDiskStorage(10);
        storageDisk.insert(new ItemStack(Items.DIRT), 10, Action.EXECUTE);

        // Act
        diskProviderManager.setDisk(7, storageDisk);

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
        StorageDisk<ItemStack> storageDisk = new ItemDiskStorage(100);
        storageDisk.insert(new ItemStack(Items.DIRT), 75, Action.EXECUTE);

        // Act
        diskProviderManager.setDisk(3, storageDisk);

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
    void Test_state_should_change_when_removing_disk() {
        // Arrange
        StorageDisk<ItemStack> storageDisk = new ItemDiskStorage(100);
        storageDisk.insert(new ItemStack(Items.DIRT), 74, Action.EXECUTE);

        // Act
        diskProviderManager.setDisk(2, storageDisk);
        diskProviderManager.setDisk(4, storageDisk);
        DiskDriveState stateBeforeRemoval = diskDrive.createState();

        diskProviderManager.removeDiskInSlot(2);
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
        diskProviderManager.setDisk(3, UUID.randomUUID());

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
    }

    @Test
    void Test_inserting() {
        // Arrange
        StorageDisk<ItemStack> storageDisk1 = new ItemDiskStorage(100);
        diskProviderManager.setDisk(1, storageDisk1);

        StorageDisk<ItemStack> storageDisk2 = new ItemDiskStorage(100);
        diskProviderManager.setDisk(2, storageDisk2);

        StorageDisk<ItemStack> storageDisk3 = new ItemDiskStorage(100);
        diskProviderManager.setDisk(3, storageDisk3);

        // Act
        Optional<ItemStack> remainder1 = diskDrive.insert(new ItemStack(Items.DIRT), 150, Action.EXECUTE);
        Optional<ItemStack> remainder2 = diskDrive.insert(new ItemStack(Items.DIRT), 10, Action.EXECUTE);
        Optional<ItemStack> remainder3 = diskDrive.insert(new ItemStack(Items.GLASS), 300, Action.EXECUTE);


        // Assert
        assertThat(remainder1).isEmpty();
        assertThat(remainder2).isEmpty();
        assertThat(remainder3).isPresent();
        assertItemStack(remainder3.get(), new ItemStack(Items.GLASS, 160));

        assertItemStackListContents(storageDisk1.getStacks(), new ItemStack(Items.DIRT, 100));
        assertItemStackListContents(storageDisk2.getStacks(), new ItemStack(Items.DIRT, 60), new ItemStack(Items.GLASS, 40));
        assertItemStackListContents(storageDisk3.getStacks(), new ItemStack(Items.GLASS, 100));

        assertThat(diskDrive.getStored()).isEqualTo(150 + 10 + 140);
    }

    @Test
    void Test_extracting() {
        // Arrange
        StorageDisk<ItemStack> storageDisk1 = new ItemDiskStorage(100);
        StorageDisk<ItemStack> storageDisk2 = new ItemDiskStorage(100);
        StorageDisk<ItemStack> storageDisk3 = new ItemDiskStorage(100);

        storageDisk1.insert(new ItemStack(Items.DIRT), 50, Action.EXECUTE);
        storageDisk1.insert(new ItemStack(Items.GLASS), 50, Action.EXECUTE);
        diskProviderManager.setDisk(1, storageDisk1);

        storageDisk2.insert(new ItemStack(Items.DIRT), 50, Action.EXECUTE);
        storageDisk2.insert(new ItemStack(Items.GLASS), 50, Action.EXECUTE);
        diskProviderManager.setDisk(2, storageDisk2);

        storageDisk3.insert(new ItemStack(Items.SPONGE), 10, Action.EXECUTE);
        diskProviderManager.setDisk(3, storageDisk3);

        // Act & assert
        Optional<ItemStack> stack1 = diskDrive.extract(new ItemStack(Items.DIRT), 85, Action.EXECUTE);

        assertThat(stack1).isPresent();
        assertItemStack(stack1.get(), new ItemStack(Items.DIRT, 85));

        assertItemStackListContents(storageDisk1.getStacks(), new ItemStack(Items.GLASS, 50));
        assertItemStackListContents(storageDisk2.getStacks(), new ItemStack(Items.GLASS, 50), new ItemStack(Items.DIRT, 15));
        assertItemStackListContents(storageDisk3.getStacks(), new ItemStack(Items.SPONGE, 10));

        assertThat(diskDrive.getStored()).isEqualTo(125);
    }

    @Test
    void Test_getting_stacks() {
        // Arrange
        StorageDisk<ItemStack> storageDisk1 = new ItemDiskStorage(100);
        StorageDisk<ItemStack> storageDisk2 = new ItemDiskStorage(100);
        StorageDisk<ItemStack> storageDisk3 = new ItemDiskStorage(100);

        storageDisk1.insert(new ItemStack(Items.DIRT), 50, Action.EXECUTE);
        storageDisk1.insert(new ItemStack(Items.GLASS), 50, Action.EXECUTE);
        diskProviderManager.setDisk(1, storageDisk1);

        storageDisk2.insert(new ItemStack(Items.DIRT), 50, Action.EXECUTE);
        storageDisk2.insert(new ItemStack(Items.GLASS), 50, Action.EXECUTE);
        diskProviderManager.setDisk(2, storageDisk2);

        storageDisk3.insert(new ItemStack(Items.SPONGE), 10, Action.EXECUTE);
        diskProviderManager.setDisk(3, storageDisk3);

        // Act
        Collection<ItemStack> stacks = diskDrive.getStacks();

        // Assert
        assertItemStackListContents(stacks, new ItemStack(Items.DIRT, 100), new ItemStack(Items.GLASS, 100), new ItemStack(Items.SPONGE, 10));
        assertThat(diskDrive.getStored()).isEqualTo(210);
    }

    @Test
    void Test_storage_should_change_when_removing_a_disk() {
        // Arrange
        StorageDisk<ItemStack> storageDisk1 = new ItemDiskStorage(100);
        diskProviderManager.setDisk(1, storageDisk1);

        StorageDisk<ItemStack> storageDisk2 = new ItemDiskStorage(100);
        diskProviderManager.setDisk(2, storageDisk2);

        // Act
        Optional<ItemStack> remainderBeforeRemovingDisk = diskDrive.insert(new ItemStack(Items.DIRT), 105, Action.SIMULATE);
        diskProviderManager.removeDiskInSlot(2);
        Optional<ItemStack> remainderAfterRemovingDisk = diskDrive.insert(new ItemStack(Items.DIRT), 105, Action.EXECUTE);

        // Assert
        assertThat(remainderBeforeRemovingDisk).isEmpty();

        assertThat(remainderAfterRemovingDisk).isPresent();
        assertItemStack(remainderAfterRemovingDisk.get(), new ItemStack(Items.DIRT, 5));
    }

    @Test
    void Test_storage_should_change_when_inserting_a_disk() {
        // Arrange
        StorageDisk<ItemStack> storageDisk1 = new ItemDiskStorage(100);
        diskProviderManager.setDisk(1, storageDisk1);

        StorageDisk<ItemStack> storageDisk2 = new ItemDiskStorage(100);

        // Act
        Optional<ItemStack> remainderBeforeInsertingDisk = diskDrive.insert(new ItemStack(Items.DIRT), 105, Action.SIMULATE);
        diskProviderManager.setDisk(2, storageDisk2);
        Optional<ItemStack> remainderAfterInsertingDisk = diskDrive.insert(new ItemStack(Items.DIRT), 105, Action.EXECUTE);

        // Assert
        assertThat(remainderBeforeInsertingDisk).isPresent();
        assertItemStack(remainderBeforeInsertingDisk.get(), new ItemStack(Items.DIRT, 5));

        assertThat(remainderAfterInsertingDisk).isEmpty();
    }
}
