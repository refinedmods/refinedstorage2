package com.refinedmods.refinedstorage.platform.neoforge.storage.externalstorage;

import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.api.storage.ExtractableStorage;
import com.refinedmods.refinedstorage.api.storage.InsertableStorage;
import com.refinedmods.refinedstorage.api.storage.external.ExternalStorageProvider;
import com.refinedmods.refinedstorage.platform.api.exporter.AmountOverride;
import com.refinedmods.refinedstorage.platform.neoforge.storage.CapabilityCache;
import com.refinedmods.refinedstorage.platform.neoforge.storage.FluidHandlerExtractableStorage;
import com.refinedmods.refinedstorage.platform.neoforge.storage.FluidHandlerInsertableStorage;

import java.util.Iterator;

class FluidHandlerExternalStorageProvider implements ExternalStorageProvider {
    private final CapabilityCache capabilityCache;
    private final InsertableStorage insertTarget;
    private final ExtractableStorage extractTarget;

    FluidHandlerExternalStorageProvider(final CapabilityCache capabilityCache) {
        this.capabilityCache = capabilityCache;
        this.insertTarget = new FluidHandlerInsertableStorage(capabilityCache, AmountOverride.NONE);
        this.extractTarget = new FluidHandlerExtractableStorage(capabilityCache, AmountOverride.NONE);
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
        return capabilityCache.getFluidAmountIterator();
    }
}
