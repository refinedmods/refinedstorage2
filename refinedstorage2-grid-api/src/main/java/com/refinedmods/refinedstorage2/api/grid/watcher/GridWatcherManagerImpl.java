package com.refinedmods.refinedstorage2.api.grid.watcher;

import com.refinedmods.refinedstorage2.api.storage.Actor;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;

import java.util.HashMap;
import java.util.Map;

import org.apiguardian.api.API;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: test.
@API(status = API.Status.STABLE, since = "2.0.0-milestone.3.3")
public class GridWatcherManagerImpl implements GridWatcherManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(GridWatcherManagerImpl.class);

    private final Map<GridWatcher, GridWatcherRegistration> watchers = new HashMap<>();

    @Override
    public void addWatcher(
        final GridWatcher watcher,
        final Class<? extends Actor> actorType,
        final GridStorageChannelProvider storageChannelProvider
    ) {
        if (watchers.containsKey(watcher)) {
            throw new IllegalArgumentException("Watcher is already registered");
        }
        final GridWatcherRegistration registration = new GridWatcherRegistration(watcher, actorType);
        attachAll(registration, storageChannelProvider, false);
        watchers.put(watcher, registration);
        LOGGER.info("Added watcher {}, new count is {}", watcher, watchers.size());
    }

    @Override
    public void attachAll(final GridStorageChannelProvider storageChannelProvider) {
        // If we get here we are affected by a network split or network merge.
        // At this point, all the storages that are affected by the split or merge have not yet been processed
        // as the grid has the highest priority.
        watchers.forEach((watcher, registration) -> {
            // Invalidate all watcher data, the resources that were synced earlier are no longer valid because we have
            // a brand-new network.
            watcher.invalidate();
            // Re-attach the watcher to the new network, and send all the resources from the new network.
            // Resources from the old network are not part of the new network yet, as mentioned above,
            // but those will be synced when the storages are re-added.
            attachAll(registration, storageChannelProvider, true);
        });
    }

    private void attachAll(final GridWatcherRegistration registration,
                           final GridStorageChannelProvider storageChannelProvider,
                           final boolean replay) {
        storageChannelProvider.getStorageChannelTypes().forEach(storageChannelType -> attach(
            registration,
            storageChannelType,
            storageChannelProvider,
            replay
        ));
    }

    private <T> void attach(
        final GridWatcherRegistration registration,
        final StorageChannelType<T> storageChannelType,
        final GridStorageChannelProvider storageChannelProvider,
        final boolean replay
    ) {
        LOGGER.info("Attaching {} to {}", registration, storageChannelType);
        registration.attach(storageChannelProvider.getStorageChannel(storageChannelType), storageChannelType, replay);
    }

    @Override
    public void removeWatcher(final GridWatcher watcher, final GridStorageChannelProvider storageChannelProvider) {
        final GridWatcherRegistration registration = watchers.get(watcher);
        if (registration == null) {
            throw new IllegalArgumentException("Watcher is not registered");
        }
        detachAll(registration, storageChannelProvider);
        watchers.remove(watcher);
        LOGGER.info("Removed watcher {}, remaining {}", watcher, watchers.size());
    }

    @Override
    public void detachAll(final GridStorageChannelProvider storageChannelProvider) {
        LOGGER.info("Detaching {} watchers", watchers.size());
        watchers.values().forEach(w -> detachAll(w, storageChannelProvider));
    }

    private void detachAll(final GridWatcherRegistration registration,
                           final GridStorageChannelProvider storageChannelProvider) {
        storageChannelProvider.getStorageChannelTypes().forEach(storageChannelType -> detach(
            registration,
            storageChannelType,
            storageChannelProvider
        ));
    }

    private <T> void detach(
        final GridWatcherRegistration registration,
        final StorageChannelType<T> storageChannelType,
        final GridStorageChannelProvider storageChannelProvider
    ) {
        LOGGER.info("Detaching {} from {}", registration, storageChannelType);
        registration.detach(storageChannelProvider.getStorageChannel(storageChannelType), storageChannelType);
    }

    @Override
    public void activeChanged(final boolean active) {
        watchers.keySet().forEach(watcher -> watcher.onActiveChanged(active));
    }
}
