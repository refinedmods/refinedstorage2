package com.refinedmods.refinedstorage.api.network.impl.node.grid;

import com.refinedmods.refinedstorage.api.autocrafting.status.TaskStatusProvider;
import com.refinedmods.refinedstorage.api.network.node.grid.GridWatcher;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.api.storage.root.RootStorage;

import java.util.HashMap;
import java.util.Map;

import org.apiguardian.api.API;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.3.3")
public class GridWatcherManagerImpl implements GridWatcherManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(GridWatcherManagerImpl.class);

    private final Map<GridWatcher, GridWatcherRegistration> watchers = new HashMap<>();

    @Override
    public void addWatcher(
        final GridWatcher watcher,
        final Class<? extends Actor> actorType,
        @Nullable final RootStorage rootStorage,
        @Nullable final TaskStatusProvider taskStatusProvider
    ) {
        if (watchers.containsKey(watcher)) {
            throw new IllegalArgumentException("Watcher is already registered");
        }
        final GridWatcherRegistration registration = new GridWatcherRegistration(watcher, actorType);
        if (rootStorage != null) {
            attach(registration, rootStorage, false);
        }
        if (taskStatusProvider != null) {
            attach(registration, taskStatusProvider, false);
        }
        watchers.put(watcher, registration);
        LOGGER.debug("Added watcher {}, new count is {}", watcher, watchers.size());
    }

    @Override
    public void attachAll(@Nullable final RootStorage rootStorage,
                          @Nullable final TaskStatusProvider taskStatusProvider) {
        // If we get here we are affected by a network split or network merge.
        // At this point, all the storages that are affected by the split or merge have not yet been processed
        // as the grid has the highest priority.
        watchers.forEach((watcher, registration) -> {
            // Invalidate all watcher data, the resources that were synced earlier are no longer valid because we have
            // a brand-new network.
            watcher.invalidate();
            if (rootStorage != null) {
                // Re-attach the watcher to the new network, and send all the resources from the new network.
                // Resources from the old network are not part of the new network yet, as mentioned above,
                // but those will be synced when the storages are re-added.
                attach(registration, rootStorage, true);
            }
            if (taskStatusProvider != null) {
                attach(registration, taskStatusProvider, true);
            }
        });
    }

    private void attach(
        final GridWatcherRegistration registration,
        final RootStorage rootStorage,
        final boolean replay
    ) {
        LOGGER.debug("Attaching {} to {}", registration, rootStorage);
        registration.attach(rootStorage, replay);
    }

    private void attach(
        final GridWatcherRegistration registration,
        final TaskStatusProvider taskStatusProvider,
        final boolean replay
    ) {
        LOGGER.debug("Attaching {} to {}", registration, taskStatusProvider);
        registration.attach(taskStatusProvider, replay);
    }

    @Override
    public void removeWatcher(final GridWatcher watcher,
                              @Nullable final RootStorage rootStorage,
                              @Nullable final TaskStatusProvider taskStatusProvider) {
        final GridWatcherRegistration registration = watchers.get(watcher);
        if (registration == null) {
            throw new IllegalArgumentException("Watcher is not registered");
        }
        if (rootStorage != null) {
            detach(registration, rootStorage);
        }
        if (taskStatusProvider != null) {
            detach(registration, taskStatusProvider);
        }
        watchers.remove(watcher);
        LOGGER.debug("Removed watcher {}, remaining {}", watcher, watchers.size());
    }

    @Override
    public void detachAll(final RootStorage rootStorage, @Nullable final TaskStatusProvider taskStatusProvider) {
        LOGGER.debug("Detaching {} watchers", watchers.size());
        watchers.values().forEach(watcher -> {
            detach(watcher, rootStorage);
            if (taskStatusProvider != null) {
                detach(watcher, taskStatusProvider);
            }
        });
    }

    private void detach(final GridWatcherRegistration registration, final RootStorage rootStorage) {
        LOGGER.debug("Detaching {} from {}", registration, rootStorage);
        registration.detach(rootStorage);
    }

    private void detach(final GridWatcherRegistration registration, final TaskStatusProvider taskStatusProvider) {
        LOGGER.debug("Detaching {} from {}", registration, taskStatusProvider);
        registration.detach(taskStatusProvider);
    }

    @Override
    public void activeChanged(final boolean active) {
        watchers.keySet().forEach(watcher -> watcher.onActiveChanged(active));
    }
}
