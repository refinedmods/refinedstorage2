package com.refinedmods.refinedstorage.common.storage.portablegrid;

import com.refinedmods.refinedstorage.api.autocrafting.preview.Preview;
import com.refinedmods.refinedstorage.api.autocrafting.task.TaskId;
import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.network.energy.EnergyStorage;
import com.refinedmods.refinedstorage.api.network.impl.node.grid.GridWatcherManager;
import com.refinedmods.refinedstorage.api.network.impl.node.grid.GridWatcherManagerImpl;
import com.refinedmods.refinedstorage.api.network.node.grid.EmptyGridOperations;
import com.refinedmods.refinedstorage.api.network.node.grid.GridOperations;
import com.refinedmods.refinedstorage.api.network.node.grid.GridWatcher;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.api.storage.NoopStorage;
import com.refinedmods.refinedstorage.api.storage.StateTrackedStorage;
import com.refinedmods.refinedstorage.api.storage.Storage;
import com.refinedmods.refinedstorage.api.storage.StorageState;
import com.refinedmods.refinedstorage.api.storage.TrackedResourceAmount;
import com.refinedmods.refinedstorage.api.storage.root.RootStorage;
import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.api.grid.Grid;
import com.refinedmods.refinedstorage.common.api.storage.PlayerActor;
import com.refinedmods.refinedstorage.common.api.support.resource.PlatformResourceKey;
import com.refinedmods.refinedstorage.common.api.support.resource.ResourceType;
import com.refinedmods.refinedstorage.common.storage.DiskInventory;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;

import net.minecraft.server.level.ServerPlayer;

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
            watchers.detachAll(storage.getRootStorage());
        }

        this.storage = diskInventory.resolve(0)
            .map(diskStorage -> new StateTrackedStorage(diskStorage, diskListener))
            .map(PortableGridStorage::new)
            .orElse(null);

        watchers.attachAll(getRootStorage());
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
        watchers.addWatcher(watcher, actorType, getRootStorage());
    }

    @Override
    public void removeWatcher(final GridWatcher watcher) {
        watchers.removeWatcher(watcher, getRootStorage());
    }

    @Nullable
    private RootStorage getRootStorage() {
        return storage != null ? storage.getRootStorage() : null;
    }

    @Override
    public Storage getItemStorage() {
        if (storage == null) {
            return new NoopStorage();
        }
        return storage.getRootStorage();
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
        final RootStorage rootStorage = storage.getRootStorage();
        return rootStorage.getAll().stream().map(resource -> new TrackedResourceAmount(
            resource,
            rootStorage.findTrackedResourceByActorType(resource.resource(), actorType).orElse(null)
        )).toList();
    }

    @Override
    public Set<PlatformResourceKey> getAutocraftableResources() {
        return Collections.emptySet();
    }

    @Override
    public GridOperations createOperations(final ResourceType resourceType, final ServerPlayer player) {
        if (storage == null) {
            return EmptyGridOperations.INSTANCE;
        }
        final RootStorage rootStorage = this.storage.getRootStorage();
        final GridOperations operations = resourceType.createGridOperations(rootStorage, new PlayerActor(player));
        return new PortableGridOperations(operations, energyStorage);
    }

    @Override
    public CompletableFuture<Optional<Preview>> getPreview(final ResourceKey resource, final long amount) {
        return CompletableFuture.completedFuture(Optional.empty());
    }

    @Override
    public CompletableFuture<Long> getMaxAmount(final ResourceKey resource) {
        return CompletableFuture.completedFuture(0L);
    }

    @Override
    public CompletableFuture<Optional<TaskId>> startTask(final ResourceKey resource,
                                                         final long amount,
                                                         final Actor actor,
                                                         final boolean notify) {
        return CompletableFuture.completedFuture(Optional.empty());
    }

    @Override
    public void cancel() {
        // no op
    }
}
