package com.refinedmods.refinedstorage.neoforge.importer;

import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.network.impl.node.importer.ImporterSource;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.neoforge.storage.ResourceHandlerExtractableStorage;
import com.refinedmods.refinedstorage.neoforge.storage.ResourceHandlerInsertableStorage;
import com.refinedmods.refinedstorage.neoforge.storage.ResourceHandlerProvider;

import java.util.Iterator;
import java.util.Optional;
import java.util.function.Function;

import net.neoforged.neoforge.transfer.resource.Resource;

class ResourceHandlerImporterSource<T extends Resource> implements ImporterSource {
    private final ResourceHandlerProvider<T> provider;
    private final ResourceHandlerInsertableStorage<T> insertTarget;
    private final ResourceHandlerExtractableStorage<T> extractTarget;

    ResourceHandlerImporterSource(final ResourceHandlerProvider<T> provider,
                                  final Function<ResourceKey, Optional<T>> mapper) {
        this.provider = provider;
        this.insertTarget = new ResourceHandlerInsertableStorage<>(provider, mapper);
        this.extractTarget = new ResourceHandlerExtractableStorage<>(provider, mapper);
    }

    public long getAmount(final ResourceKey resource) {
        return extractTarget.getAmount(resource);
    }

    @Override
    public Iterator<ResourceKey> getResources() {
        return provider.iterator();
    }

    @Override
    public long extract(final ResourceKey resource, final long amount, final Action action, final Actor actor) {
        return extractTarget.extract(resource, amount, action, actor);
    }

    @Override
    public long insert(final ResourceKey resource, final long amount, final Action action, final Actor actor) {
        return insertTarget.insert(resource, amount, action, actor);
    }
}
