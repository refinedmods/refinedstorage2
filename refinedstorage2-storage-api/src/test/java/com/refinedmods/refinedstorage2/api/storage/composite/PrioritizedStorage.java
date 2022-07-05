package com.refinedmods.refinedstorage2.api.storage.composite;

import com.refinedmods.refinedstorage2.api.storage.AbstractProxyStorage;
import com.refinedmods.refinedstorage2.api.storage.Storage;

public class PrioritizedStorage<T> extends AbstractProxyStorage<T> implements Priority {
    private int priority;

    public PrioritizedStorage(final int priority, final Storage<T> delegate) {
        super(delegate);
        this.priority = priority;
    }

    public void setPriority(final int priority) {
        this.priority = priority;
    }

    @Override
    public int getPriority() {
        return priority;
    }
}
