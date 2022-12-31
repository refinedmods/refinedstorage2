package com.refinedmods.refinedstorage2.api.network.impl.node.externalstorage;

import com.refinedmods.refinedstorage2.api.network.node.AbstractConfiguredProxyStorage;
import com.refinedmods.refinedstorage2.api.network.node.StorageConfiguration;
import com.refinedmods.refinedstorage2.api.storage.Actor;
import com.refinedmods.refinedstorage2.api.storage.composite.CompositeAwareChild;
import com.refinedmods.refinedstorage2.api.storage.composite.ConsumingStorage;
import com.refinedmods.refinedstorage2.api.storage.composite.ParentComposite;
import com.refinedmods.refinedstorage2.api.storage.external.ExternalStorage;
import com.refinedmods.refinedstorage2.api.storage.external.ExternalStorageListener;
import com.refinedmods.refinedstorage2.api.storage.external.ExternalStorageProvider;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedResource;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedStorage;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedStorageRepository;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.LongSupplier;
import javax.annotation.Nullable;

public class ExposedExternalStorage<T> extends AbstractConfiguredProxyStorage<T, ExternalStorage<T>>
    implements ConsumingStorage<T>, CompositeAwareChild<T>, TrackedStorage<T>, ExternalStorageListener<T> {
    private final Set<ParentComposite<T>> parents = new HashSet<>();
    private final TrackedStorageRepository<T> trackingRepository;
    private final LongSupplier clock;

    ExposedExternalStorage(final StorageConfiguration config,
                           final TrackedStorageRepository<T> trackingRepository,
                           final LongSupplier clock) {
        super(config);
        this.trackingRepository = trackingRepository;
        this.clock = clock;
    }

    @Nullable
    public ExternalStorageProvider<T> getExternalStorageProvider() {
        final ExternalStorage<T> delegate = getUnsafeDelegate();
        if (delegate == null) {
            return null;
        }
        return delegate.getProvider();
    }

    @Override
    public void onAddedIntoComposite(final ParentComposite<T> parentComposite) {
        parents.add(parentComposite);
        final ExternalStorage<T> delegate = getUnsafeDelegate();
        if (delegate != null) {
            delegate.onAddedIntoComposite(parentComposite);
        }
    }

    @Override
    public void onRemovedFromComposite(final ParentComposite<T> parentComposite) {
        parents.remove(parentComposite);
        final ExternalStorage<T> delegate = getUnsafeDelegate();
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
        final ExternalStorage<T> delegate = getDelegate();
        parents.forEach(parent -> {
            parent.onSourceRemovedFromChild(delegate);
            delegate.onRemovedFromComposite(parent);
        });
        super.clearDelegate();
    }

    public boolean detectChanges() {
        final ExternalStorage<T> delegate = getUnsafeDelegate();
        if (delegate == null) {
            return false;
        }
        return delegate.detectChanges();
    }

    @Override
    public Optional<TrackedResource> findTrackedResourceByActorType(final T resource,
                                                                    final Class<? extends Actor> actorType) {
        return trackingRepository.findTrackedResourceByActorType(resource, actorType);
    }

    @Override
    public void beforeDetectChanges(final T resource, final Actor actor) {
        trackingRepository.update(resource, actor, clock.getAsLong());
    }
}
