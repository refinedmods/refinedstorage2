package com.refinedmods.refinedstorage.api.network.impl.node.externalstorage;

import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.api.storage.Storage;
import com.refinedmods.refinedstorage.api.storage.external.ExternalStorageProvider;

import java.util.Iterator;

public class StorageExternalStorageProvider implements ExternalStorageProvider {
    private final Storage storage;

    public StorageExternalStorageProvider(final Storage storage) {
        this.storage = storage;
    }

    @Override
    public long extract(final ResourceKey resource, final long amount, final Action action, final Actor actor) {
        return storage.extract(resource, amount, action, actor);
    }

    @Override
    public long insert(final ResourceKey resource, final long amount, final Action action, final Actor actor) {
        return storage.insert(resource, amount, action, actor);
    }

    @Override
    public Iterator<ResourceAmount> iterator() {
        return storage.getAll().iterator();
    }
}
