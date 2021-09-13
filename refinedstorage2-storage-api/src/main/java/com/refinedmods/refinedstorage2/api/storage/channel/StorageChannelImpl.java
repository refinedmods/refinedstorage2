package com.refinedmods.refinedstorage2.api.storage.channel;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.stack.ResourceAmount;
import com.refinedmods.refinedstorage2.api.stack.list.StackList;
import com.refinedmods.refinedstorage2.api.stack.list.listenable.ListenableStackList;
import com.refinedmods.refinedstorage2.api.stack.list.listenable.StackListListener;
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

public class StorageChannelImpl<T> implements StorageChannel<T> {
    private final Supplier<StackList<T>> listFactory;
    private final StorageTracker<T> tracker;
    private final Set<StackListListener<T>> listeners = new HashSet<>();
    private ListenableStackList<T> list;
    private CompositeStorage<T> storage;
    private final List<Storage<T>> sources = new ArrayList<>();

    public StorageChannelImpl(Supplier<StackList<T>> listFactory, StorageTracker<T> tracker, CompositeStorage<T> defaultStorage) {
        this.listFactory = listFactory;
        this.tracker = tracker;
        this.storage = defaultStorage;
    }

    @Override
    public void invalidate() {
        this.list = new ListenableStackList<>(listFactory.get(), listeners);
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
    public void addListener(StackListListener<T> listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(StackListListener<T> listener) {
        listeners.remove(listener);
    }

    @Override
    public long extract(T resource, long amount, Source source) {
        tracker.onChanged(resource, source.getName());
        return extract(resource, amount, Action.EXECUTE);
    }

    @Override
    public long insert(T resource, long amount, Source source) {
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
