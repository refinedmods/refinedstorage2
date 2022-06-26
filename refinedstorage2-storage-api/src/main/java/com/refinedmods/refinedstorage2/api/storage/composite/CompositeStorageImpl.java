package com.refinedmods.refinedstorage2.api.storage.composite;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.resource.list.ResourceList;
import com.refinedmods.refinedstorage2.api.storage.Source;
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
    public CompositeStorageImpl(ResourceList<T> list) {
        this.list = list;
    }

    @Override
    public void sortSources() {
        sources.sort(PrioritizedStorageComparator.INSTANCE);
    }

    @Override
    public void addSource(Storage<T> source) {
        sources.add(source);
        sortSources();
        addContentOfSourceToList(source);
        parentComposites.forEach(parentComposite -> parentComposite.onSourceAddedToChild(source));
        if (source instanceof CompositeAwareChild<T> compositeAwareChild) {
            compositeAwareChild.onAddedIntoComposite(this);
        }
    }

    @Override
    public void removeSource(Storage<T> source) {
        sources.remove(source);
        sortSources();
        removeContentOfSourceFromList(source);
        parentComposites.forEach(parentComposite -> parentComposite.onSourceRemovedFromChild(source));
        if (source instanceof CompositeAwareChild<T> compositeAwareChild) {
            compositeAwareChild.onRemovedFromComposite(this);
        }
    }

    @Override
    public void clearSources() {
        Set<Storage<T>> oldSources = new HashSet<>(sources);
        oldSources.forEach(this::removeSource);
    }

    @Override
    public long extract(T resource, long amount, Action action, Source source) {
        long extracted = extractFromStorages(resource, amount, action, source);
        if (action == Action.EXECUTE && extracted > 0) {
            list.remove(resource, extracted);
        }
        return extracted;
    }

    private long extractFromStorages(T template, long amount, Action action, Source actionSource) {
        long remaining = amount;
        for (Storage<T> source : sources) {
            long extracted = source.extract(template, remaining, action, actionSource);
            remaining -= extracted;
            if (remaining == 0) {
                break;
            }
        }

        return amount - remaining;
    }

    @Override
    public long insert(T resource, long amount, Action action, Source source) {
        long inserted = insertIntoStorages(resource, amount, action, source);
        if (action == Action.EXECUTE && inserted > 0) {
            list.add(resource, inserted);
        }
        return inserted;
    }

    private long insertIntoStorages(T template, long amount, Action action, Source actionSource) {
        long inserted = 0;
        for (Storage<T> source : sources) {
            inserted += source.insert(template, amount - inserted, action, actionSource);
            if (inserted == amount) {
                break;
            }
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
    public Optional<TrackedResource> findTrackedResourceBySourceType(T resource, Class<? extends Source> sourceType) {
        return sources
                .stream()
                .filter(TrackedStorage.class::isInstance)
                .map(storage -> (TrackedStorage<T>) storage)
                .flatMap(trackedStorage -> trackedStorage.findTrackedResourceBySourceType(resource, sourceType).stream())
                .max(Comparator.comparingLong(TrackedResource::getTime));
    }

    @Override
    public void onAddedIntoComposite(ParentComposite<T> parentComposite) {
        parentComposites.add(parentComposite);
    }

    @Override
    public void onRemovedFromComposite(ParentComposite<T> parentComposite) {
        parentComposites.remove(parentComposite);
    }

    @Override
    public void onSourceAddedToChild(Storage<T> source) {
        addContentOfSourceToList(source);
    }

    @Override
    public void onSourceRemovedFromChild(Storage<T> source) {
        removeContentOfSourceFromList(source);
    }

    private void addContentOfSourceToList(Storage<T> source) {
        source.getAll().forEach(list::add);
    }

    private void removeContentOfSourceFromList(Storage<T> source) {
        source.getAll().forEach(resourceAmount -> list.remove(resourceAmount.getResource(), resourceAmount.getAmount()));
    }
}
