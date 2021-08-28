package com.refinedmods.refinedstorage2.platform.fabric.api.storage.disk;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.stack.Rs2Stack;
import com.refinedmods.refinedstorage2.api.stack.list.StackList;
import com.refinedmods.refinedstorage2.api.storage.disk.StorageDiskImpl;

import java.util.Optional;

// TODO: Add test
public class PlatformStorageDiskImpl<T extends Rs2Stack> extends StorageDiskImpl<T> implements PlatformStorageDisk<T> {
    private final Runnable listener;
    private final StorageDiskType<T> type;

    public PlatformStorageDiskImpl(long capacity, StackList<T> list, StorageDiskType<T> type, Runnable listener) {
        super(capacity, list);
        this.listener = listener;
        this.type = type;
    }

    @Override
    public Optional<T> extract(T template, long amount, Action action) {
        Optional<T> extracted = super.extract(template, amount, action);
        if (extracted.isPresent() && action == Action.EXECUTE) {
            listener.run();
        }
        return extracted;
    }

    @Override
    public Optional<T> insert(T template, long amount, Action action) {
        Optional<T> remainder = super.insert(template, amount, action);
        boolean insertedSomething = !remainder.isPresent() || remainder.get().getAmount() != amount;
        if (insertedSomething && action == Action.EXECUTE) {
            listener.run();
        }
        return remainder;
    }

    @Override
    public StorageDiskType<T> getType() {
        return type;
    }
}
