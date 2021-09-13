package com.refinedmods.refinedstorage2.api.grid.eventhandler;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.stack.list.listenable.StackListListener;
import com.refinedmods.refinedstorage2.api.storage.Source;
import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannel;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageTracker;

import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class StorageTrackerEntryPresentAssertionItemStorageChannel<T> implements StorageChannel<T> {
    private final StorageChannel<T> parent;

    public StorageTrackerEntryPresentAssertionItemStorageChannel(StorageChannel<T> parent) {
        this.parent = parent;
    }

    @Override
    public Optional<T> extract(T resource, long amount, Action action) {
        if (action == Action.EXECUTE) {
            Optional<StorageTracker.Entry> entry = getTracker().getEntry(resource);
            assertThat(entry).isPresent();
        }
        return parent.extract(resource, amount, action);
    }

    @Override
    public Optional<T> insert(T resource, long amount, Action action) {
        if (action == Action.EXECUTE) {
            Optional<StorageTracker.Entry> entry = getTracker().getEntry(resource);
            assertThat(entry).isPresent();
        }
        return parent.insert(resource, amount, action);
    }

    @Override
    public Collection<T> getAll() {
        return parent.getAll();
    }

    @Override
    public long getStored() {
        return parent.getStored();
    }

    @Override
    public void addListener(StackListListener<T> listener) {
        parent.addListener(listener);
    }

    @Override
    public void removeListener(StackListListener<T> listener) {
        parent.removeListener(listener);
    }

    @Override
    public Optional<T> extract(T resource, long amount, Source source) {
        return parent.extract(resource, amount, source);
    }

    @Override
    public Optional<T> insert(T resource, long amount, Source source) {
        return parent.insert(resource, amount, source);
    }

    @Override
    public StorageTracker<T, ?> getTracker() {
        return parent.getTracker();
    }

    @Override
    public Optional<T> get(T resource) {
        return parent.get(resource);
    }

    @Override
    public void sortSources() {
        parent.sortSources();
    }

    @Override
    public void addSource(Storage<?> source) {
        parent.addSource(source);
    }

    @Override
    public void removeSource(Storage<?> source) {
        parent.removeSource(source);
    }

    @Override
    public void invalidate() {
        parent.invalidate();
    }
}
