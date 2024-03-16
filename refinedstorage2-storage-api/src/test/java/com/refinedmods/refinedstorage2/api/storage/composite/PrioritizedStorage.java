package com.refinedmods.refinedstorage2.api.storage.composite;

import com.refinedmods.refinedstorage2.api.storage.AbstractProxyStorage;
import com.refinedmods.refinedstorage2.api.storage.Storage;

public class PrioritizedStorage extends AbstractProxyStorage implements Priority {
    private int priority;

    public PrioritizedStorage(final int priority, final Storage delegate) {
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
