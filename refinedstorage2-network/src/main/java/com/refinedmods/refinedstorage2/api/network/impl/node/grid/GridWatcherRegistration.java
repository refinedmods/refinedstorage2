package com.refinedmods.refinedstorage2.api.network.impl.node.grid;

import com.refinedmods.refinedstorage2.api.grid.GridWatcher;
import com.refinedmods.refinedstorage2.api.resource.list.listenable.ResourceListListener;
import com.refinedmods.refinedstorage2.api.storage.Actor;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannel;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;

import java.util.HashMap;
import java.util.Map;

class GridWatcherRegistration {
    private final GridWatcher watcher;
    private final Class<? extends Actor> actorType;
    private final Map<StorageChannelType<?>, ResourceListListener<?>> listeners = new HashMap<>();

    GridWatcherRegistration(final GridWatcher watcher, final Class<? extends Actor> actorType) {
        this.watcher = watcher;
        this.actorType = actorType;
    }

    <T> void attach(final StorageChannel<T> storageChannel, final StorageChannelType<T> storageChannelType) {
        final ResourceListListener<T> listener = change -> watcher.onChanged(
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
    }

    @SuppressWarnings("unchecked")
    <T> void detach(final StorageChannel<T> storageChannel, final StorageChannelType<T> storageChannelType) {
        final ResourceListListener<T> listener = (ResourceListListener<T>) listeners.get(storageChannelType);
        storageChannel.removeListener(listener);
        listeners.remove(storageChannelType);
    }
}
