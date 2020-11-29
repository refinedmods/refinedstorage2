package com.refinedmods.refinedstorage2.core.storage;

import com.refinedmods.refinedstorage2.core.list.StackList;
import com.refinedmods.refinedstorage2.core.list.StackListResult;
import com.refinedmods.refinedstorage2.core.list.item.ItemStackList;
import com.refinedmods.refinedstorage2.core.util.Action;
import net.minecraft.item.ItemStack;

import java.util.*;

public class ItemStorageChannel implements StorageChannel<ItemStack> {
    private final Set<StorageChannelListener<ItemStack>> listeners = new HashSet<>();
    private CompositeItemStorage storage = new CompositeItemStorage(Collections.emptyList(), new ItemStackList());

    public void setSources(List<Storage<ItemStack>> sources) {
        storage = new CompositeItemStorage(sources, new ItemStackList() {
            @Override
            public StackListResult<ItemStack> add(ItemStack template, int amount) {
                StackListResult<ItemStack> result = super.add(template, amount);
                listeners.forEach(listener -> listener.onChanged(result));
                return result;
            }

            @Override
            public Optional<StackListResult<ItemStack>> remove(ItemStack template, int amount) {
                Optional<StackListResult<ItemStack>> resultMaybe = super.remove(template, amount);
                resultMaybe.ifPresent(result -> listeners.forEach(listener -> listener.onChanged(result)));
                return resultMaybe;
            }
        });
    }

    @Override
    public void addListener(StorageChannelListener<ItemStack> listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(StorageChannelListener<ItemStack> listener) {
        listeners.remove(listener);
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
    public StackList<ItemStack> getStacks() {
        return storage.getStacks();
    }

    @Override
    public int getStored() {
        return storage.getStored();
    }
}
