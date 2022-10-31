package com.refinedmods.refinedstorage2.api.network.node.externalstorage;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.storage.AccessMode;
import com.refinedmods.refinedstorage2.api.storage.Actor;
import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.composite.CompositeAwareChild;
import com.refinedmods.refinedstorage2.api.storage.composite.ConsumingStorage;
import com.refinedmods.refinedstorage2.api.storage.composite.ParentComposite;
import com.refinedmods.refinedstorage2.api.storage.composite.Priority;
import com.refinedmods.refinedstorage2.api.storage.external.ExternalStorage;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nullable;

class NetworkNodeStorage<T> implements Storage<T>, ConsumingStorage<T>, CompositeAwareChild<T>, Priority {
    private final ExternalStorageNetworkNode networkNode;
    private final Set<ParentComposite<T>> parents = new HashSet<>();
    @Nullable
    private ExternalStorage<T> storage;

    NetworkNodeStorage(final ExternalStorageNetworkNode networkNode) {
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
    public Collection<ResourceAmount<T>> getAll() {
        return storage == null ? Collections.emptySet() : storage.getAll();
    }

    @Override
    public long getStored() {
        return storage == null ? 0 : storage.getStored();
    }

    @Override
    public void onAddedIntoComposite(final ParentComposite<T> parentComposite) {
        parents.add(parentComposite);
        if (storage != null) {
            storage.onAddedIntoComposite(parentComposite);
        }
    }

    @Override
    public void onRemovedFromComposite(final ParentComposite<T> parentComposite) {
        parents.remove(parentComposite);
        if (storage != null) {
            storage.onRemovedFromComposite(parentComposite);
        }
    }

    public void setStorage(final ExternalStorage<T> storage) {
        if (this.storage != null) {
            throw new IllegalStateException("Storage is already set");
        }
        this.storage = storage;
        parents.forEach(parent -> {
            parent.onSourceAddedToChild(storage);
            storage.onAddedIntoComposite(parent);
        });
    }

    public void tryRemoveStorage() {
        if (storage == null) {
            return;
        }
        parents.forEach(parent -> {
            parent.onSourceRemovedFromChild(storage);
            storage.onRemovedFromComposite(parent);
        });
        this.storage = null;
    }

    public boolean detectChanges() {
        return storage != null && storage.detectChanges();
    }

    @Override
    public int getPriority() {
        return networkNode.getPriority();
    }
}
