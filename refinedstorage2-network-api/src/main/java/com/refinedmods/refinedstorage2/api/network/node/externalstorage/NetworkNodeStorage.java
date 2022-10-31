package com.refinedmods.refinedstorage2.api.network.node.externalstorage;

import com.refinedmods.refinedstorage2.api.network.node.AbstractConfiguredProxyStorage;
import com.refinedmods.refinedstorage2.api.network.node.StorageConfiguration;
import com.refinedmods.refinedstorage2.api.storage.composite.CompositeAwareChild;
import com.refinedmods.refinedstorage2.api.storage.composite.ConsumingStorage;
import com.refinedmods.refinedstorage2.api.storage.composite.ParentComposite;
import com.refinedmods.refinedstorage2.api.storage.external.ExternalStorage;

import java.util.HashSet;
import java.util.Set;

class NetworkNodeStorage<T> extends AbstractConfiguredProxyStorage<T, ExternalStorage<T>>
    implements ConsumingStorage<T>, CompositeAwareChild<T> {
    private final Set<ParentComposite<T>> parents = new HashSet<>();

    NetworkNodeStorage(final StorageConfiguration config) {
        super(config);
    }

    @Override
    public void onAddedIntoComposite(final ParentComposite<T> parentComposite) {
        parents.add(parentComposite);
        if (delegate != null) {
            delegate.onAddedIntoComposite(parentComposite);
        }
    }

    @Override
    public void onRemovedFromComposite(final ParentComposite<T> parentComposite) {
        parents.remove(parentComposite);
        if (delegate != null) {
            delegate.onRemovedFromComposite(parentComposite);
        }
    }

    public void setStorage(final ExternalStorage<T> storage) {
        if (this.delegate != null) {
            throw new IllegalStateException("Storage is already set");
        }
        this.delegate = storage;
        parents.forEach(parent -> {
            parent.onSourceAddedToChild(storage);
            storage.onAddedIntoComposite(parent);
        });
    }

    public void tryRemoveStorage() {
        if (delegate == null) {
            return;
        }
        parents.forEach(parent -> {
            parent.onSourceRemovedFromChild(delegate);
            delegate.onRemovedFromComposite(parent);
        });
        this.delegate = null;
    }

    public boolean detectChanges() {
        return delegate != null && delegate.detectChanges();
    }
}
