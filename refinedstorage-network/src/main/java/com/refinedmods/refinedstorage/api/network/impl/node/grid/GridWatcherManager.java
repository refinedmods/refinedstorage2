package com.refinedmods.refinedstorage.api.network.impl.node.grid;

import com.refinedmods.refinedstorage.api.autocrafting.status.TaskStatusProvider;
import com.refinedmods.refinedstorage.api.network.node.grid.GridWatcher;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.api.storage.root.RootStorage;

import org.apiguardian.api.API;
import org.jspecify.annotations.Nullable;

/**
 * This manager helps with attaching and detaching listeners to
 * {@link RootStorage}s and {@link TaskStatusProvider}s.
 */
@API(status = API.Status.STABLE, since = "2.0.0-milestone.3.3")
public interface GridWatcherManager {
    void addWatcher(GridWatcher watcher,
                    Class<? extends Actor> actorType,
                    @Nullable RootStorage rootStorage,
                    @Nullable TaskStatusProvider taskStatusProvider);

    void attachAll(@Nullable RootStorage rootStorage, @Nullable TaskStatusProvider taskStatusProvider);

    void removeWatcher(GridWatcher watcher,
                       @Nullable RootStorage rootStorage,
                       @Nullable TaskStatusProvider taskStatusProvider);

    void detachAll(RootStorage rootStorage, @Nullable TaskStatusProvider taskStatusProvider);

    void activeChanged(boolean active);
}
