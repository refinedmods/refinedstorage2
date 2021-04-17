package com.refinedmods.refinedstorage2.core.network.node.diskdrive;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.refinedmods.refinedstorage2.core.World;
import com.refinedmods.refinedstorage2.core.list.item.ItemStackList;
import com.refinedmods.refinedstorage2.core.network.node.NetworkNodeImpl;
import com.refinedmods.refinedstorage2.core.network.node.NetworkNodeReference;
import com.refinedmods.refinedstorage2.core.storage.CompositeItemStorage;
import com.refinedmods.refinedstorage2.core.storage.Priority;
import com.refinedmods.refinedstorage2.core.storage.Storage;
import com.refinedmods.refinedstorage2.core.storage.disk.DiskState;
import com.refinedmods.refinedstorage2.core.storage.disk.StorageDisk;
import com.refinedmods.refinedstorage2.core.storage.disk.StorageDiskManager;
import com.refinedmods.refinedstorage2.core.util.Action;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DiskDriveNetworkNode extends NetworkNodeImpl implements Storage<ItemStack>, Priority {
    private static final Logger LOGGER = LogManager.getLogger(DiskDriveNetworkNode.class);

    public static final int DISK_COUNT = 8;
    private static final double DISK_NEAR_CAPACITY_THRESHOLD = .75;

    private final StorageDiskManager diskManager;
    private final StorageDiskProvider diskProvider;
    private final StorageDisk[] disks = new StorageDisk[DISK_COUNT];
    private CompositeItemStorage compositeStorage = CompositeItemStorage.emptyStorage();
    private int priority;

    public DiskDriveNetworkNode(World world, BlockPos pos, NetworkNodeReference ref, StorageDiskManager diskManager, StorageDiskProvider diskProvider) {
        super(world, pos, ref);

        this.diskManager = diskManager;
        this.diskProvider = diskProvider;
    }

    public void onDiskChanged(int slot) {
        if (slot < 0 || slot >= disks.length) {
            LOGGER.warn("Tried to change disk in invalid slot {}", slot);
            return;
        }

        disks[slot] = diskProvider.getDiskId(slot).flatMap(diskManager::getDisk).orElse(null);

        List<Storage<ItemStack>> sources = new ArrayList<>();
        for (StorageDisk<ItemStack> disk : disks) {
            if (disk != null) {
                sources.add(disk);
            }
        }

        compositeStorage = new CompositeItemStorage(sources, new ItemStackList());
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

    @Override
    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
        network.invalidateStorageChannelSources();
    }

    @Override
    public Optional<ItemStack> extract(ItemStack template, int amount, Action action) {
        return compositeStorage.extract(template, amount, action);
    }

    @Override
    public Optional<ItemStack> insert(ItemStack template, int amount, Action action) {
        return compositeStorage.insert(template, amount, action);
    }

    @Override
    public Collection<ItemStack> getStacks() {
        return compositeStorage.getStacks();
    }

    @Override
    public int getStored() {
        return compositeStorage.getStored();
    }
}
