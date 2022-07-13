package com.refinedmods.refinedstorage2.api.network.node.storage;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.core.CoreValidations;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.storage.AccessMode;
import com.refinedmods.refinedstorage2.api.storage.Actor;
import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.composite.CompositeAwareChild;
import com.refinedmods.refinedstorage2.api.storage.composite.ParentComposite;
import com.refinedmods.refinedstorage2.api.storage.composite.Priority;
import com.refinedmods.refinedstorage2.api.storage.limited.LimitedStorage;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedResource;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedStorage;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;

class NetworkNodeStorage<T> implements TrackedStorage<T>, Priority, CompositeAwareChild<T> {
    private final StorageNetworkNode<T> networkNode;
    private final Set<ParentComposite<T>> parentComposites = new HashSet<>();
    @Nullable
    private Storage<T> storage;

    NetworkNodeStorage(final StorageNetworkNode<T> networkNode) {
        this.networkNode = networkNode;
    }

    @Override
    public long extract(final T resource, final long amount, final Action action, final Actor actor) {
        if (storage == null || networkNode.getAccessMode() == AccessMode.INSERT || !networkNode.isActive()) {
            return 0;
        }
        return storage.extract(resource, amount, action, actor);
    }

    @Override
    public long insert(final T resource, final long amount, final Action action, final Actor actor) {
        if (storage == null
            || networkNode.getAccessMode() == AccessMode.EXTRACT
            || !networkNode.isActive()
            || !networkNode.isAllowed(resource)) {
            return 0;
        }
        return storage.insert(resource, amount, action, actor);
    }

    @Override
    public Optional<TrackedResource> findTrackedResourceBySourceType(final T resource,
                                                                     final Class<? extends Actor> actorType) {
        return storage instanceof TrackedStorage<T> trackedStorage
            ? trackedStorage.findTrackedResourceBySourceType(resource, actorType)
            : Optional.empty();
    }

    @Override
    public int getPriority() {
        return networkNode.getPriority();
    }

    @Override
    public void onAddedIntoComposite(final ParentComposite<T> parentComposite) {
        parentComposites.add(parentComposite);
    }

    @Override
    public void onRemovedFromComposite(final ParentComposite<T> parentComposite) {
        parentComposites.remove(parentComposite);
    }

    @Override
    public Collection<ResourceAmount<T>> getAll() {
        return storage == null ? Collections.emptySet() : storage.getAll();
    }

    @Override
    public long getStored() {
        return storage == null ? 0L : storage.getStored();
    }

    public long getCapacity() {
        return storage instanceof LimitedStorage<?> limitedStorage ? limitedStorage.getCapacity() : 0L;
    }

    public void setSource(final Storage<T> source) {
        CoreValidations.validateNotNull(source, "Source cannot be null");
        this.storage = source;
        parentComposites.forEach(parentComposite -> parentComposite.onSourceAddedToChild(storage));
    }

    public void removeSource() {
        CoreValidations.validateNotNull(this.storage, "Cannot remove source when no source was present");
        parentComposites.forEach(parentComposite -> parentComposite.onSourceRemovedFromChild(this.storage));
        this.storage = null;
    }
}
