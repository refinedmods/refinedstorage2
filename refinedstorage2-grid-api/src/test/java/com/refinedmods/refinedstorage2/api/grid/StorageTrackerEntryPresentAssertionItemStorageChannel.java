package com.refinedmods.refinedstorage2.api.grid;

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
    public Optional<T> extract(T template, long amount, Action action) {
        if (action == Action.EXECUTE) {
            Optional<StorageTracker.Entry> entry = getTracker().getEntry(template);
            assertThat(entry).isPresent();
        }
        return parent.extract(template, amount, action);
    }

    @Override
    public Optional<T> insert(T template, long amount, Action action) {
        if (action == Action.EXECUTE) {
            Optional<StorageTracker.Entry> entry = getTracker().getEntry(template);
            assertThat(entry).isPresent();
        }
        return parent.insert(template, amount, action);
    }

    @Override
    public Collection<T> getStacks() {
        return parent.getStacks();
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
    public Optional<T> extract(T template, long amount, Source source) {
        return parent.extract(template, amount, source);
    }

    @Override
    public Optional<T> insert(T template, long amount, Source source) {
        return parent.insert(template, amount, source);
    }

    @Override
    public StorageTracker<T, ?> getTracker() {
        return parent.getTracker();
    }

    @Override
    public Optional<T> get(T template) {
        return parent.get(template);
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
