package com.refinedmods.refinedstorage2.api.storage.composite;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.resource.ResourceKey;
import com.refinedmods.refinedstorage2.api.resource.list.ResourceList;
import com.refinedmods.refinedstorage2.api.storage.Actor;
import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedResource;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedStorage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apiguardian.api.API;

/**
 * An implementation of {@link CompositeStorage} that can be contained into other {@link CompositeStorage}s.
 */
@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.0")
public class CompositeStorageImpl implements CompositeStorage, CompositeAwareChild, ParentComposite {
    private final List<Storage> sources = new ArrayList<>();
    private final ResourceList list;
    private final Set<ParentComposite> parentComposites = new HashSet<>();

    /**
     * @param list the backing list of this composite storage, used to retrieve a view of the sources
     */
    public CompositeStorageImpl(final ResourceList list) {
        this.list = list;
    }

    @Override
    public void sortSources() {
        sources.sort(PrioritizedStorageComparator.INSTANCE);
    }

    @Override
    public void addSource(final Storage source) {
        sources.add(source);
        sortSources();
        addContentOfSourceToList(source);
        parentComposites.forEach(parentComposite -> parentComposite.onSourceAddedToChild(source));
        if (source instanceof CompositeAwareChild compositeAwareChild) {
            compositeAwareChild.onAddedIntoComposite(this);
        }
    }

    @Override
    public void removeSource(final Storage source) {
        sources.remove(source);
        // Re-sort isn't necessary, since they are ordered when added.
        removeContentOfSourceFromList(source);
        parentComposites.forEach(parentComposite -> parentComposite.onSourceRemovedFromChild(source));
        if (source instanceof CompositeAwareChild compositeAwareChild) {
            compositeAwareChild.onRemovedFromComposite(this);
        }
    }

    @Override
    public List<Storage> getSources() {
        return Collections.unmodifiableList(sources);
    }

    @Override
    public void clearSources() {
        final Set<Storage> oldSources = new HashSet<>(sources);
        oldSources.forEach(this::removeSource);
    }

    @Override
    public long extract(final ResourceKey resource, final long amount, final Action action, final Actor actor) {
        long remaining = amount;
        long toRemoveFromList = 0;
        for (final Storage source : sources) {
            if (source instanceof CompositeAwareChild compositeAwareChild) {
                final Amount extracted = compositeAwareChild.compositeExtract(resource, remaining, action, actor);
                remaining -= extracted.amount();
                toRemoveFromList += extracted.amountForList();
            } else {
                final long extracted = source.extract(resource, remaining, action, actor);
                remaining -= extracted;
                toRemoveFromList += extracted;
            }
            if (remaining == 0) {
                break;
            }
        }
        final long extracted = amount - remaining;
        if (action == Action.EXECUTE && toRemoveFromList > 0) {
            list.remove(resource, toRemoveFromList);
        }
        return extracted;
    }

    @Override
    public long insert(final ResourceKey resource, final long amount, final Action action, final Actor actor) {
        long inserted = 0;
        long toInsertIntoList = 0;
        for (final Storage source : sources) {
            if (source instanceof CompositeAwareChild compositeAwareChild) {
                final Amount insertedAmount = compositeAwareChild.compositeInsert(
                    resource,
                    amount - inserted,
                    action,
                    actor
                );
                inserted += insertedAmount.amount();
                toInsertIntoList += insertedAmount.amountForList();
            } else {
                final long insertedAmount = source.insert(resource, amount - inserted, action, actor);
                inserted += insertedAmount;
                toInsertIntoList += insertedAmount;
            }
            if (inserted == amount) {
                break;
            }
        }
        if (action == Action.EXECUTE && toInsertIntoList > 0) {
            list.add(resource, toInsertIntoList);
        }
        return inserted;
    }

    @Override
    public Collection<ResourceAmount> getAll() {
        return list.getAll();
    }

    @Override
    public long getStored() {
        return sources.stream().mapToLong(Storage::getStored).sum();
    }

    @Override
    public Optional<TrackedResource> findTrackedResourceByActorType(final ResourceKey resource,
                                                                    final Class<? extends Actor> actorType) {
        return sources
            .stream()
            .filter(TrackedStorage.class::isInstance)
            .map(TrackedStorage.class::cast)
            .flatMap(storage -> storage.findTrackedResourceByActorType(resource, actorType).stream())
            .max(Comparator.comparingLong(TrackedResource::getTime));
    }

    @Override
    public void onAddedIntoComposite(final ParentComposite parentComposite) {
        parentComposites.add(parentComposite);
    }

    @Override
    public void onRemovedFromComposite(final ParentComposite parentComposite) {
        parentComposites.remove(parentComposite);
    }

    @Override
    public Amount compositeInsert(final ResourceKey resource,
                                  final long amount,
                                  final Action action,
                                  final Actor actor) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Amount compositeExtract(final ResourceKey resource,
                                   final long amount,
                                   final Action action,
                                   final Actor actor) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void onSourceAddedToChild(final Storage source) {
        addContentOfSourceToList(source);
    }

    @Override
    public void onSourceRemovedFromChild(final Storage source) {
        removeContentOfSourceFromList(source);
    }

    @Override
    public void addToCache(final ResourceKey resource, final long amount) {
        list.add(resource, amount);
    }

    @Override
    public void removeFromCache(final ResourceKey resource, final long amount) {
        list.remove(resource, amount);
    }

    private void addContentOfSourceToList(final Storage source) {
        source.getAll().forEach(list::add);
    }

    private void removeContentOfSourceFromList(final Storage source) {
        source.getAll().forEach(resourceAmount -> list.remove(
            resourceAmount.getResource(),
            resourceAmount.getAmount()
        ));
    }
}
