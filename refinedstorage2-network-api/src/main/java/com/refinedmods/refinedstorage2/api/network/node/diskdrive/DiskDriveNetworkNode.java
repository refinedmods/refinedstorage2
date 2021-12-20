package com.refinedmods.refinedstorage2.api.network.node.diskdrive;

import com.refinedmods.refinedstorage2.api.core.filter.Filter;
import com.refinedmods.refinedstorage2.api.core.filter.FilterMode;
import com.refinedmods.refinedstorage2.api.network.component.StorageNetworkComponent;
import com.refinedmods.refinedstorage2.api.network.node.NetworkNodeImpl;
import com.refinedmods.refinedstorage2.api.storage.AccessMode;
import com.refinedmods.refinedstorage2.api.storage.CappedStorage;
import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.StorageRepository;
import com.refinedmods.refinedstorage2.api.storage.StorageSource;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelTypeRegistry;

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

    private StorageRepository storageRepository;
    private StorageDiskProvider diskProvider;
    private DiskDriveListener listener;

    private final long energyUsage;
    private final long energyUsagePerDisk;

    private final DiskDriveDiskStorage[] disks = new DiskDriveDiskStorage[DISK_COUNT];
    private final Map<StorageChannelType<?>, DiskDriveStorage<?>> compositeStorages;
    private int diskCount;

    private final Filter filter = new Filter();

    private AccessMode accessMode = AccessMode.INSERT_EXTRACT;
    private int priority;

    public DiskDriveNetworkNode(long energyUsage, long energyUsagePerDisk, StorageChannelTypeRegistry storageChannelTypeRegistry) {
        this.energyUsage = energyUsage;
        this.energyUsagePerDisk = energyUsagePerDisk;
        this.compositeStorages = createCompositeStorages(storageChannelTypeRegistry);
    }

    private Map<StorageChannelType<?>, DiskDriveStorage<?>> createCompositeStorages(StorageChannelTypeRegistry storageChannelTypeRegistry) {
        return storageChannelTypeRegistry
                .getTypes()
                .stream()
                .collect(ImmutableMap.toImmutableMap(type -> type, this::createCompositeStorage));
    }

    private DiskDriveStorage<?> createCompositeStorage(StorageChannelType<?> type) {
        return new DiskDriveStorage(this, type, filter);
    }

    public void initialize(StorageRepository storageRepository) {
        this.storageRepository = storageRepository;

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

    private <T> void invalidateChannel(StorageChannelType<T> channelType) {
        List<Storage<T>> sources = getSourcesForChannel(channelType);
        ((DiskDriveStorage<T>) compositeStorages.get(channelType)).setSources(sources);
    }

    private <T> List<Storage<T>> getSourcesForChannel(StorageChannelType<T> channelType) {
        List<Storage<T>> sources = new ArrayList<>();
        for (DiskDriveDiskStorage<?> disk : disks) {
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
                    .flatMap(storageRepository::get)
                    .filter(this::isValidDisk)
                    .map(CappedStorage.class::cast)
                    .map(cappedStorage -> new DiskDriveDiskStorage(cappedStorage, type, listener))
                    .orElse(null);

            affectedStorageChannelTypes.add(type);
        }, () -> disks[slot] = null);

        return affectedStorageChannelTypes;
    }

    private boolean isValidDisk(Storage<?> storage) {
        return storage instanceof CappedStorage;
    }

    @Override
    public void onActiveChanged(boolean active) {
        super.onActiveChanged(active);
        LOGGER.info("Invalidating storage due to disk drive activeness change");
        if (network != null) {
            compositeStorages.keySet().forEach(type -> network.getComponent(StorageNetworkComponent.class).getStorageChannel(type).invalidate());
        }
    }

    public FilterMode getFilterMode() {
        return filter.getMode();
    }

    public void setFilterMode(FilterMode mode) {
        filter.setMode(mode);
    }

    public void setFilterTemplates(Set<Object> templates) {
        filter.setTemplates(templates);
    }

    public void setDiskProvider(StorageDiskProvider diskProvider) {
        this.diskProvider = diskProvider;
    }

    public void setListener(DiskDriveListener listener) {
        this.listener = listener;
    }

    @Override
    public long getEnergyUsage() {
        return energyUsage + (energyUsagePerDisk * diskCount);
    }

    public DiskDriveState createState() {
        DiskDriveState states = new DiskDriveState(DISK_COUNT);
        for (int i = 0; i < DISK_COUNT; ++i) {
            states.setState(i, getState(disks[i]));
        }
        return states;
    }

    private StorageDiskState getState(DiskDriveDiskStorage<?> disk) {
        if (disk == null) {
            return StorageDiskState.NONE;
        } else if (!isActive()) {
            return StorageDiskState.DISCONNECTED;
        }
        return disk.getState();
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
    public <T> Optional<Storage<T>> getStorageForChannel(StorageChannelType<T> channelType) {
        DiskDriveStorage<?> storage = compositeStorages.get(channelType);
        if (storage != null) {
            return Optional.of((Storage<T>) storage);
        }
        return Optional.empty();
    }
}
