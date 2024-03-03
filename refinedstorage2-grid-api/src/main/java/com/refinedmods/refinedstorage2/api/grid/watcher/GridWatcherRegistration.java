package com.refinedmods.refinedstorage2.api.grid.watcher;

import com.refinedmods.refinedstorage2.api.resource.list.listenable.ResourceListListener;
import com.refinedmods.refinedstorage2.api.storage.Actor;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannel;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;

import java.util.HashMap;
import java.util.Map;

class GridWatcherRegistration {
    private final GridWatcher watcher;
    private final Class<? extends Actor> actorType;
    private final Map<StorageChannelType, ResourceListListener> listeners = new HashMap<>();

    GridWatcherRegistration(final GridWatcher watcher, final Class<? extends Actor> actorType) {
        this.watcher = watcher;
        this.actorType = actorType;
    }

    void attach(final StorageChannel storageChannel,
                final StorageChannelType storageChannelType,
                final boolean replay) {
        final ResourceListListener listener = change -> watcher.onChanged(
            storageChannelType,
            change.resourceAmount().getResource(),
            change.change(),
            storageChannel.findTrackedResourceByActorType(
                change.resourceAmount().getResource(),
                actorType
            ).orElse(null)
        );
        storageChannel.addListener(listener);
        listeners.put(storageChannelType, listener);
        if (replay) {
            storageChannel.getAll().forEach(resourceAmount -> watcher.onChanged(
                storageChannelType,
                resourceAmount.getResource(),
                resourceAmount.getAmount(),
                storageChannel.findTrackedResourceByActorType(
                    resourceAmount.getResource(),
                    actorType
                ).orElse(null)
            ));
        }
    }

    void detach(final StorageChannel storageChannel, final StorageChannelType storageChannelType) {
        final ResourceListListener listener = listeners.get(storageChannelType);
        storageChannel.removeListener(listener);
        listeners.remove(storageChannelType);
    }
}
