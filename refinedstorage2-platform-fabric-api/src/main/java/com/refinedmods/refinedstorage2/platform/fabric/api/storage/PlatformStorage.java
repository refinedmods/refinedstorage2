package com.refinedmods.refinedstorage2.platform.fabric.api.storage;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.storage.InMemoryStorageImpl;
import com.refinedmods.refinedstorage2.platform.fabric.api.storage.type.StorageType;

public class PlatformStorage<T> extends InMemoryStorageImpl<T> implements StorageTypeAccessor<T> {
    private final Runnable listener;
    private final StorageType<T> type;

    public PlatformStorage(StorageType<T> type, Runnable listener) {
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
    public StorageType<T> getType() {
        return type;
    }
}
