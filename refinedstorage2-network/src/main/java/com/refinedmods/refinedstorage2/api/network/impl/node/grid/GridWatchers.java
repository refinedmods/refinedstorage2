package com.refinedmods.refinedstorage2.api.network.impl.node.grid;

import com.refinedmods.refinedstorage2.api.grid.GridWatcher;
import com.refinedmods.refinedstorage2.api.network.Network;
import com.refinedmods.refinedstorage2.api.network.component.StorageNetworkComponent;
import com.refinedmods.refinedstorage2.api.storage.Actor;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannel;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GridWatchers {
    public static final Logger LOGGER = LoggerFactory.getLogger(GridWatchers.class);

    private final Collection<? extends StorageChannelType<?>> storageChannelTypes;
    private final Map<GridWatcher, GridWatcherRegistration> watchers = new HashMap<>();

    public GridWatchers(final Collection<? extends StorageChannelType<?>> storageChannelTypes) {
        this.storageChannelTypes = storageChannelTypes;
    }

    private <T> StorageChannel<T> getStorageChannel(final Network network, final StorageChannelType<T> type) {
        return network.getComponent(StorageNetworkComponent.class).getStorageChannel(type);
    }

    public void addWatcher(
        final GridWatcher watcher,
        final Class<? extends Actor> actorType,
        final Network network
    ) {
        if (watchers.containsKey(watcher)) {
            throw new IllegalArgumentException("Watcher is already registered");
        }
        final GridWatcherRegistration registration = new GridWatcherRegistration(watcher, actorType);
        attachAll(registration, network);
        watchers.put(watcher, registration);
        LOGGER.info("Added watcher {}, new count is {}", watcher, watchers.size());
    }

    public void attachAll(final Network network) {
        watchers.forEach((watcher, registration) -> {
            watcher.clear();
            attachAll(registration, network);
        });
    }

    private void attachAll(final GridWatcherRegistration registration, final Network network) {
        storageChannelTypes.forEach(storageChannelType -> attach(registration, storageChannelType, network));
    }

    private <T> void attach(
        final GridWatcherRegistration registration,
        final StorageChannelType<T> storageChannelType,
        final Network network
    ) {
        LOGGER.info("Attaching {} to {}", registration, storageChannelType);
        registration.attach(getStorageChannel(network, storageChannelType), storageChannelType);
    }

    public void removeWatcher(final GridWatcher watcher, final Network network) {
        final GridWatcherRegistration registration = watchers.get(watcher);
        if (registration == null) {
            throw new IllegalArgumentException("Watcher is not registered");
        }
        detachAll(registration, network);
        watchers.remove(watcher);
        LOGGER.info("Removed watcher {}, remaining {}", watcher, watchers.size());
    }

    public void detachAll(final Network network) {
        LOGGER.info("Detaching {} watchers", watchers.size());
        watchers.values().forEach(w -> detachAll(w, network));
    }

    private void detachAll(final GridWatcherRegistration registration, final Network network) {
        storageChannelTypes.forEach(storageChannelType -> detach(registration, storageChannelType, network));
    }

    private <T> void detach(
        final GridWatcherRegistration registration,
        final StorageChannelType<T> storageChannelType,
        final Network network
    ) {
        LOGGER.info("Detaching {} from {}", registration, storageChannelType);
        registration.detach(getStorageChannel(network, storageChannelType), storageChannelType);
    }

    public void activeChanged(final boolean active) {
        watchers.keySet().forEach(watcher -> watcher.onActiveChanged(active));
    }
}
