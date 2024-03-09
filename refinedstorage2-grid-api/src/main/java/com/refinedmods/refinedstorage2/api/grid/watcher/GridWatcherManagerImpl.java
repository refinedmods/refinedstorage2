package com.refinedmods.refinedstorage2.api.grid.watcher;

import com.refinedmods.refinedstorage2.api.storage.Actor;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannel;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;

import org.apiguardian.api.API;
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
        @Nullable final StorageChannel storageChannel
    ) {
        if (watchers.containsKey(watcher)) {
            throw new IllegalArgumentException("Watcher is already registered");
        }
        final GridWatcherRegistration registration = new GridWatcherRegistration(watcher, actorType);
        if (storageChannel != null) {
            attach(registration, storageChannel, false);
        }
        watchers.put(watcher, registration);
        LOGGER.info("Added watcher {}, new count is {}", watcher, watchers.size());
    }

    @Override
    public void attachAll(@Nullable final StorageChannel storageChannel) {
        // If we get here we are affected by a network split or network merge.
        // At this point, all the storages that are affected by the split or merge have not yet been processed
        // as the grid has the highest priority.
        watchers.forEach((watcher, registration) -> {
            // Invalidate all watcher data, the resources that were synced earlier are no longer valid because we have
            // a brand-new network.
            watcher.invalidate();
            if (storageChannel != null) {
                // Re-attach the watcher to the new network, and send all the resources from the new network.
                // Resources from the old network are not part of the new network yet, as mentioned above,
                // but those will be synced when the storages are re-added.
                attach(registration, storageChannel, true);
            }
        });
    }

    private void attach(
        final GridWatcherRegistration registration,
        final StorageChannel storageChannel,
        final boolean replay
    ) {
        LOGGER.info("Attaching {} to {}", registration, storageChannel);
        registration.attach(storageChannel, replay);
    }

    @Override
    public void removeWatcher(final GridWatcher watcher, @Nullable final StorageChannel storageChannel) {
        final GridWatcherRegistration registration = watchers.get(watcher);
        if (registration == null) {
            throw new IllegalArgumentException("Watcher is not registered");
        }
        if (storageChannel != null) {
            detach(registration, storageChannel);
        }
        watchers.remove(watcher);
        LOGGER.info("Removed watcher {}, remaining {}", watcher, watchers.size());
    }

    @Override
    public void detachAll(final StorageChannel storageChannel) {
        LOGGER.info("Detaching {} watchers", watchers.size());
        watchers.values().forEach(watcher -> detach(watcher, storageChannel));
    }

    private void detach(final GridWatcherRegistration registration, final StorageChannel storageChannel) {
        LOGGER.info("Detaching {} from {}", registration, storageChannel);
        registration.detach(storageChannel);
    }

    @Override
    public void activeChanged(final boolean active) {
        watchers.keySet().forEach(watcher -> watcher.onActiveChanged(active));
    }
}
