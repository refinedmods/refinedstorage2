package com.refinedmods.refinedstorage.api.network.impl.node.grid;

import com.refinedmods.refinedstorage.api.network.node.grid.GridWatcher;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.api.storage.root.RootStorage;

import javax.annotation.Nullable;

import org.apiguardian.api.API;

/**
 * This manager helps with attaching and detaching listeners to
 * {@link RootStorage}s.
 */
@API(status = API.Status.STABLE, since = "2.0.0-milestone.3.3")
public interface GridWatcherManager {
    void addWatcher(GridWatcher watcher, Class<? extends Actor> actorType, @Nullable RootStorage rootStorage);

    void attachAll(@Nullable RootStorage rootStorage);

    void removeWatcher(GridWatcher watcher, @Nullable RootStorage rootStorage);

    void detachAll(RootStorage rootStorage);

    void activeChanged(boolean active);
}
