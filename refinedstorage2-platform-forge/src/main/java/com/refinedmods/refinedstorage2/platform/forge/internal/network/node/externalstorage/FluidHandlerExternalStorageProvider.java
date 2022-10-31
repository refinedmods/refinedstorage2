package com.refinedmods.refinedstorage2.platform.forge.internal.network.node.externalstorage;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.storage.Actor;
import com.refinedmods.refinedstorage2.api.storage.ExtractableStorage;
import com.refinedmods.refinedstorage2.api.storage.InsertableStorage;
import com.refinedmods.refinedstorage2.api.storage.external.ExternalStorageProvider;
import com.refinedmods.refinedstorage2.platform.api.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.forge.internal.storage.FluidHandlerExtractableStorage;
import com.refinedmods.refinedstorage2.platform.forge.internal.storage.FluidHandlerInsertableStorage;
import com.refinedmods.refinedstorage2.platform.forge.internal.storage.InteractionCoordinates;

import java.util.Iterator;

public class FluidHandlerExternalStorageProvider implements ExternalStorageProvider<FluidResource> {
    private final InteractionCoordinates interactionCoordinates;
    private final InsertableStorage<FluidResource> insertTarget;
    private final ExtractableStorage<FluidResource> extractTarget;

    public FluidHandlerExternalStorageProvider(final InteractionCoordinates interactionCoordinates) {
        this.interactionCoordinates = interactionCoordinates;
        this.insertTarget = new FluidHandlerInsertableStorage(interactionCoordinates);
        this.extractTarget = new FluidHandlerExtractableStorage(interactionCoordinates);
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
        return interactionCoordinates.getFluidAmountIterator();
    }
}
