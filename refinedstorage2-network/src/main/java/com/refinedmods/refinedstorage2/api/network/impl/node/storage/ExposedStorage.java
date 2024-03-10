package com.refinedmods.refinedstorage2.api.network.impl.node.storage;

import com.refinedmods.refinedstorage2.api.network.impl.storage.AbstractConfiguredProxyStorage;
import com.refinedmods.refinedstorage2.api.network.impl.storage.StorageConfiguration;
import com.refinedmods.refinedstorage2.api.resource.ResourceKey;
import com.refinedmods.refinedstorage2.api.storage.Actor;
import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.composite.ParentComposite;
import com.refinedmods.refinedstorage2.api.storage.limited.LimitedStorage;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedResource;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedStorage;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

class ExposedStorage extends AbstractConfiguredProxyStorage<Storage> implements TrackedStorage {
    private final Set<ParentComposite> parents = new HashSet<>();

    ExposedStorage(final StorageConfiguration config) {
        super(config);
    }

    @Override
    public Optional<TrackedResource> findTrackedResourceByActorType(final ResourceKey resource,
                                                                    final Class<? extends Actor> actorType) {
        return getUnsafeDelegate() instanceof TrackedStorage trackedStorage
            ? trackedStorage.findTrackedResourceByActorType(resource, actorType)
            : Optional.empty();
    }

    @Override
    public void onAddedIntoComposite(final ParentComposite parentComposite) {
        parents.add(parentComposite);
    }

    @Override
    public void onRemovedFromComposite(final ParentComposite parentComposite) {
        parents.remove(parentComposite);
    }

    public long getCapacity() {
        return getUnsafeDelegate() instanceof LimitedStorage limitedStorage
            ? limitedStorage.getCapacity()
            : 0L;
    }

    @Override
    public void setDelegate(final Storage newDelegate) {
        super.setDelegate(newDelegate);
        parents.forEach(parent -> parent.onSourceAddedToChild(newDelegate));
    }

    @Override
    public void clearDelegate() {
        final Storage delegate = getDelegate();
        parents.forEach(parent -> parent.onSourceRemovedFromChild(delegate));
        super.clearDelegate();
    }
}
