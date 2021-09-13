package com.refinedmods.refinedstorage2.api.storage.composite;

import com.refinedmods.refinedstorage2.api.storage.ProxyStorage;
import com.refinedmods.refinedstorage2.api.storage.Storage;

public class PrioritizedStorage<T> extends ProxyStorage<T> implements Priority {
    private int priority;

    public PrioritizedStorage(int priority, Storage<T> parent) {
        super(parent);
        this.priority = priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    @Override
    public int getPriority() {
        return priority;
    }
}
