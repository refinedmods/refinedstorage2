package com.refinedmods.refinedstorage2.api.network.node.iface;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.storage.Actor;
import com.refinedmods.refinedstorage2.api.storage.external.ExternalStorageProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class InterfaceExternalStorageProvider<T> implements ExternalStorageProvider<T> {
    private final InterfaceExportStateProvider<T> exportStateProvider;

    public InterfaceExternalStorageProvider(final InterfaceExportStateProvider<T> exportStateProvider) {
        this.exportStateProvider = exportStateProvider;
    }

    @Override
    public long extract(final T resource, final long amount, final Action action, final Actor actor) {
        final InterfaceExportState<T> exportState = exportStateProvider.getExportState();
        if (exportState == null) {
            return 0;
        }
        long extracted = 0;
        for (int i = 0; i < exportState.getSlots(); ++i) {
            if (!resource.equals(exportState.getCurrentlyExportedResource(i))) {
                continue;
            }
            final long stillNeeded = amount - extracted;
            final long toExtract = Math.min(
                exportState.getCurrentlyExportedResourceAmount(i),
                stillNeeded
            );
            if (action == Action.EXECUTE) {
                exportState.decrementCurrentlyExportedAmount(i, stillNeeded);
            }
            extracted += toExtract;
        }
        return extracted;
    }

    @Override
    public long insert(final T resource, final long amount, final Action action, final Actor actor) {
        final InterfaceExportState<T> exportState = exportStateProvider.getExportState();
        if (exportState == null) {
            return 0;
        }
        return exportState.insert(resource, amount, action, actor);
    }

    @Override
    public Iterator<ResourceAmount<T>> iterator() {
        final InterfaceExportState<T> exportState = exportStateProvider.getExportState();
        if (exportState == null) {
            return Collections.emptyIterator();
        }
        final List<ResourceAmount<T>> slots = new ArrayList<>();
        for (int i = 0; i < exportState.getSlots(); ++i) {
            final T resource = exportState.getCurrentlyExportedResource(i);
            if (resource == null) {
                continue;
            }
            slots.add(new ResourceAmount<>(
                resource,
                exportState.getCurrentlyExportedResourceAmount(i)
            ));
        }
        return slots.iterator();
    }
}
