package com.refinedmods.refinedstorage.api.grid.watcher;

import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.api.storage.channel.StorageChannel;

import javax.annotation.Nullable;

import org.apiguardian.api.API;

/**
 * This manager helps with attaching and detaching listeners to
 * {@link com.refinedmods.refinedstorage.api.storage.channel.StorageChannel}s.
 */
@API(status = API.Status.STABLE, since = "2.0.0-milestone.3.3")
public interface GridWatcherManager {
    void addWatcher(GridWatcher watcher,
                    Class<? extends Actor> actorType,
                    @Nullable StorageChannel storageChannel);

    void attachAll(@Nullable StorageChannel storageChannel);

    void removeWatcher(GridWatcher watcher, @Nullable StorageChannel storageChannel);

    void detachAll(StorageChannel storageChannel);

    void activeChanged(boolean active);
}
