package com.refinedmods.refinedstorage2.core.grid;

import com.refinedmods.refinedstorage2.core.item.Rs2ItemStack;
import com.refinedmods.refinedstorage2.core.list.StackListListener;
import com.refinedmods.refinedstorage2.core.storage.ItemStorageChannel;
import com.refinedmods.refinedstorage2.core.storage.Source;
import com.refinedmods.refinedstorage2.core.storage.StorageChannel;
import com.refinedmods.refinedstorage2.core.storage.StorageTracker;
import com.refinedmods.refinedstorage2.core.util.Action;

import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class StorageTrackerEntryPresentAssertionItemStorageChannel implements StorageChannel<Rs2ItemStack> {
    private final ItemStorageChannel parent;

    public StorageTrackerEntryPresentAssertionItemStorageChannel(ItemStorageChannel parent) {
        this.parent = parent;
    }

    @Override
    public Optional<Rs2ItemStack> extract(Rs2ItemStack template, long amount, Action action) {
        if (action == Action.EXECUTE) {
            Optional<StorageTracker.Entry> entry = getTracker().getEntry(template);
            assertThat(entry).isPresent();
        }

        return parent.extract(template, amount, action);
    }

    @Override
    public Optional<Rs2ItemStack> insert(Rs2ItemStack template, long amount, Action action) {
        if (action == Action.EXECUTE) {
            Optional<StorageTracker.Entry> entry = getTracker().getEntry(template);
            assertThat(entry).isPresent();
        }

        return parent.insert(template, amount, action);
    }

    @Override
    public Collection<Rs2ItemStack> getStacks() {
        return parent.getStacks();
    }

    @Override
    public long getStored() {
        return parent.getStored();
    }

    @Override
    public void addListener(StackListListener<Rs2ItemStack> listener) {
        parent.addListener(listener);
    }

    @Override
    public void removeListener(StackListListener<Rs2ItemStack> listener) {
        parent.removeListener(listener);
    }

    @Override
    public Optional<Rs2ItemStack> extract(Rs2ItemStack template, long amount, Source source) {
        return parent.extract(template, amount, source);
    }

    @Override
    public Optional<Rs2ItemStack> insert(Rs2ItemStack template, long amount, Source source) {
        return parent.insert(template, amount, source);
    }

    @Override
    public StorageTracker<Rs2ItemStack, ?> getTracker() {
        return parent.getTracker();
    }

    @Override
    public Optional<Rs2ItemStack> get(Rs2ItemStack template) {
        return parent.get(template);
    }

    @Override
    public void sortSources() {
        parent.sortSources();
    }
}
