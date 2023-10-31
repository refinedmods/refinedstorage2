package com.refinedmods.refinedstorage2.platform.api.grid;

import com.refinedmods.refinedstorage2.api.grid.GridWatcher;
import com.refinedmods.refinedstorage2.api.grid.operations.GridOperations;
import com.refinedmods.refinedstorage2.api.storage.Actor;
import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.TrackedResourceAmount;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;
import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.api.storage.channel.PlatformStorageChannelType;

import java.util.List;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.3.0")
public interface Grid {
    void addWatcher(GridWatcher watcher, Class<? extends Actor> actorType);

    void removeWatcher(GridWatcher watcher);

    Storage<ItemResource> getItemStorage();

    boolean isGridActive();

    <T> List<TrackedResourceAmount<T>> getResources(StorageChannelType<T> type, Class<? extends Actor> actorType);

    <T> GridOperations<T> createOperations(PlatformStorageChannelType<T> storageChannelType, Actor actor);
}
