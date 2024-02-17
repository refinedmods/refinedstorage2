package com.refinedmods.refinedstorage2.api.grid.watcher;

import com.refinedmods.refinedstorage2.api.storage.Actor;

import org.apiguardian.api.API;

/**
 * This manager helps with attaching and detaching listeners to
 * {@link com.refinedmods.refinedstorage2.api.storage.channel.StorageChannel}s.
 */
@API(status = API.Status.STABLE, since = "2.0.0-milestone.3.3")
public interface GridWatcherManager {
    void addWatcher(
        GridWatcher watcher,
        Class<? extends Actor> actorType,
        GridStorageChannelProvider storageChannelProvider
    );

    void attachAll(GridStorageChannelProvider storageChannelProvider);

    void removeWatcher(GridWatcher watcher, GridStorageChannelProvider storageChannelProvider);

    void detachAll(GridStorageChannelProvider storageChannelProvider);

    void activeChanged(boolean active);
}
