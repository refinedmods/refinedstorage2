package com.refinedmods.refinedstorage2.core.network.node.diskdrive;

import com.refinedmods.refinedstorage2.core.network.node.NetworkNodeImpl;
import com.refinedmods.refinedstorage2.core.network.node.NetworkNodeReference;
import com.refinedmods.refinedstorage2.core.storage.disk.DiskState;
import com.refinedmods.refinedstorage2.core.storage.disk.StorageDisk;
import com.refinedmods.refinedstorage2.core.storage.disk.StorageDiskManager;
import net.minecraft.util.math.BlockPos;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DiskDriveNetworkNode extends NetworkNodeImpl {
    private static final Logger LOGGER = LogManager.getLogger(DiskDriveNetworkNode.class);

    public static final int DISK_COUNT = 8;
    private static final double DISK_NEAR_CAPACITY_THRESHOLD = .75;

    private final StorageDiskManager diskManager;
    private final StorageDiskProvider diskProvider;
    private final StorageDisk[] disks = new StorageDisk[DISK_COUNT];

    public DiskDriveNetworkNode(BlockPos pos, NetworkNodeReference ref, StorageDiskManager diskManager, StorageDiskProvider diskProvider) {
        super(pos, ref);

        this.diskManager = diskManager;
        this.diskProvider = diskProvider;
    }

    public void onDiskChanged(int slot) {
        if (slot < 0 || slot >= disks.length) {
            LOGGER.warn("Tried to change disk in invalid slot {}", slot);
            return;
        }

        disks[slot] = null;

        diskProvider.getDiskId(slot).ifPresent(id -> disks[slot] = diskManager.getDisk(id).orElse(null));
    }

    public DiskDriveState createState() {
        DiskDriveState states = new DiskDriveState(DISK_COUNT);
        for (int i = 0; i < DISK_COUNT; ++i) {
            states.setState(i, getState(disks[i]));
        }

        return states;
    }

    private DiskState getState(StorageDisk<?> disk) {
        if (disk == null) {
            return DiskState.NONE;
        } else {
            double fullness = (double) disk.getStored() / (double) disk.getCapacity();

            if (fullness >= 1D) {
                return DiskState.FULL;
            } else if (fullness >= DISK_NEAR_CAPACITY_THRESHOLD) {
                return DiskState.NEAR_CAPACITY;
            } else {
                return DiskState.NORMAL;
            }
        }
    }
}
