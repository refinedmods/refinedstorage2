package com.refinedmods.refinedstorage2.api.storage.composite;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.resource.list.ResourceList;
import com.refinedmods.refinedstorage2.api.storage.Storage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apiguardian.api.API;

/**
 * This represents a single storage that can be backed by multiple storages.
 *
 * @param <T> the type of resource
 */
@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.0")
public class CompositeStorageImpl<T> implements CompositeStorage<T>, CompositeStorageListener<T> {
    private final List<Storage<T>> sources = new ArrayList<>();
    private final ResourceList<T> list;
    private final Set<CompositeStorageListener<T>> listeners = new HashSet<>();

    /**
     * @param list the backing list of this composite storage, used to retrieve a view of the sources
     */
    public CompositeStorageImpl(ResourceList<T> list) {
        this.list = list;
    }

    /**
     * Sort the sources of this composite.
     * If a storage implements {@link Priority}, the composite will account for this.
     */
    @Override
    public void sortSources() {
        sources.sort(PrioritizedStorageComparator.INSTANCE);
    }

    @Override
    public void addSource(Storage<T> source) {
        sources.add(source);
        sortSources();
        onSourceAdded(source);
        listeners.forEach(listener -> listener.onSourceAdded(source));
        if (source instanceof CompositeStorage<T> childComposite) {
            childComposite.addListener(this);
        }
    }

    @Override
    public void removeSource(Storage<T> source) {
        sources.remove(source);
        sortSources();
        onSourceRemoved(source);
        listeners.forEach(listener -> listener.onSourceRemoved(source));
        if (source instanceof CompositeStorage<T> childComposite) {
            childComposite.removeListener(this);
        }
    }

    @Override
    public void addListener(CompositeStorageListener<T> listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(CompositeStorageListener<T> listener) {
        listeners.remove(listener);
    }

    @Override
    public long extract(T resource, long amount, Action action) {
        long extracted = extractFromStorages(resource, amount, action);
        if (action == Action.EXECUTE && extracted > 0) {
            list.remove(resource, extracted);
        }
        return extracted;
    }

    private long extractFromStorages(T template, long amount, Action action) {
        long remaining = amount;
        for (Storage<T> source : sources) {
            long extracted = source.extract(template, remaining, action);
            remaining -= extracted;
            if (remaining == 0) {
                break;
            }
        }

        return amount - remaining;
    }

    @Override
    public long insert(T resource, long amount, Action action) {
        long remainder = insertIntoStorages(resource, amount, action);
        if (action == Action.EXECUTE) {
            long inserted = amount - remainder;
            if (inserted > 0) {
                list.add(resource, inserted);
            }
        }
        return remainder;
    }

    private long insertIntoStorages(T template, long amount, Action action) {
        long remainder = amount;
        for (Storage<T> source : sources) {
            remainder = source.insert(template, remainder, action);
            if (remainder == 0) {
                break;
            }
        }
        return remainder;
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
    public void onSourceAdded(Storage<T> source) {
        source.getAll().forEach(list::add);
    }

    @Override
    public void onSourceRemoved(Storage<T> source) {
        source.getAll().forEach(resourceAmount -> list.remove(resourceAmount.getResource(), resourceAmount.getAmount()));
    }
}
