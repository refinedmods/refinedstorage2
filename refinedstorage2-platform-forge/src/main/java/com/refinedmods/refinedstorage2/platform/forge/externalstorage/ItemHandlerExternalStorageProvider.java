package com.refinedmods.refinedstorage2.platform.forge.externalstorage;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.storage.Actor;
import com.refinedmods.refinedstorage2.api.storage.ExtractableStorage;
import com.refinedmods.refinedstorage2.api.storage.InsertableStorage;
import com.refinedmods.refinedstorage2.api.storage.external.ExternalStorageProvider;
import com.refinedmods.refinedstorage2.platform.api.exporter.AmountOverride;
import com.refinedmods.refinedstorage2.platform.api.support.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.forge.storage.InteractionCoordinates;
import com.refinedmods.refinedstorage2.platform.forge.storage.ItemHandlerExtractableStorage;
import com.refinedmods.refinedstorage2.platform.forge.storage.ItemHandlerInsertableStorage;

import java.util.Iterator;

public class ItemHandlerExternalStorageProvider implements ExternalStorageProvider<ItemResource> {
    private final InteractionCoordinates interactionCoordinates;
    private final InsertableStorage<ItemResource> insertTarget;
    private final ExtractableStorage<ItemResource> extractTarget;

    public ItemHandlerExternalStorageProvider(final InteractionCoordinates interactionCoordinates) {
        this.interactionCoordinates = interactionCoordinates;
        this.insertTarget = new ItemHandlerInsertableStorage(interactionCoordinates, AmountOverride.NONE);
        this.extractTarget = new ItemHandlerExtractableStorage(interactionCoordinates, AmountOverride.NONE);
    }

    @Override
    public long extract(final ItemResource resource, final long amount, final Action action, final Actor actor) {
        return extractTarget.extract(resource, amount, action, actor);
    }

    @Override
    public long insert(final ItemResource resource, final long amount, final Action action, final Actor actor) {
        return insertTarget.insert(resource, amount, action, actor);
    }

    @Override
    public Iterator<ResourceAmount<ItemResource>> iterator() {
        return interactionCoordinates.getItemAmountIterator();
    }
}
