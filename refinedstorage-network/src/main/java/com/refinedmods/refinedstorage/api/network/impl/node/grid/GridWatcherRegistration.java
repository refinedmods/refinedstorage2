package com.refinedmods.refinedstorage.api.network.impl.node.grid;

import com.refinedmods.refinedstorage.api.autocrafting.status.TaskStatusProvider;
import com.refinedmods.refinedstorage.api.network.node.grid.GridWatcher;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.api.storage.root.RootStorage;
import com.refinedmods.refinedstorage.api.storage.root.RootStorageListener;
import com.refinedmods.refinedstorage.api.storage.tracked.TrackedResource;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;

class GridWatcherRegistration {
    private final GridWatcher watcher;
    private final Class<? extends Actor> actorType;
    @Nullable
    private RootStorageListener listener;

    GridWatcherRegistration(final GridWatcher watcher, final Class<? extends Actor> actorType) {
        this.watcher = watcher;
        this.actorType = actorType;
    }

    void attach(final RootStorage rootStorage, final boolean replay) {
        this.listener = new GridWatcherRootStorageListener(watcher, rootStorage, actorType);
        rootStorage.addListener(listener);
        if (replay) {
            replay(rootStorage);
        }
    }

    void attach(final TaskStatusProvider taskStatusProvider, final boolean replay) {
        taskStatusProvider.addListener(watcher);
        if (replay) {
            taskStatusProvider.getStatuses().forEach(watcher::taskAdded);
        }
    }

    private void replay(final RootStorage rootStorage) {
        final Collection<ResourceAmount> all = rootStorage.getAll();
        final Set<ResourceKey> resources = all.stream().map(ResourceAmount::resource).collect(Collectors.toSet());
        final Map<ResourceKey, TrackedResource> trackedResources =
            rootStorage.getTrackedResourcesByActorType(actorType, resources);
        all.forEach(resourceAmount -> watcher.onChanged(
            resourceAmount.resource(),
            resourceAmount.amount(),
            trackedResources.get(resourceAmount.resource())
        ));
    }

    void detach(final RootStorage rootStorage) {
        if (listener == null) {
            return;
        }
        rootStorage.removeListener(listener);
        listener = null;
    }

    void detach(final TaskStatusProvider taskStatusProvider) {
        taskStatusProvider.removeListener(watcher);
    }
}
