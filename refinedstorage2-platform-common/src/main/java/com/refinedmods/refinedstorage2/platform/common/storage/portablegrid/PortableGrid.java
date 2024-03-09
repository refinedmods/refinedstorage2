package com.refinedmods.refinedstorage2.platform.common.storage.portablegrid;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.grid.operations.GridOperations;
import com.refinedmods.refinedstorage2.api.grid.operations.NoopGridOperations;
import com.refinedmods.refinedstorage2.api.grid.watcher.GridWatcher;
import com.refinedmods.refinedstorage2.api.grid.watcher.GridWatcherManager;
import com.refinedmods.refinedstorage2.api.grid.watcher.GridWatcherManagerImpl;
import com.refinedmods.refinedstorage2.api.network.energy.EnergyStorage;
import com.refinedmods.refinedstorage2.api.storage.Actor;
import com.refinedmods.refinedstorage2.api.storage.NoopStorage;
import com.refinedmods.refinedstorage2.api.storage.StateTrackedStorage;
import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.StorageState;
import com.refinedmods.refinedstorage2.api.storage.TrackedResourceAmount;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannel;
import com.refinedmods.refinedstorage2.platform.api.grid.Grid;
import com.refinedmods.refinedstorage2.platform.api.support.resource.ResourceType;
import com.refinedmods.refinedstorage2.platform.common.Platform;
import com.refinedmods.refinedstorage2.platform.common.storage.DiskInventory;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;

class PortableGrid implements Grid {
    private final EnergyStorage energyStorage;
    private final DiskInventory diskInventory;
    private final GridWatcherManager watchers = new GridWatcherManagerImpl();
    private final StateTrackedStorage.Listener diskListener;
    @Nullable
    private PortableGridStorage storage;

    PortableGrid(final EnergyStorage energyStorage,
                 final DiskInventory diskInventory,
                 final StateTrackedStorage.Listener diskListener) {
        this.energyStorage = energyStorage;
        this.diskInventory = diskInventory;
        this.diskListener = diskListener;
    }

    void updateStorage() {
        if (storage != null) {
            watchers.detachAll(storage.getStorageChannel());
        }

        this.storage = diskInventory.resolve(0)
            .map(diskStorage -> new StateTrackedStorage(diskStorage, diskListener))
            .map(PortableGridStorage::new)
            .orElse(null);

        watchers.attachAll(getStorageChannel());
    }

    void activeChanged(final boolean active) {
        watchers.activeChanged(active);
    }

    StorageState getStorageState() {
        if (storage == null) {
            return StorageState.NONE;
        }
        if (!isGridActive()) {
            return StorageState.INACTIVE;
        }
        return storage.getState();
    }

    @Override
    public void addWatcher(final GridWatcher watcher, final Class<? extends Actor> actorType) {
        energyStorage.extract(Platform.INSTANCE.getConfig().getPortableGrid().getOpenEnergyUsage(), Action.EXECUTE);
        watchers.addWatcher(watcher, actorType, getStorageChannel());
    }

    @Override
    public void removeWatcher(final GridWatcher watcher) {
        watchers.removeWatcher(watcher, getStorageChannel());
    }

    @Nullable
    private StorageChannel getStorageChannel() {
        return storage != null ? storage.getStorageChannel() : null;
    }

    @Override
    public Storage getItemStorage() {
        if (storage == null) {
            return new NoopStorage();
        }
        return storage.getStorageChannel();
    }

    @Override
    public boolean isGridActive() {
        return energyStorage.getStored() > 0 && storage != null;
    }

    @Override
    public List<TrackedResourceAmount> getResources(final Class<? extends Actor> actorType) {
        if (storage == null) {
            return Collections.emptyList();
        }
        final StorageChannel storageChannel = storage.getStorageChannel();
        return storageChannel.getAll().stream().map(resource -> new TrackedResourceAmount(
            resource,
            storageChannel.findTrackedResourceByActorType(resource.getResource(), actorType).orElse(null)
        )).toList();
    }

    @Override
    public GridOperations createOperations(final ResourceType resourceType,
                                           final Actor actor) {
        if (storage == null) {
            return new NoopGridOperations();
        }
        final StorageChannel storageChannel = this.storage.getStorageChannel();
        final GridOperations operations = resourceType.createGridOperations(storageChannel, actor);
        return new PortableGridOperations(operations, energyStorage);
    }
}
