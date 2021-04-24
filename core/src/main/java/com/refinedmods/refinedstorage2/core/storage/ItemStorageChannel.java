package com.refinedmods.refinedstorage2.core.storage;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.refinedmods.refinedstorage2.core.item.Rs2ItemStack;
import com.refinedmods.refinedstorage2.core.item.Rs2ItemStackIdentifier;
import com.refinedmods.refinedstorage2.core.list.ListenableStackList;
import com.refinedmods.refinedstorage2.core.list.StackListListener;
import com.refinedmods.refinedstorage2.core.list.item.ItemStackList;
import com.refinedmods.refinedstorage2.core.util.Action;

public class ItemStorageChannel implements StorageChannel<Rs2ItemStack> {
    private final StorageTracker<Rs2ItemStack, Rs2ItemStackIdentifier> tracker = new StorageTracker<>(Rs2ItemStackIdentifier::new, System::currentTimeMillis);
    private final Set<StackListListener<Rs2ItemStack>> listeners = new HashSet<>();
    private ListenableStackList<Rs2ItemStack> list;
    private CompositeItemStorage storage = CompositeItemStorage.empty();

    public void setSources(List<Storage<Rs2ItemStack>> sources) {
        this.list = new ListenableStackList<>(ItemStackList.create(), listeners);
        this.storage = new CompositeItemStorage(sources, list);
        sortSources();
    }

    @Override
    public void sortSources() {
        storage.sortSources();
    }

    @Override
    public void addListener(StackListListener<Rs2ItemStack> listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(StackListListener<Rs2ItemStack> listener) {
        listeners.remove(listener);
    }

    @Override
    public Optional<Rs2ItemStack> extract(Rs2ItemStack template, long amount, Source source) {
        tracker.onChanged(template, source.getName());
        return extract(template, amount, Action.EXECUTE);
    }

    @Override
    public Optional<Rs2ItemStack> insert(Rs2ItemStack template, long amount, Source source) {
        tracker.onChanged(template, source.getName());
        return insert(template, amount, Action.EXECUTE);
    }

    @Override
    public StorageTracker<Rs2ItemStack, ?> getTracker() {
        return tracker;
    }

    @Override
    public Optional<Rs2ItemStack> get(Rs2ItemStack template) {
        return list.get(template);
    }

    @Override
    public Optional<Rs2ItemStack> extract(Rs2ItemStack template, long amount, Action action) {
        return storage.extract(template, amount, action);
    }

    @Override
    public Optional<Rs2ItemStack> insert(Rs2ItemStack template, long amount, Action action) {
        return storage.insert(template, amount, action);
    }

    @Override
    public Collection<Rs2ItemStack> getStacks() {
        return storage.getStacks();
    }

    @Override
    public long getStored() {
        return storage.getStored();
    }
}
