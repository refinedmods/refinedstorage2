package com.refinedmods.refinedstorage2.core.network.node.diskdrive;

import com.refinedmods.refinedstorage2.core.RefinedStorage2Test;
import com.refinedmods.refinedstorage2.core.network.node.NetworkNodeReference;
import com.refinedmods.refinedstorage2.core.storage.disk.DiskState;
import com.refinedmods.refinedstorage2.core.storage.disk.ItemDiskStorage;
import com.refinedmods.refinedstorage2.core.storage.disk.StorageDisk;
import com.refinedmods.refinedstorage2.core.storage.disk.StorageDiskManager;
import com.refinedmods.refinedstorage2.core.util.Action;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RefinedStorage2Test
class DiskDriveNetworkNodeTest {
    private DiskDriveNetworkNode diskDrive;
    private StorageDiskProvider diskProvider;
    private StorageDiskManager diskManager;

    @BeforeEach
    void setUp() {
        diskManager = mock(StorageDiskManager.class);
        diskProvider = mock(StorageDiskProvider.class);

        diskDrive = new DiskDriveNetworkNode(
            BlockPos.ORIGIN,
            mock(NetworkNodeReference.class),
            diskManager,
            diskProvider
        );
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
        UUID id = UUID.randomUUID();

        StorageDisk<ItemStack> storageDisk = new ItemDiskStorage(10);
        storageDisk.insert(new ItemStack(Items.DIRT), 7, Action.EXECUTE);

        when(diskManager.getDisk(id)).thenReturn(Optional.of((StorageDisk) storageDisk));

        when(diskProvider.getDiskId(anyInt())).thenReturn(Optional.empty());
        when(diskProvider.getDiskId(2)).thenReturn(Optional.of(id));

        // Act
        diskDrive.onDiskChanged(2);

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
        UUID id = UUID.randomUUID();

        StorageDisk<ItemStack> storageDisk = new ItemDiskStorage(10);
        storageDisk.insert(new ItemStack(Items.DIRT), 10, Action.EXECUTE);

        when(diskManager.getDisk(id)).thenReturn(Optional.of((StorageDisk) storageDisk));

        when(diskProvider.getDiskId(anyInt())).thenReturn(Optional.empty());
        when(diskProvider.getDiskId(7)).thenReturn(Optional.of(id));

        // Act
        diskDrive.onDiskChanged(7);

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
        UUID id = UUID.randomUUID();

        StorageDisk<ItemStack> storageDisk = new ItemDiskStorage(10);
        storageDisk.insert(new ItemStack(Items.DIRT), 8, Action.EXECUTE);

        when(diskManager.getDisk(id)).thenReturn(Optional.of((StorageDisk) storageDisk));

        when(diskProvider.getDiskId(anyInt())).thenReturn(Optional.empty());
        when(diskProvider.getDiskId(3)).thenReturn(Optional.of(id));

        // Act
        diskDrive.onDiskChanged(3);

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
    void Test_state_with_a_disk_that_does_not_exist() {
        // Arrange
        UUID id = UUID.randomUUID();

        when(diskProvider.getDiskId(anyInt())).thenReturn(Optional.empty());
        when(diskProvider.getDiskId(3)).thenReturn(Optional.of(id));

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
}
