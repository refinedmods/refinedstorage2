package com.refinedmods.refinedstorage.platform.neoforge.importer;

import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.network.impl.node.importer.ImporterSource;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.api.storage.ExtractableStorage;
import com.refinedmods.refinedstorage.api.storage.InsertableStorage;
import com.refinedmods.refinedstorage.platform.api.exporter.AmountOverride;
import com.refinedmods.refinedstorage.platform.neoforge.storage.CapabilityCache;
import com.refinedmods.refinedstorage.platform.neoforge.storage.ItemHandlerExtractableStorage;
import com.refinedmods.refinedstorage.platform.neoforge.storage.ItemHandlerInsertableStorage;

import java.util.Iterator;

class ItemHandlerImporterSource implements ImporterSource {
    private final CapabilityCache capabilityCache;
    private final InsertableStorage insertTarget;
    private final ExtractableStorage extractTarget;

    ItemHandlerImporterSource(final CapabilityCache capabilityCache,
                              final AmountOverride amountOverride) {
        this.capabilityCache = capabilityCache;
        this.insertTarget = new ItemHandlerInsertableStorage(capabilityCache, AmountOverride.NONE);
        this.extractTarget = new ItemHandlerExtractableStorage(capabilityCache, amountOverride);
    }

    @Override
    public Iterator<ResourceKey> getResources() {
        return capabilityCache.getItemIterator();
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
