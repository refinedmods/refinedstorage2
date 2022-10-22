package com.refinedmods.refinedstorage2.api.storage.external;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.resource.list.ResourceList;
import com.refinedmods.refinedstorage2.api.resource.list.ResourceListImpl;
import com.refinedmods.refinedstorage2.api.storage.Actor;
import com.refinedmods.refinedstorage2.api.storage.composite.CompositeAwareChild;
import com.refinedmods.refinedstorage2.api.storage.composite.ConsumingStorage;
import com.refinedmods.refinedstorage2.api.storage.composite.ParentComposite;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class ExternalStorage<T> implements ConsumingStorage<T>, CompositeAwareChild<T> {
    private final ExternalStorageProvider<T> provider;
    private final Set<ParentComposite<T>> listeners = new HashSet<>();
    private final ResourceList<T> cache = new ResourceListImpl<>();

    public ExternalStorage(final ExternalStorageProvider<T> provider) {
        this.provider = provider;
    }

    @Override
    public long extract(final T resource, final long amount, final Action action, final Actor actor) {
        final long extracted = provider.extract(resource, amount, action, actor);
        if (action == Action.EXECUTE && extracted > 0) {
            detectChanges();
        }
        return extracted;
    }

    @Override
    public long insert(final T resource, final long amount, final Action action, final Actor actor) {
        final long inserted = provider.insert(resource, amount, action, actor);
        if (action == Action.EXECUTE && inserted > 0) {
            detectChanges();
        }
        return inserted;
    }

    public void detectChanges() {
        final ResourceList<T> updatedCache = buildCache();
        detectCompleteRemovals(updatedCache);
        detectAdditionsAndPartialRemovals(updatedCache);
    }

    private void detectCompleteRemovals(final ResourceList<T> updatedCache) {
        final Set<ResourceAmount<T>> removedInUpdatedCache = new HashSet<>();
        for (final ResourceAmount<T> inOldCache : cache.getAll()) {
            final Optional<ResourceAmount<T>> inUpdatedCache = updatedCache.get(inOldCache.getResource());
            if (inUpdatedCache.isEmpty()) {
                removedInUpdatedCache.add(inOldCache);
            }
        }
        removedInUpdatedCache.forEach(removed -> removeFromCache(removed.getResource(), removed.getAmount()));
    }

    private void detectAdditionsAndPartialRemovals(final ResourceList<T> updatedCache) {
        for (final ResourceAmount<T> inUpdatedCache : updatedCache.getAll()) {
            final Optional<ResourceAmount<T>> inOldCache = cache.get(inUpdatedCache.getResource());
            final boolean doesNotExistInOldCache = inOldCache.isEmpty();
            if (doesNotExistInOldCache) {
                addToCache(inUpdatedCache.getResource(), inUpdatedCache.getAmount());
            } else {
                detectPotentialDifference(inUpdatedCache, inOldCache.get());
            }
        }
    }

    private void detectPotentialDifference(final ResourceAmount<T> inUpdatedCache,
                                           final ResourceAmount<T> inOldCache) {
        final T resource = inUpdatedCache.getResource();
        final long diff = inUpdatedCache.getAmount() - inOldCache.getAmount();
        if (diff > 0) {
            addToCache(resource, diff);
        } else if (diff < 0) {
            removeFromCache(resource, Math.abs(diff));
        }
    }

    private void addToCache(final T resource, final long amount) {
        cache.add(resource, amount);
        listeners.forEach(listener -> listener.addToCache(resource, amount));
    }

    private void removeFromCache(final T resource, final long amount) {
        cache.remove(resource, amount);
        listeners.forEach(listener -> listener.removeFromCache(resource, amount));
    }

    private ResourceList<T> buildCache() {
        final ResourceList<T> list = new ResourceListImpl<>();
        provider.iterator().forEachRemaining(list::add);
        return list;
    }

    @Override
    public Collection<ResourceAmount<T>> getAll() {
        return cache.getAll();
    }

    @Override
    public long getStored() {
        return getAll().stream().mapToLong(ResourceAmount::getAmount).sum();
    }

    @Override
    public void onAddedIntoComposite(final ParentComposite<T> parentComposite) {
        listeners.add(parentComposite);
    }

    @Override
    public void onRemovedFromComposite(final ParentComposite<T> parentComposite) {
        listeners.remove(parentComposite);
    }
}
