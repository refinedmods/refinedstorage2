package com.refinedmods.refinedstorage2.core.network.node.diskdrive;

import com.refinedmods.refinedstorage2.core.Rs2World;
import com.refinedmods.refinedstorage2.core.item.Rs2ItemStack;
import com.refinedmods.refinedstorage2.core.list.item.ItemStackList;
import com.refinedmods.refinedstorage2.core.network.component.ItemStorageNetworkComponent;
import com.refinedmods.refinedstorage2.core.network.node.NetworkNodeImpl;
import com.refinedmods.refinedstorage2.core.storage.AccessMode;
import com.refinedmods.refinedstorage2.core.storage.CompositeItemStorage;
import com.refinedmods.refinedstorage2.core.storage.Priority;
import com.refinedmods.refinedstorage2.core.storage.Storage;
import com.refinedmods.refinedstorage2.core.storage.disk.DiskState;
import com.refinedmods.refinedstorage2.core.storage.disk.StorageDisk;
import com.refinedmods.refinedstorage2.core.storage.disk.StorageDiskManager;
import com.refinedmods.refinedstorage2.core.util.Action;
import com.refinedmods.refinedstorage2.core.util.Filter;
import com.refinedmods.refinedstorage2.core.util.FilterMode;
import com.refinedmods.refinedstorage2.core.util.ItemFilter;
import com.refinedmods.refinedstorage2.core.util.Position;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DiskDriveNetworkNode extends NetworkNodeImpl implements Storage<Rs2ItemStack>, Priority {
    public static final int DISK_COUNT = 8;
    private static final Logger LOGGER = LogManager.getLogger(DiskDriveNetworkNode.class);
    private static final double DISK_NEAR_CAPACITY_THRESHOLD = .75;

    private final StorageDiskManager diskManager;
    private final StorageDiskProvider diskProvider;
    private final StorageDisk[] disks = new StorageDisk[DISK_COUNT];
    private int diskCount;
    private final Filter<Rs2ItemStack> itemFilter = new ItemFilter();
    private AccessMode accessMode = AccessMode.INSERT_EXTRACT;
    private CompositeItemStorage compositeStorage = CompositeItemStorage.empty();
    private int priority;
    private final long energyUsage;
    private final long energyUsagePerDisk;

    public DiskDriveNetworkNode(Rs2World world, Position pos, StorageDiskManager diskManager, StorageDiskProvider diskProvider, long energyUsage, long energyUsagePerDisk) {
        super(world, pos);

        this.diskManager = diskManager;
        this.diskProvider = diskProvider;
        this.energyUsage = energyUsage;
        this.energyUsagePerDisk = energyUsagePerDisk;
    }

    @Override
    protected void onActiveChanged(boolean active) {
        super.onActiveChanged(active);
        network.getComponent(ItemStorageNetworkComponent.class).invalidate();
    }

    @Override
    protected long getEnergyUsage() {
        return energyUsage + (energyUsagePerDisk * diskCount);
    }

    public void onDiskChanged(int slot) {
        if (slot < 0 || slot >= disks.length) {
            LOGGER.warn("Tried to change disk in invalid slot {}", slot);
            return;
        }

        disks[slot] = diskProvider.getDiskId(slot).flatMap(diskManager::getDisk).orElse(null);
        diskCount = (int) Arrays.stream(disks).filter(Objects::nonNull).count();

        List<Storage<Rs2ItemStack>> sources = new ArrayList<>();
        for (StorageDisk<Rs2ItemStack> disk : disks) {
            if (disk != null) {
                sources.add(disk);
            }
        }

        compositeStorage = new CompositeItemStorage(sources, ItemStackList.create());
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
            if (!isActive()) {
                return DiskState.DISCONNECTED;
            }

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

    public boolean isExactMode() {
        return itemFilter.isExact();
    }

    public void setExactMode(boolean exactMode) {
        itemFilter.setExact(exactMode);
    }

    public FilterMode getFilterMode() {
        return itemFilter.getMode();
    }

    public void setFilterMode(FilterMode mode) {
        itemFilter.setMode(mode);
    }

    public void setFilterTemplates(List<Rs2ItemStack> templates) {
        itemFilter.setTemplates(templates);
    }

    public AccessMode getAccessMode() {
        return accessMode;
    }

    public void setAccessMode(AccessMode accessMode) {
        this.accessMode = accessMode;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    @Override
    public Optional<Rs2ItemStack> extract(Rs2ItemStack template, long amount, Action action) {
        if (accessMode == AccessMode.INSERT || !isActive()) {
            return Optional.empty();
        }
        return compositeStorage.extract(template, amount, action);
    }

    @Override
    public Optional<Rs2ItemStack> insert(Rs2ItemStack template, long amount, Action action) {
        if (!itemFilter.isAllowed(template) || accessMode == AccessMode.EXTRACT || !isActive()) {
            Rs2ItemStack remainder = template.copy();
            remainder.setAmount(amount);
            return Optional.of(remainder);
        }
        return compositeStorage.insert(template, amount, action);
    }

    @Override
    public Collection<Rs2ItemStack> getStacks() {
        if (!isActive()) {
            return Collections.emptyList();
        }
        return compositeStorage.getStacks();
    }

    @Override
    public long getStored() {
        return compositeStorage.getStored();
    }
}
