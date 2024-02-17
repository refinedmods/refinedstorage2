package com.refinedmods.refinedstorage2.platform.forge.importer;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.network.node.importer.ImporterSource;
import com.refinedmods.refinedstorage2.api.storage.Actor;
import com.refinedmods.refinedstorage2.api.storage.ExtractableStorage;
import com.refinedmods.refinedstorage2.api.storage.InsertableStorage;
import com.refinedmods.refinedstorage2.platform.api.exporter.AmountOverride;
import com.refinedmods.refinedstorage2.platform.api.support.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.forge.storage.CapabilityCache;
import com.refinedmods.refinedstorage2.platform.forge.storage.ItemHandlerExtractableStorage;
import com.refinedmods.refinedstorage2.platform.forge.storage.ItemHandlerInsertableStorage;

import java.util.Iterator;

class ItemHandlerImporterSource implements ImporterSource<ItemResource> {
    private final CapabilityCache capabilityCache;
    private final InsertableStorage<ItemResource> insertTarget;
    private final ExtractableStorage<ItemResource> extractTarget;

    ItemHandlerImporterSource(final CapabilityCache capabilityCache,
                              final AmountOverride amountOverride) {
        this.capabilityCache = capabilityCache;
        this.insertTarget = new ItemHandlerInsertableStorage(capabilityCache, AmountOverride.NONE);
        this.extractTarget = new ItemHandlerExtractableStorage(capabilityCache, amountOverride);
    }

    @Override
    public Iterator<ItemResource> getResources() {
        return capabilityCache.getItemIterator();
    }

    @Override
    public long extract(final ItemResource resource, final long amount, final Action action, final Actor actor) {
        return extractTarget.extract(resource, amount, action, actor);
    }

    @Override
    public long insert(final ItemResource resource, final long amount, final Action action, final Actor actor) {
        return insertTarget.insert(resource, amount, action, actor);
    }
}
