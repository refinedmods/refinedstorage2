package com.refinedmods.refinedstorage2.api.storage.channel;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.stack.Rs2Stack;
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

public class StorageChannelImpl<S extends Rs2Stack, I> implements StorageChannel<S> {
    private final Supplier<StackList<S>> listFactory;
    private final StorageTracker<S, I> tracker;
    private final Set<StackListListener<S>> listeners = new HashSet<>();
    private ListenableStackList<S> list;
    private CompositeStorage<S> storage;
    private final List<Storage<S>> sources = new ArrayList<>();

    public StorageChannelImpl(Supplier<StackList<S>> listFactory, StorageTracker<S, I> tracker, CompositeStorage<S> defaultStorage) {
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
        sources.add((Storage<S>) source);
        invalidate();
    }

    @Override
    public void removeSource(Storage<?> source) {
        sources.remove((Storage<S>) source);
        invalidate();
    }

    @Override
    public void addListener(StackListListener<S> listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(StackListListener<S> listener) {
        listeners.remove(listener);
    }

    @Override
    public Optional<S> extract(S template, long amount, Source source) {
        tracker.onChanged(template, source.getName());
        return extract(template, amount, Action.EXECUTE);
    }

    @Override
    public Optional<S> insert(S template, long amount, Source source) {
        tracker.onChanged(template, source.getName());
        return insert(template, amount, Action.EXECUTE);
    }

    @Override
    public StorageTracker<S, ?> getTracker() {
        return tracker;
    }

    @Override
    public Optional<S> get(S template) {
        return list.get(template);
    }

    @Override
    public Optional<S> extract(S template, long amount, Action action) {
        return storage.extract(template, amount, action);
    }

    @Override
    public Optional<S> insert(S template, long amount, Action action) {
        return storage.insert(template, amount, action);
    }

    @Override
    public Collection<S> getStacks() {
        return storage.getStacks();
    }

    @Override
    public long getStored() {
        return storage.getStored();
    }
}
