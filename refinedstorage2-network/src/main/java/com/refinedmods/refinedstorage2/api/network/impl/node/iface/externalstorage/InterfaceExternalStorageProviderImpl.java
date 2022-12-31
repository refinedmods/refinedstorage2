package com.refinedmods.refinedstorage2.api.network.impl.node.iface.externalstorage;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.network.impl.node.iface.InterfaceExportState;
import com.refinedmods.refinedstorage2.api.network.impl.node.iface.InterfaceNetworkNode;
import com.refinedmods.refinedstorage2.api.network.node.NetworkNodeActor;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.storage.Actor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class InterfaceExternalStorageProviderImpl<T> implements InterfaceExternalStorageProvider<T> {
    private final InterfaceNetworkNode<T> networkNode;

    public InterfaceExternalStorageProviderImpl(final InterfaceNetworkNode<T> networkNode) {
        this.networkNode = networkNode;
    }

    @Override
    public long extract(final T resource, final long amount, final Action action, final Actor actor) {
        if (isAnotherInterfaceActingAsExternalStorage(actor)) {
            return 0;
        }
        final InterfaceExportState<T> exportState = networkNode.getExportState();
        if (exportState == null) {
            return 0;
        }
        return doExtract(resource, amount, action, exportState);
    }

    private long doExtract(final T resource,
                           final long amount,
                           final Action action,
                           final InterfaceExportState<T> exportState) {
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
        if (isAnotherInterfaceActingAsExternalStorage(actor)) {
            return 0;
        }
        final InterfaceExportState<T> exportState = networkNode.getExportState();
        if (exportState == null) {
            return 0;
        }
        return exportState.insert(resource, amount, action, actor);
    }

    private boolean isAnotherInterfaceActingAsExternalStorage(final Actor actor) {
        return actor instanceof NetworkNodeActor networkNodeActor
            && networkNodeActor.networkNode() instanceof InterfaceNetworkNode<?> actingInterface
            && actingInterface.isActingAsExternalStorage();
    }

    @Override
    public Iterator<ResourceAmount<T>> iterator() {
        final InterfaceExportState<T> exportState = networkNode.getExportState();
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

    @Override
    public InterfaceNetworkNode<T> getInterface() {
        return networkNode;
    }
}
