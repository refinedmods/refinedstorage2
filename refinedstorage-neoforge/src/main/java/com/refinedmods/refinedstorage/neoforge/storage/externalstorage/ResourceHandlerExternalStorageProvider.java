package com.refinedmods.refinedstorage.neoforge.storage.externalstorage;

import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.api.storage.ExtractableStorage;
import com.refinedmods.refinedstorage.api.storage.InsertableStorage;
import com.refinedmods.refinedstorage.api.storage.external.ExternalStorageProvider;
import com.refinedmods.refinedstorage.neoforge.storage.ResourceHandlerExtractableStorage;
import com.refinedmods.refinedstorage.neoforge.storage.ResourceHandlerInsertableStorage;
import com.refinedmods.refinedstorage.neoforge.storage.ResourceHandlerProvider;

import java.util.Iterator;
import java.util.Optional;
import java.util.function.Function;

import net.neoforged.neoforge.transfer.resource.Resource;

class ResourceHandlerExternalStorageProvider<T extends Resource> implements ExternalStorageProvider {
    private final ResourceHandlerProvider<T> provider;
    private final InsertableStorage insertTarget;
    private final ExtractableStorage extractTarget;

    ResourceHandlerExternalStorageProvider(final ResourceHandlerProvider<T> provider,
                                           final Function<ResourceKey, Optional<T>> mapper) {
        this.provider = provider;
        this.insertTarget = new ResourceHandlerInsertableStorage<>(provider, mapper);
        this.extractTarget = new ResourceHandlerExtractableStorage<>(provider, mapper);
    }

    @Override
    public long extract(final ResourceKey resource, final long amount, final Action action, final Actor actor) {
        return extractTarget.extract(resource, amount, action, actor);
    }

    @Override
    public long insert(final ResourceKey resource, final long amount, final Action action, final Actor actor) {
        return insertTarget.insert(resource, amount, action, actor);
    }

    @Override
    public Iterator<ResourceAmount> iterator() {
        return provider.amountIterator();
    }
}
