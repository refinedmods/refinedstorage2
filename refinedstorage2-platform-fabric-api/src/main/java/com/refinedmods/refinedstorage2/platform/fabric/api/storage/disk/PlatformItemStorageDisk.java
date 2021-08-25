package com.refinedmods.refinedstorage2.platform.fabric.api.storage.disk;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.stack.item.Rs2ItemStack;
import com.refinedmods.refinedstorage2.api.storage.disk.ItemStorageDisk;
import com.refinedmods.refinedstorage2.platform.fabric.api.Rs2PlatformApiFacade;

import java.util.Optional;

// TODO: Add test
public class PlatformItemStorageDisk extends ItemStorageDisk implements PlatformStorageDisk<Rs2ItemStack> {
    private final Runnable listener;

    public PlatformItemStorageDisk(long capacity, Runnable listener) {
        super(capacity);
        this.listener = listener;
    }

    @Override
    public Optional<Rs2ItemStack> extract(Rs2ItemStack template, long amount, Action action) {
        Optional<Rs2ItemStack> extracted = super.extract(template, amount, action);
        if (extracted.isPresent() && action == Action.EXECUTE) {
            listener.run();
        }
        return extracted;
    }

    @Override
    public Optional<Rs2ItemStack> insert(Rs2ItemStack template, long amount, Action action) {
        Optional<Rs2ItemStack> remainder = super.insert(template, amount, action);
        boolean insertedSomething = !remainder.isPresent() || remainder.get().getAmount() != amount;
        if (insertedSomething && action == Action.EXECUTE) {
            listener.run();
        }
        return remainder;
    }

    @Override
    public StorageDiskType<Rs2ItemStack> getType() {
        return Rs2PlatformApiFacade.INSTANCE.getItemStorageDiskType();
    }
}
