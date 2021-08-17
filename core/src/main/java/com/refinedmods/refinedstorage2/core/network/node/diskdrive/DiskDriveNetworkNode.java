package com.refinedmods.refinedstorage2.core.network.node.diskdrive;

import com.refinedmods.refinedstorage2.core.list.item.StackListImpl;
import com.refinedmods.refinedstorage2.core.network.component.StorageNetworkComponent;
import com.refinedmods.refinedstorage2.core.network.node.NetworkNodeImpl;
import com.refinedmods.refinedstorage2.core.stack.item.Rs2ItemStack;
import com.refinedmods.refinedstorage2.core.storage.AccessMode;
import com.refinedmods.refinedstorage2.core.storage.Storage;
import com.refinedmods.refinedstorage2.core.storage.StorageSource;
import com.refinedmods.refinedstorage2.core.storage.channel.StorageChannelType;
import com.refinedmods.refinedstorage2.core.storage.channel.StorageChannelTypes;
import com.refinedmods.refinedstorage2.core.storage.composite.CompositeStorage;
import com.refinedmods.refinedstorage2.core.storage.composite.Priority;
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

public class DiskDriveNetworkNode extends NetworkNodeImpl implements Storage<Rs2ItemStack>, StorageSource, Priority {
    public static final int DISK_COUNT = 8;

    private static final Logger LOGGER = LogManager.getLogger(DiskDriveNetworkNode.class);

    private StorageDiskManager diskManager;

    private final StorageDiskProvider diskProvider;
    private final long energyUsage;
    private final long energyUsagePerDisk;
    private final DiskDriveListener listener;

    private final DiskDriveItemStorageDisk[] disks = new DiskDriveItemStorageDisk[DISK_COUNT];
    private int diskCount;
    private CompositeStorage<Rs2ItemStack> compositeStorage = CompositeStorage.emptyItemStackStorage();

    private final Filter<Rs2ItemStack> itemFilter = new ItemFilter();
    private AccessMode accessMode = AccessMode.INSERT_EXTRACT;
    private int priority;

    public DiskDriveNetworkNode(Position pos, StorageDiskProvider diskProvider, long energyUsage, long energyUsagePerDisk, DiskDriveListener listener) {
        super(pos);
        this.diskProvider = diskProvider;
        this.energyUsage = energyUsage;
        this.energyUsagePerDisk = energyUsagePerDisk;
        this.listener = listener;
    }

    public void initialize(StorageDiskManager diskManager) {
        this.diskManager = diskManager;
        for (int i = 0; i < DISK_COUNT; ++i) {
            initializeDiskInSlot(i);
        }
        initializeDiskCountAndStorage();
    }

    public void onDiskChanged(int slot) {
        if (slot < 0 || slot >= disks.length) {
            LOGGER.warn("Tried to change disk in invalid slot {}", slot);
            return;
        }
        initializeDiskInSlot(slot);
        initializeDiskCountAndStorage();
        LOGGER.info("Invalidating storage due to disk drive disk change");
        network.getComponent(StorageNetworkComponent.class).getStorageChannel(StorageChannelTypes.ITEM).invalidate();
    }

    private void initializeDiskCountAndStorage() {
        this.diskCount = (int) Arrays
                .stream(disks)
                .filter(Objects::nonNull)
                .count();
        this.compositeStorage = new CompositeStorage<>(createSources(), StackListImpl.createItemStackList());
    }

    private void initializeDiskInSlot(int slot) {
        disks[slot] = diskProvider
                .getDiskId(slot)
                .flatMap(diskManager::getDisk)
                .map(disk -> new DiskDriveItemStorageDisk((StorageDisk) disk, listener))
                .orElse(null);
    }

    private List<Storage<Rs2ItemStack>> createSources() {
        List<Storage<Rs2ItemStack>> sources = new ArrayList<>();
        for (StorageDisk<Rs2ItemStack> disk : disks) {
            if (disk != null) {
                sources.add(disk);
            }
        }
        return sources;
    }

    @Override
    protected void onActiveChanged(boolean active) {
        super.onActiveChanged(active);
        LOGGER.info("Invalidating storage due to disk drive activeness change");
        network.getComponent(StorageNetworkComponent.class).getStorageChannel(StorageChannelTypes.ITEM).invalidate();
    }

    @Override
    protected long getEnergyUsage() {
        return energyUsage + (energyUsagePerDisk * diskCount);
    }

    public DiskDriveState createState() {
        DiskDriveState states = new DiskDriveState(DISK_COUNT);
        for (int i = 0; i < DISK_COUNT; ++i) {
            states.setState(i, getState(disks[i]));
        }
        return states;
    }

    private DiskState getState(DiskDriveItemStorageDisk disk) {
        if (disk == null) {
            return DiskState.NONE;
        } else if (!isActive()) {
            return DiskState.DISCONNECTED;
        }
        return disk.getState();
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
        if (network != null) {
            network.getComponent(StorageNetworkComponent.class).getStorageChannel(StorageChannelTypes.ITEM).sortSources();
        }
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

    @Override
    public <T> Optional<Storage<T>> getStorageForChannel(StorageChannelType<T> channelType) {
        if (channelType == StorageChannelTypes.ITEM) {
            return Optional.of((Storage<T>) this);
        }
        return Optional.empty();
    }
}
