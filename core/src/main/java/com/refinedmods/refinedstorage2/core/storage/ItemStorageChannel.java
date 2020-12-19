package com.refinedmods.refinedstorage2.core.storage;

import com.refinedmods.refinedstorage2.core.list.ListenableStackList;
import com.refinedmods.refinedstorage2.core.list.StackListListener;
import com.refinedmods.refinedstorage2.core.list.item.ItemStackList;
import com.refinedmods.refinedstorage2.core.util.Action;
import net.minecraft.item.ItemStack;

import java.util.*;

public class ItemStorageChannel implements StorageChannel<ItemStack> {
    private final Set<StackListListener<ItemStack>> listeners = new HashSet<>();
    private ListenableStackList<ItemStack> list;
    private CompositeItemStorage storage = CompositeItemStorage.emptyStorage();

    public void setSources(List<Storage<ItemStack>> sources) {
        this.list = new ListenableStackList<>(new ItemStackList(), listeners);
        this.storage = new CompositeItemStorage(sources, list);
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
