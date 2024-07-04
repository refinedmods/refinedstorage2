package com.refinedmods.refinedstorage.api.storage.composite;

import com.refinedmods.refinedstorage.api.storage.AbstractProxyStorage;
import com.refinedmods.refinedstorage.api.storage.Storage;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.3.6")
public class PriorityStorage extends AbstractProxyStorage implements PriorityProvider {
    private int priority;

    private PriorityStorage(final int priority, final Storage delegate) {
        super(delegate);
        this.priority = priority;
    }

    public static PriorityStorage of(final Storage delegate, final int priority) {
        return new PriorityStorage(priority, delegate);
    }

    public void setPriority(final int priority) {
        this.priority = priority;
    }

    @Override
    public int getPriority() {
        return priority;
    }
}

