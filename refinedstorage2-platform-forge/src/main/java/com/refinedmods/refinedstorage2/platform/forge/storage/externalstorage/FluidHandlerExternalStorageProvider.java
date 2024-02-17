package com.refinedmods.refinedstorage2.platform.forge.storage.externalstorage;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.storage.Actor;
import com.refinedmods.refinedstorage2.api.storage.ExtractableStorage;
import com.refinedmods.refinedstorage2.api.storage.InsertableStorage;
import com.refinedmods.refinedstorage2.api.storage.external.ExternalStorageProvider;
import com.refinedmods.refinedstorage2.platform.api.exporter.AmountOverride;
import com.refinedmods.refinedstorage2.platform.api.support.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.forge.storage.CapabilityCache;
import com.refinedmods.refinedstorage2.platform.forge.storage.FluidHandlerExtractableStorage;
import com.refinedmods.refinedstorage2.platform.forge.storage.FluidHandlerInsertableStorage;

import java.util.Iterator;

class FluidHandlerExternalStorageProvider implements ExternalStorageProvider<FluidResource> {
    private final CapabilityCache capabilityCache;
    private final InsertableStorage<FluidResource> insertTarget;
    private final ExtractableStorage<FluidResource> extractTarget;

    FluidHandlerExternalStorageProvider(final CapabilityCache capabilityCache) {
        this.capabilityCache = capabilityCache;
        this.insertTarget = new FluidHandlerInsertableStorage(capabilityCache, AmountOverride.NONE);
        this.extractTarget = new FluidHandlerExtractableStorage(capabilityCache, AmountOverride.NONE);
    }

    @Override
    public long extract(final FluidResource resource, final long amount, final Action action, final Actor actor) {
        return extractTarget.extract(resource, amount, action, actor);
    }

    @Override
    public long insert(final FluidResource resource, final long amount, final Action action, final Actor actor) {
        return insertTarget.insert(resource, amount, action, actor);
    }

    @Override
    public Iterator<ResourceAmount<FluidResource>> iterator() {
        return capabilityCache.getFluidAmountIterator();
    }
}
