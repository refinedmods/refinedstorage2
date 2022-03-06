package com.refinedmods.refinedstorage2.api.storage.channel;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.resource.list.ResourceListImpl;
import com.refinedmods.refinedstorage2.api.resource.list.listenable.ListenableResourceList;
import com.refinedmods.refinedstorage2.api.resource.list.listenable.ResourceListListener;
import com.refinedmods.refinedstorage2.api.storage.Source;
import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.composite.CompositeStorage;

import java.util.Collection;
import java.util.Optional;

import com.google.common.base.Preconditions;
import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.0")
public class StorageChannelImpl<T> implements StorageChannel<T> {
    private final StorageTracker<T> tracker;
    private final ListenableResourceList<T> list;
    private final CompositeStorage<T> storage;

    /**
     * @param tracker the storage tracker
     */
    public StorageChannelImpl(StorageTracker<T> tracker) {
        this.tracker = tracker;
        this.list = new ListenableResourceList<>(new ResourceListImpl<>());
        this.storage = new CompositeStorage<>(list);
    }

    @Override
    public void sortSources() {
        storage.sortSources();
    }

    @Override
    public void addSource(Storage<?> source) {
        storage.addSource((Storage<T>) source);
    }

    @Override
    public void removeSource(Storage<?> source) {
        storage.removeSource((Storage<T>) source);
    }

    @Override
    public void addListener(ResourceListListener<T> listener) {
        list.addListener(listener);
    }

    @Override
    public void removeListener(ResourceListListener<T> listener) {
        list.removeListener(listener);
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
