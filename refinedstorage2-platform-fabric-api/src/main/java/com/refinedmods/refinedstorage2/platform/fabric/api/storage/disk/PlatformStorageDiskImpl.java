package com.refinedmods.refinedstorage2.platform.fabric.api.storage.disk;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.storage.disk.StorageDiskImpl;

// TODO: Add test
public class PlatformStorageDiskImpl<T> extends StorageDiskImpl<T> implements PlatformStorageDisk<T> {
    private final Runnable listener;
    private final StorageDiskType<T> type;

    public PlatformStorageDiskImpl(long capacity, StorageDiskType<T> type, Runnable listener) {
        super(capacity);
        this.listener = listener;
        this.type = type;
    }

    @Override
    public long extract(T resource, long amount, Action action) {
        long extracted = super.extract(resource, amount, action);
        if (extracted > 0 && action == Action.EXECUTE) {
            listener.run();
        }
        return extracted;
    }

    @Override
    public long insert(T resource, long amount, Action action) {
        long remainder = super.insert(resource, amount, action);
        boolean insertedSomething = remainder != amount;
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
