package com.refinedmods.refinedstorage2.core.grid;

import java.util.Collection;
import java.util.Optional;

import com.refinedmods.refinedstorage2.core.list.StackListListener;
import com.refinedmods.refinedstorage2.core.storage.ItemStorageChannel;
import com.refinedmods.refinedstorage2.core.storage.Source;
import com.refinedmods.refinedstorage2.core.storage.StorageChannel;
import com.refinedmods.refinedstorage2.core.storage.StorageTracker;
import com.refinedmods.refinedstorage2.core.util.Action;
import net.minecraft.item.ItemStack;

import static org.assertj.core.api.Assertions.assertThat;

public class StorageTrackerEntryPresentAssertionItemStorageChannel implements StorageChannel<ItemStack> {
    private final ItemStorageChannel parent;

    public StorageTrackerEntryPresentAssertionItemStorageChannel(ItemStorageChannel parent) {
        this.parent = parent;
    }

    @Override
    public Optional<ItemStack> extract(ItemStack template, int amount, Action action) {
        if (action == Action.EXECUTE) {
            Optional<StorageTracker.Entry> entry = getTracker().getEntry(template);
            assertThat(entry).isPresent();
        }

        return parent.extract(template, amount, action);
    }

    @Override
    public Optional<ItemStack> insert(ItemStack template, int amount, Action action) {
        if (action == Action.EXECUTE) {
            Optional<StorageTracker.Entry> entry = getTracker().getEntry(template);
            assertThat(entry).isPresent();
        }

        return parent.insert(template, amount, action);
    }

    @Override
    public Collection<ItemStack> getStacks() {
        return parent.getStacks();
    }

    @Override
    public int getStored() {
        return parent.getStored();
    }

    @Override
    public void addListener(StackListListener<ItemStack> listener) {
        parent.addListener(listener);
    }

    @Override
    public void removeListener(StackListListener<ItemStack> listener) {
        parent.removeListener(listener);
    }

    @Override
    public Optional<ItemStack> extract(ItemStack template, int amount, Source source) {
        return parent.extract(template, amount, source);
    }

    @Override
    public Optional<ItemStack> insert(ItemStack template, int amount, Source source) {
        return parent.insert(template, amount, source);
    }

    @Override
    public StorageTracker<ItemStack, ?> getTracker() {
        return parent.getTracker();
    }

    @Override
    public Optional<ItemStack> get(ItemStack template) {
        return parent.get(template);
    }
}
