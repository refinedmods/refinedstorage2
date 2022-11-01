package com.refinedmods.refinedstorage2.api.network.node.diskdrive;

import com.refinedmods.refinedstorage2.api.core.registry.OrderedRegistry;
import com.refinedmods.refinedstorage2.api.network.component.StorageProvider;
import com.refinedmods.refinedstorage2.api.network.node.AbstractStorageNetworkNode;
import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.StorageRepository;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DiskDriveNetworkNode extends AbstractStorageNetworkNode implements StorageProvider {
    private static final Logger LOGGER = LogManager.getLogger(DiskDriveNetworkNode.class);

    @Nullable
    private StorageRepository storageRepository;
    @Nullable
    private StorageDiskProvider diskProvider;
    @Nullable
    private DiskDriveListener listener;

    private final long energyUsage;
    private final long energyUsagePerDisk;

    private final DiskDriveDiskStorage<?>[] disks;
    private final Map<StorageChannelType<?>, DiskDriveCompositeStorage<?>> compositeStorages;
    private int diskCount;

    public DiskDriveNetworkNode(final long energyUsage,
                                final long energyUsagePerDisk,
                                final OrderedRegistry<?, StorageChannelType<?>> storageChannelTypeRegistry,
                                final int diskCount) {
        this.energyUsage = energyUsage;
        this.energyUsagePerDisk = energyUsagePerDisk;
        this.compositeStorages = createCompositeStorages(storageChannelTypeRegistry);
        this.disks = new DiskDriveDiskStorage[diskCount];
    }

    private Map<StorageChannelType<?>, DiskDriveCompositeStorage<?>> createCompositeStorages(
        final OrderedRegistry<?, StorageChannelType<?>> storageChannelTypeRegistry
    ) {
        return storageChannelTypeRegistry
            .getAll()
            .stream()
            .collect(Collectors.toUnmodifiableMap(Function.identity(), this::createCompositeStorage));
    }

    private DiskDriveCompositeStorage<?> createCompositeStorage(final StorageChannelType<?> type) {
        return new DiskDriveCompositeStorage<>(this);
    }

    public void initialize(final StorageRepository newStorageRepository) {
        // Avoid initializing multiple times, this causes problems with already initialized storages going out of sync
        // with the composite storage (object reference changes).
        if (diskCount > 0) {
            return;
        }
        this.storageRepository = newStorageRepository;
        for (int i = 0; i < disks.length; ++i) {
            initializeDiskInSlot(i);
        }
        updateDiskCount();
    }

    public void onDiskChanged(final int slot) {
        if (slot < 0 || slot >= disks.length) {
            LOGGER.warn("Tried to change disk in invalid slot {}", slot);
            return;
        }
        initializeDiskInSlot(slot).forEach(this::processDiskChange);
        updateDiskCount();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private Set<DiskChange> initializeDiskInSlot(final int slot) {
        final Set<DiskChange> results = new HashSet<>();
        if (disks[slot] != null) {
            final StorageChannelType<?> removedStorageChannelType = disks[slot].getStorageChannelType();
            final DiskDriveCompositeStorage<?> removedStorage = compositeStorages.get(removedStorageChannelType);
            results.add(new DiskChange(true, removedStorage, disks[slot]));
        }

        if (diskProvider != null && storageRepository != null) {
            diskProvider.getStorageChannelType(slot).ifPresentOrElse(type -> {
                disks[slot] = diskProvider
                    .getDiskId(slot)
                    .flatMap(storageRepository::get)
                    .map(storage -> new DiskDriveDiskStorage(storage, type, listener))
                    .orElse(null);

                if (disks[slot] != null) {
                    final StorageChannelType<?> addedStorageChannelType = disks[slot].getStorageChannelType();
                    final DiskDriveCompositeStorage<?> addedStorage = compositeStorages.get(addedStorageChannelType);
                    results.add(new DiskChange(false, addedStorage, disks[slot]));
                }
            }, () -> disks[slot] = null);
        }

        return results;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void processDiskChange(final DiskChange change) {
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

    @Override
    protected void onActiveChanged(final boolean newActive) {
        super.onActiveChanged(newActive);
        if (network == null) {
            return;
        }
        LOGGER.info("Disk drive activeness got changed to '{}', updating underlying storage", newActive);
        if (newActive) {
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

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void enableAllDisksForChannel(final StorageChannelType<?> type,
                                          final DiskDriveCompositeStorage<?> composite) {
        for (final DiskDriveDiskStorage<?> disk : disks) {
            if (disk != null && disk.getStorageChannelType() == type) {
                composite.addSource((DiskDriveDiskStorage) disk);
            }
        }
    }

    public void setDiskProvider(final StorageDiskProvider diskProvider) {
        this.diskProvider = diskProvider;
    }

    public void setListener(final DiskDriveListener listener) {
        this.listener = listener;
    }

    @Override
    public long getEnergyUsage() {
        return energyUsage + (energyUsagePerDisk * diskCount);
    }

    public DiskDriveState createState() {
        return DiskDriveState.of(disks.length, idx -> getState(disks[idx]));
    }

    private StorageDiskState getState(@Nullable final DiskDriveDiskStorage<?> disk) {
        if (disk == null) {
            return StorageDiskState.NONE;
        } else if (!isActive()) {
            return StorageDiskState.INACTIVE;
        }
        return disk.getState();
    }

    @Override
    protected Set<StorageChannelType<?>> getRelevantStorageChannelTypes() {
        return compositeStorages.keySet();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<Storage<T>> getStorageForChannel(final StorageChannelType<T> channelType) {
        final DiskDriveCompositeStorage<?> storage = compositeStorages.get(channelType);
        if (storage != null) {
            return Optional.of((Storage<T>) storage);
        }
        return Optional.empty();
    }

    public int getAmountOfDiskSlots() {
        return disks.length;
    }

    private record DiskChange(boolean removed,
                              DiskDriveCompositeStorage<?> compositeStorage,
                              DiskDriveDiskStorage<?> storage) {
    }
}
