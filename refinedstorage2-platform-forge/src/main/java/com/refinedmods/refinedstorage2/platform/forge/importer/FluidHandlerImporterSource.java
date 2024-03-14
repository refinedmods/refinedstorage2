package com.refinedmods.refinedstorage2.platform.forge.importer;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.network.impl.node.importer.ImporterSource;
import com.refinedmods.refinedstorage2.api.resource.ResourceKey;
import com.refinedmods.refinedstorage2.api.storage.Actor;
import com.refinedmods.refinedstorage2.api.storage.ExtractableStorage;
import com.refinedmods.refinedstorage2.api.storage.InsertableStorage;
import com.refinedmods.refinedstorage2.platform.api.exporter.AmountOverride;
import com.refinedmods.refinedstorage2.platform.forge.storage.CapabilityCache;
import com.refinedmods.refinedstorage2.platform.forge.storage.FluidHandlerExtractableStorage;
import com.refinedmods.refinedstorage2.platform.forge.storage.FluidHandlerInsertableStorage;

import java.util.Iterator;

class FluidHandlerImporterSource implements ImporterSource {
    private final CapabilityCache capabilityCache;
    private final InsertableStorage insertTarget;
    private final ExtractableStorage extractTarget;

    FluidHandlerImporterSource(final CapabilityCache capabilityCache,
                               final AmountOverride amountOverride) {
        this.capabilityCache = capabilityCache;
        this.insertTarget = new FluidHandlerInsertableStorage(capabilityCache, AmountOverride.NONE);
        this.extractTarget = new FluidHandlerExtractableStorage(capabilityCache, amountOverride);
    }

    @Override
    public Iterator<ResourceKey> getResources() {
        return capabilityCache.getFluidIterator();
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
