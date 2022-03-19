package com.refinedmods.refinedstorage2.platform.api.storage;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.storage.ProxyStorage;
import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.platform.api.storage.type.StorageType;

public class PlatformStorage<T> extends ProxyStorage<T> implements StorageTypeAccessor<T> {
    private final Runnable listener;
    private final StorageType<T> type;

    public PlatformStorage(Storage<T> parent, StorageType<T> type, Runnable listener) {
        super(parent);
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
        long inserted = super.insert(resource, amount, action);
        if (inserted > 0 && action == Action.EXECUTE) {
            listener.run();
        }
        return inserted;
    }

    @Override
    public StorageType<T> getType() {
        return type;
    }
}
