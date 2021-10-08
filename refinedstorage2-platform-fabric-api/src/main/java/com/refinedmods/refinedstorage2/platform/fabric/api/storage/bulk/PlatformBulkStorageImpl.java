package com.refinedmods.refinedstorage2.platform.fabric.api.storage.bulk;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.storage.bulk.BulkStorageImpl;

// TODO: Add test
public class PlatformBulkStorageImpl<T> extends BulkStorageImpl<T> implements PlatformBulkStorage<T> {
    private final Runnable listener;
    private final StorageDiskType<T> type;

    public PlatformBulkStorageImpl(long capacity, StorageDiskType<T> type, Runnable listener) {
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
