package com.refinedmods.refinedstorage2.api.grid.watcher;

import com.refinedmods.refinedstorage2.api.resource.list.listenable.ResourceListListener;
import com.refinedmods.refinedstorage2.api.storage.Actor;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannel;

import javax.annotation.Nullable;

class GridWatcherRegistration {
    private final GridWatcher watcher;
    private final Class<? extends Actor> actorType;
    @Nullable
    private ResourceListListener listener;

    GridWatcherRegistration(final GridWatcher watcher, final Class<? extends Actor> actorType) {
        this.watcher = watcher;
        this.actorType = actorType;
    }

    void attach(final StorageChannel storageChannel, final boolean replay) {
        this.listener = change -> watcher.onChanged(
            change.resourceAmount().getResource(),
            change.change(),
            storageChannel.findTrackedResourceByActorType(
                change.resourceAmount().getResource(),
                actorType
            ).orElse(null)
        );
        storageChannel.addListener(listener);
        if (replay) {
            storageChannel.getAll().forEach(resourceAmount -> watcher.onChanged(
                resourceAmount.getResource(),
                resourceAmount.getAmount(),
                storageChannel.findTrackedResourceByActorType(
                    resourceAmount.getResource(),
                    actorType
                ).orElse(null)
            ));
        }
    }

    void detach(final StorageChannel storageChannel) {
        if (listener == null) {
            return;
        }
        storageChannel.removeListener(listener);
        listener = null;
    }
}
