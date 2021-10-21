package com.refinedmods.refinedstorage2.api.storage.channel;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.resource.list.ResourceList;
import com.refinedmods.refinedstorage2.api.resource.list.listenable.ListenableResourceList;
import com.refinedmods.refinedstorage2.api.resource.list.listenable.ResourceListListener;
import com.refinedmods.refinedstorage2.api.storage.Source;
import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.composite.CompositeStorage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import com.google.common.base.Preconditions;
import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.0")
public class StorageChannelImpl<T> implements StorageChannel<T> {
    private final Supplier<ResourceList<T>> listFactory;
    private final StorageTracker<T> tracker;
    private final Set<ResourceListListener<T>> listeners = new HashSet<>();
    private ListenableResourceList<T> list;
    private CompositeStorage<T> storage;
    private final List<Storage<T>> sources = new ArrayList<>();

    /**
     * @param storage     the backing {@link CompositeStorage}
     * @param listFactory a supplier for the backing list of the {@link CompositeStorage}
     * @param tracker     the storage tracker
     */
    public StorageChannelImpl(CompositeStorage<T> storage, Supplier<ResourceList<T>> listFactory, StorageTracker<T> tracker) {
        this.listFactory = listFactory;
        this.tracker = tracker;
        this.storage = storage;
    }

    @Override
    public void invalidate() {
        this.list = new ListenableResourceList<>(listFactory.get(), listeners);
        this.storage = new CompositeStorage<>(sources, list);
        sortSources();
    }

    @Override
    public void sortSources() {
        storage.sortSources();
    }

    @Override
    public void addSource(Storage<?> source) {
        sources.add((Storage<T>) source);
        invalidate();
    }

    @Override
    public void removeSource(Storage<?> source) {
        sources.remove((Storage<T>) source);
        invalidate();
    }

    @Override
    public void addListener(ResourceListListener<T> listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(ResourceListListener<T> listener) {
        listeners.remove(listener);
    }

    @Override
    public long extract(T resource, long amount, Source source) {
        Preconditions.checkNotNull(resource);
        Preconditions.checkNotNull(source);
        tracker.onChanged(resource, source.getName());
        return extract(resource, amount, Action.EXECUTE);
    }

    @Override
    public long insert(T resource, long amount, Source source) {
        Preconditions.checkNotNull(resource);
        Preconditions.checkNotNull(source);
        tracker.onChanged(resource, source.getName());
        return insert(resource, amount, Action.EXECUTE);
    }

    @Override
    public StorageTracker<T> getTracker() {
        return tracker;
    }

    @Override
    public Optional<ResourceAmount<T>> get(T resource) {
        return list.get(resource);
    }

    @Override
    public long extract(T resource, long amount, Action action) {
        return storage.extract(resource, amount, action);
    }

    @Override
    public long insert(T resource, long amount, Action action) {
        return storage.insert(resource, amount, action);
    }

    @Override
    public Collection<ResourceAmount<T>> getAll() {
        return storage.getAll();
    }

    @Override
    public long getStored() {
        return storage.getStored();
    }
}
