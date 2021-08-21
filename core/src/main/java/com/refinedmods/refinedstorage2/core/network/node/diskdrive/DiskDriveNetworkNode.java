package com.refinedmods.refinedstorage2.core.network.node.diskdrive;

import com.refinedmods.refinedstorage2.api.core.Position;
import com.refinedmods.refinedstorage2.api.stack.Rs2Stack;
import com.refinedmods.refinedstorage2.api.stack.filter.Filter;
import com.refinedmods.refinedstorage2.api.stack.filter.FilterMode;
import com.refinedmods.refinedstorage2.api.stack.filter.ItemFilter;
import com.refinedmods.refinedstorage2.api.stack.item.Rs2ItemStack;
import com.refinedmods.refinedstorage2.core.network.component.StorageNetworkComponent;
import com.refinedmods.refinedstorage2.core.network.node.NetworkNodeImpl;
import com.refinedmods.refinedstorage2.core.storage.AccessMode;
import com.refinedmods.refinedstorage2.core.storage.Storage;
import com.refinedmods.refinedstorage2.core.storage.StorageSource;
import com.refinedmods.refinedstorage2.core.storage.channel.StorageChannelType;
import com.refinedmods.refinedstorage2.core.storage.channel.StorageChannelTypeRegistry;
import com.refinedmods.refinedstorage2.core.storage.channel.StorageChannelTypes;
import com.refinedmods.refinedstorage2.core.storage.disk.DiskState;
import com.refinedmods.refinedstorage2.core.storage.disk.StorageDiskManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import com.google.common.collect.ImmutableMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DiskDriveNetworkNode extends NetworkNodeImpl implements StorageSource {
    public static final int DISK_COUNT = 8;

    private static final Logger LOGGER = LogManager.getLogger(DiskDriveNetworkNode.class);

    private StorageDiskManager diskManager;

    private final StorageDiskProvider diskProvider;
    private final long energyUsage;
    private final long energyUsagePerDisk;
    private final DiskDriveListener listener;

    private final DiskDriveStorageDisk[] disks = new DiskDriveStorageDisk[DISK_COUNT];
    private final Map<StorageChannelType<?>, DiskDriveStorage<?>> compositeStorages;
    private int diskCount;

    private final Filter<Rs2ItemStack> itemFilter = new ItemFilter();
    private AccessMode accessMode = AccessMode.INSERT_EXTRACT;
    private int priority;

    public DiskDriveNetworkNode(Position pos, StorageDiskProvider diskProvider, long energyUsage, long energyUsagePerDisk, DiskDriveListener listener, StorageChannelTypeRegistry storageChannelTypeRegistry) {
        super(pos);
        this.diskProvider = diskProvider;
        this.energyUsage = energyUsage;
        this.energyUsagePerDisk = energyUsagePerDisk;
        this.listener = listener;
        this.compositeStorages = createCompositeStorages(storageChannelTypeRegistry);
    }

    private Map<StorageChannelType<?>, DiskDriveStorage<?>> createCompositeStorages(StorageChannelTypeRegistry storageChannelTypeRegistry) {
        return storageChannelTypeRegistry
                .getTypes()
                .stream()
                .collect(ImmutableMap.toImmutableMap(type -> type, this::createCompositeStorage));
    }

    private DiskDriveStorage<?> createCompositeStorage(StorageChannelType<?> type) {
        if (type == StorageChannelTypes.ITEM) {
            return new DiskDriveItemStorage(this, itemFilter);
        }
        return new DiskDriveStorage<>(this, type);
    }

    public void initialize(StorageDiskManager diskManager) {
        this.diskManager = diskManager;

        Set<StorageChannelType<?>> affectedStorageChannelTypes = new HashSet<>();
        for (int i = 0; i < DISK_COUNT; ++i) {
            affectedStorageChannelTypes.addAll(initializeDiskInSlot(i));
        }
        affectedStorageChannelTypes.forEach(this::invalidateChannel);
        setDiskCount();
    }

    public void onDiskChanged(int slot) {
        if (slot < 0 || slot >= disks.length) {
            LOGGER.warn("Tried to change disk in invalid slot {}", slot);
            return;
        }
        Set<StorageChannelType<?>> affectedStorageChannelTypes = initializeDiskInSlot(slot);
        affectedStorageChannelTypes.forEach(type -> {
            invalidateChannel(type);
            network.getComponent(StorageNetworkComponent.class).getStorageChannel(type).invalidate();
        });
        setDiskCount();
    }

    private void setDiskCount() {
        this.diskCount = (int) Arrays
                .stream(disks)
                .filter(Objects::nonNull)
                .count();
    }

    private <T extends Rs2Stack> void invalidateChannel(StorageChannelType<T> channelType) {
        List<Storage<T>> sources = getSourcesForChannel(channelType);
        ((DiskDriveStorage<T>) compositeStorages.get(channelType)).setSources(sources);
    }

    private <T extends Rs2Stack> List<Storage<T>> getSourcesForChannel(StorageChannelType<T> channelType) {
        List<Storage<T>> sources = new ArrayList<>();
        for (DiskDriveStorageDisk<?> disk : disks) {
            if (disk != null && disk.getStorageChannelType() == channelType) {
                sources.add((Storage<T>) disk);
            }
        }
        return sources;
    }

    private Set<StorageChannelType<?>> initializeDiskInSlot(int slot) {
        Set<StorageChannelType<?>> affectedStorageChannelTypes = new HashSet<>();
        if (disks[slot] != null) {
            affectedStorageChannelTypes.add(disks[slot].getStorageChannelType());
        }

        diskProvider.getStorageChannelType(slot).ifPresentOrElse(type -> {
            disks[slot] = diskProvider
                    .getDiskId(slot)
                    .flatMap(diskManager::getDisk)
                    .map(disk -> new DiskDriveStorageDisk(disk, type, listener))
                    .orElse(null);

            affectedStorageChannelTypes.add(type);
        }, () -> disks[slot] = null);

        return affectedStorageChannelTypes;
    }

    @Override
    protected void onActiveChanged(boolean active) {
        super.onActiveChanged(active);
        LOGGER.info("Invalidating storage due to disk drive activeness change");
        compositeStorages.keySet().forEach(type -> network.getComponent(StorageNetworkComponent.class).getStorageChannel(type).invalidate());
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

    private DiskState getState(DiskDriveStorageDisk<?> disk) {
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

    public void setPriority(int priority) {
        this.priority = priority;
        if (network != null) {
            compositeStorages.keySet().forEach(type -> network.getComponent(StorageNetworkComponent.class).getStorageChannel(type).sortSources());
        }
    }

    public int getPriority() {
        return priority;
    }

    @Override
    public <T extends Rs2Stack> Optional<Storage<T>> getStorageForChannel(StorageChannelType<T> channelType) {
        DiskDriveStorage<?> storage = compositeStorages.get(channelType);
        if (storage != null) {
            return Optional.of((Storage<T>) storage);
        }
        return Optional.empty();
    }
}
