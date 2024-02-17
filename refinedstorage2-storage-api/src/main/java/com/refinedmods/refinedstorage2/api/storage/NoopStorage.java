package com.refinedmods.refinedstorage2.api.storage;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;

import java.util.Collection;
import java.util.Collections;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.3.3")
public class NoopStorage<T> implements Storage<T> {
    @Override
    public long extract(final T resource, final long amount, final Action action, final Actor actor) {
        return 0;
    }

    @Override
    public long insert(final T resource, final long amount, final Action action, final Actor actor) {
        return 0;
    }

    @Override
    public Collection<ResourceAmount<T>> getAll() {
        return Collections.emptyList();
    }

    @Override
    public long getStored() {
        return 0;
    }
}
