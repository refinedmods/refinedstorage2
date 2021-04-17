package com.refinedmods.refinedstorage2.core.storage;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.refinedmods.refinedstorage2.core.list.ListenableStackList;
import com.refinedmods.refinedstorage2.core.list.StackListListener;
import com.refinedmods.refinedstorage2.core.list.item.ItemStackList;
import com.refinedmods.refinedstorage2.core.util.Action;
import com.refinedmods.refinedstorage2.core.util.ItemStackIdentifier;
import net.minecraft.item.ItemStack;

public class ItemStorageChannel implements StorageChannel<ItemStack> {
    private final StorageTracker<ItemStack, ItemStackIdentifier> tracker = new StorageTracker<>(ItemStackIdentifier::new, System::currentTimeMillis);
    private final Set<StackListListener<ItemStack>> listeners = new HashSet<>();
    private ListenableStackList<ItemStack> list;
    private CompositeItemStorage storage = CompositeItemStorage.emptyStorage();

    public void setSources(List<Storage<ItemStack>> sources) {
        this.list = new ListenableStackList<>(new ItemStackList(), listeners);
        this.storage = new CompositeItemStorage(sources, list);
        sortSources();
    }

    @Override
    public void sortSources() {
        storage.sortSources();
    }

    @Override
    public void addListener(StackListListener<ItemStack> listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(StackListListener<ItemStack> listener) {
        listeners.remove(listener);
    }

    @Override
    public Optional<ItemStack> extract(ItemStack template, int amount, Source source) {
        tracker.onChanged(template, source.getName());
        return extract(template, amount, Action.EXECUTE);
    }

    @Override
    public Optional<ItemStack> insert(ItemStack template, int amount, Source source) {
        tracker.onChanged(template, source.getName());
        return insert(template, amount, Action.EXECUTE);
    }

    @Override
    public StorageTracker<ItemStack, ?> getTracker() {
        return tracker;
    }

    @Override
    public Optional<ItemStack> get(ItemStack template) {
        return list.get(template);
    }

    @Override
    public Optional<ItemStack> extract(ItemStack template, int amount, Action action) {
        return storage.extract(template, amount, action);
    }

    @Override
    public Optional<ItemStack> insert(ItemStack template, int amount, Action action) {
        return storage.insert(template, amount, action);
    }

    @Override
    public Collection<ItemStack> getStacks() {
        return storage.getStacks();
    }

    @Override
    public int getStored() {
        return storage.getStored();
    }
}
