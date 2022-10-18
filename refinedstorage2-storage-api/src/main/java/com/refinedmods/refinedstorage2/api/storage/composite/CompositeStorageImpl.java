package com.refinedmods.refinedstorage2.api.storage.composite;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.resource.list.ResourceList;
import com.refinedmods.refinedstorage2.api.storage.Actor;
import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedResource;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedStorage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apiguardian.api.API;

/**
 * An implementation of {@link CompositeStorage} that can be contained into other {@link CompositeStorage}s.
 *
 * @param <T> the type of resource
 */
@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.0")
public class CompositeStorageImpl<T> implements CompositeStorage<T>, CompositeAwareChild<T>, ParentComposite<T> {
    private final List<Storage<T>> sources = new ArrayList<>();
    private final ResourceList<T> list;
    private final Set<ParentComposite<T>> parentComposites = new HashSet<>();

    /**
     * @param list the backing list of this composite storage, used to retrieve a view of the sources
     */
    public CompositeStorageImpl(final ResourceList<T> list) {
        this.list = list;
    }

    @Override
    public void sortSources() {
        sources.sort(PrioritizedStorageComparator.INSTANCE);
    }

    @Override
    public void addSource(final Storage<T> source) {
        sources.add(source);
        sortSources();
        addContentOfSourceToList(source);
        parentComposites.forEach(parentComposite -> parentComposite.onSourceAddedToChild(source));
        if (source instanceof CompositeAwareChild<T> compositeAwareChild) {
            compositeAwareChild.onAddedIntoComposite(this);
        }
    }

    @Override
    public void removeSource(final Storage<T> source) {
        sources.remove(source);
        // Re-sort isn't necessary, since they are ordered when added.
        removeContentOfSourceFromList(source);
        parentComposites.forEach(parentComposite -> parentComposite.onSourceRemovedFromChild(source));
        if (source instanceof CompositeAwareChild<T> compositeAwareChild) {
            compositeAwareChild.onRemovedFromComposite(this);
        }
    }

    @Override
    public void clearSources() {
        final Set<Storage<T>> oldSources = new HashSet<>(sources);
        oldSources.forEach(this::removeSource);
    }

    @Override
    public long extract(final T resource, final long amount, final Action action, final Actor actor) {
        final long extracted = extractFromStorages(resource, amount, action, actor);
        if (action == Action.EXECUTE && extracted > 0) {
            list.remove(resource, extracted);
        }
        return extracted;
    }

    private long extractFromStorages(final T template, final long amount, final Action action, final Actor actor) {
        long remaining = amount;
        for (final Storage<T> source : sources) {
            final long extracted = source.extract(template, remaining, action, actor);
            remaining -= extracted;
            if (remaining == 0) {
                break;
            }
        }

        return amount - remaining;
    }

    @Override
    public long insert(final T resource, final long amount, final Action action, final Actor actor) {
        long inserted = 0;
        long toInsertIntoList = 0;
        for (final Storage<T> source : sources) {
            final long insertedIntoSource = source.insert(resource, amount - inserted, action, actor);
            if (!(source instanceof ConsumingStorage)) {
                toInsertIntoList += insertedIntoSource;
            }
            inserted += insertedIntoSource;
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
    public Collection<ResourceAmount<T>> getAll() {
        return list.getAll();
    }

    @Override
    public long getStored() {
        return sources.stream().mapToLong(Storage::getStored).sum();
    }

    @Override
    public Optional<TrackedResource> findTrackedResourceByActorType(final T resource,
                                                                    final Class<? extends Actor> actorType) {
        return sources
            .stream()
            .filter(TrackedStorage.class::isInstance)
            .map(storage -> (TrackedStorage<T>) storage)
            .flatMap(storage -> storage.findTrackedResourceByActorType(resource, actorType).stream())
            .max(Comparator.comparingLong(TrackedResource::getTime));
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
    public void onSourceAddedToChild(final Storage<T> source) {
        addContentOfSourceToList(source);
    }

    @Override
    public void onSourceRemovedFromChild(final Storage<T> source) {
        removeContentOfSourceFromList(source);
    }

    private void addContentOfSourceToList(final Storage<T> source) {
        source.getAll().forEach(list::add);
    }

    private void removeContentOfSourceFromList(final Storage<T> source) {
        source.getAll().forEach(resourceAmount -> list.remove(
            resourceAmount.getResource(),
            resourceAmount.getAmount()
        ));
    }
}
