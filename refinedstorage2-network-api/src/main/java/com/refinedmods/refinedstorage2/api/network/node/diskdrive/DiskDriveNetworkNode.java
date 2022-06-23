package com.refinedmods.refinedstorage2.api.network.node.diskdrive;

import com.refinedmods.refinedstorage2.api.core.filter.Filter;
import com.refinedmods.refinedstorage2.api.core.filter.FilterMode;
import com.refinedmods.refinedstorage2.api.core.registry.OrderedRegistry;
import com.refinedmods.refinedstorage2.api.network.component.StorageNetworkComponent;
import com.refinedmods.refinedstorage2.api.network.component.StorageProvider;
import com.refinedmods.refinedstorage2.api.network.node.NetworkNodeImpl;
import com.refinedmods.refinedstorage2.api.storage.AccessMode;
import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.StorageRepository;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.UnaryOperator;

import com.google.common.collect.ImmutableMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DiskDriveNetworkNode extends NetworkNodeImpl implements StorageProvider {
    public static final int DISK_COUNT = 8;

    private static final Logger LOGGER = LogManager.getLogger(DiskDriveNetworkNode.class);

    private StorageRepository storageRepository;
    private StorageDiskProvider diskProvider;
    private DiskDriveListener listener;

    private final long energyUsage;
    private final long energyUsagePerDisk;

    private final DiskDriveDiskStorage<?>[] disks = new DiskDriveDiskStorage[DISK_COUNT];
    private final Map<StorageChannelType<?>, DiskDriveCompositeStorage<?>> compositeStorages;
    private int diskCount;

    private final Filter filter = new Filter();

    private AccessMode accessMode = AccessMode.INSERT_EXTRACT;
    private int priority;

    public DiskDriveNetworkNode(long energyUsage, long energyUsagePerDisk, OrderedRegistry<?, StorageChannelType<?>> storageChannelTypeRegistry) {
        this.energyUsage = energyUsage;
        this.energyUsagePerDisk = energyUsagePerDisk;
        this.compositeStorages = createCompositeStorages(storageChannelTypeRegistry);
    }

    private Map<StorageChannelType<?>, DiskDriveCompositeStorage<?>> createCompositeStorages(OrderedRegistry<?, StorageChannelType<?>> storageChannelTypeRegistry) {
        return storageChannelTypeRegistry
                .getAll()
                .stream()
                .collect(ImmutableMap.toImmutableMap(type -> type, this::createCompositeStorage));
    }

    private DiskDriveCompositeStorage<?> createCompositeStorage(StorageChannelType<?> type) {
        return new DiskDriveCompositeStorage<>(this, filter);
    }

    public void initialize(StorageRepository storageRepository) {
        this.storageRepository = storageRepository;
        for (int i = 0; i < DISK_COUNT; ++i) {
            initializeDiskInSlot(i);
        }
        updateDiskCount();
    }

    public void onDiskChanged(int slot) {
        if (slot < 0 || slot >= disks.length) {
            LOGGER.warn("Tried to change disk in invalid slot {}", slot);
            return;
        }
        initializeDiskInSlot(slot).forEach(this::processDiskChange);
        updateDiskCount();
    }

    private Set<DiskChange> initializeDiskInSlot(int slot) {
        Set<DiskChange> results = new HashSet<>();
        if (disks[slot] != null) {
            results.add(new DiskChange(true, compositeStorages.get(disks[slot].getStorageChannelType()), disks[slot]));
        }

        diskProvider.getStorageChannelType(slot).ifPresentOrElse(type -> {
            disks[slot] = diskProvider
                    .getDiskId(slot)
                    .flatMap(storageRepository::get)
                    .map(storage -> new DiskDriveDiskStorage(storage, type, listener))
                    .orElse(null);

            if (disks[slot] != null) {
                results.add(new DiskChange(false, compositeStorages.get(disks[slot].getStorageChannelType()), disks[slot]));
            }
        }, () -> disks[slot] = null);

        return results;
    }

    private void processDiskChange(DiskChange change) {
        if (!isActive()) {
            return;
        }
        if (change.removed) {
            change.compositeStorage.removeSource((Storage) change.storage);
        } else {
            change.compositeStorage.addSource((Storage) change.storage);
        }
    }

    private void updateDiskCount() {
        this.diskCount = (int) Arrays.stream(disks).filter(Objects::nonNull).count();
    }

    private record DiskChange(boolean removed,
                              DiskDriveCompositeStorage<?> compositeStorage,
                              DiskDriveDiskStorage<?> storage) {
    }

    @Override
    public void onActiveChanged(boolean active) {
        super.onActiveChanged(active);
        if (network == null) {
            return;
        }
        LOGGER.info("Disk drive activeness got changed to '{}', updating underlying storage", active);
        if (active) {
            enableAllDisks();
        } else {
            disableAllDisks();
        }
    }

    private void disableAllDisks() {
        compositeStorages.values().forEach(DiskDriveCompositeStorage::clearSources);
    }

    private void enableAllDisks() {
        compositeStorages.forEach(this::enableAllDisksForChannel);
    }

    private void enableAllDisksForChannel(StorageChannelType<?> type, DiskDriveCompositeStorage<?> composite) {
        for (int i = 0; i < DISK_COUNT; ++i) {
            DiskDriveDiskStorage<?> disk = disks[i];
            if (disk != null && disk.getStorageChannelType() == type) {
                composite.addSource((DiskDriveDiskStorage) disk);
            }
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

    public void setNormalizer(UnaryOperator<Object> normalizer) {
        filter.setNormalizer(normalizer);
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
        DiskDriveCompositeStorage<?> storage = compositeStorages.get(channelType);
        if (storage != null) {
            return Optional.of((Storage<T>) storage);
        }
        return Optional.empty();
    }
}
