package com.refinedmods.refinedstorage2.api.network.node.externalstorage;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.storage.Actor;
import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.external.ExternalStorageProvider;

import java.util.Iterator;

public class StorageExternalStorageProvider<T> implements ExternalStorageProvider<T> {
    private final Storage<T> storage;

    public StorageExternalStorageProvider(final Storage<T> storage) {
        this.storage = storage;
    }

    @Override
    public long extract(final T resource, final long amount, final Action action, final Actor actor) {
        return storage.extract(resource, amount, action, actor);
    }

    @Override
    public long insert(final T resource, final long amount, final Action action, final Actor actor) {
        return storage.insert(resource, amount, action, actor);
    }

    @Override
    public Iterator<ResourceAmount<T>> iterator() {
        return storage.getAll().iterator();
    }
}
