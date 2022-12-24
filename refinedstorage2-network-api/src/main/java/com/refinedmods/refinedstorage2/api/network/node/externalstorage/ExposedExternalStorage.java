package com.refinedmods.refinedstorage2.api.network.node.externalstorage;

import com.refinedmods.refinedstorage2.api.network.node.AbstractConfiguredProxyStorage;
import com.refinedmods.refinedstorage2.api.network.node.StorageConfiguration;
import com.refinedmods.refinedstorage2.api.storage.composite.CompositeAwareChild;
import com.refinedmods.refinedstorage2.api.storage.composite.ConsumingStorage;
import com.refinedmods.refinedstorage2.api.storage.composite.ParentComposite;
import com.refinedmods.refinedstorage2.api.storage.external.ExternalStorage;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nullable;

public class ExposedExternalStorage<T> extends AbstractConfiguredProxyStorage<T, ExternalStorage<T>>
    implements ConsumingStorage<T>, CompositeAwareChild<T> {
    private final Set<ParentComposite<T>> parents = new HashSet<>();

    ExposedExternalStorage(final StorageConfiguration config) {
        super(config);
    }

    @Nullable
    public ExternalStorage<T> getDelegate() {
        return delegate;
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

    @Override
    public void setDelegate(final ExternalStorage<T> newDelegate) {
        super.setDelegate(newDelegate);
        parents.forEach(parent -> {
            parent.onSourceAddedToChild(newDelegate);
            newDelegate.onAddedIntoComposite(parent);
        });
    }

    @Override
    public void clearDelegate() {
        parents.forEach(parent -> {
            parent.onSourceRemovedFromChild(Objects.requireNonNull(delegate));
            delegate.onRemovedFromComposite(parent);
        });
        super.clearDelegate();
    }

    public boolean detectChanges() {
        return delegate != null && delegate.detectChanges();
    }
}
