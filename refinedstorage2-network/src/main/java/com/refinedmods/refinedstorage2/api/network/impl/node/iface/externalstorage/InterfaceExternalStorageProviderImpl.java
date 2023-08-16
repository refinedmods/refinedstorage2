package com.refinedmods.refinedstorage2.api.network.impl.node.iface.externalstorage;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.network.impl.node.iface.InterfaceExportState;
import com.refinedmods.refinedstorage2.api.network.impl.node.iface.InterfaceNetworkNode;
import com.refinedmods.refinedstorage2.api.network.node.NetworkNodeActor;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.storage.Actor;
import com.refinedmods.refinedstorage2.api.storage.ResourceTemplate;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class InterfaceExternalStorageProviderImpl<T> implements InterfaceExternalStorageProvider<T> {
    private final InterfaceNetworkNode networkNode;
    private final StorageChannelType<T> storageChannelType;

    public InterfaceExternalStorageProviderImpl(final InterfaceNetworkNode networkNode,
                                                final StorageChannelType<T> storageChannelType) {
        this.networkNode = networkNode;
        this.storageChannelType = storageChannelType;
    }

    @Override
    public long extract(final T resource, final long amount, final Action action, final Actor actor) {
        if (isAnotherInterfaceActingAsExternalStorage(actor)) {
            return 0;
        }
        final InterfaceExportState exportState = networkNode.getExportState();
        if (exportState == null) {
            return 0;
        }
        return doExtract(resource, amount, action, exportState);
    }

    private long doExtract(final T resource,
                           final long amount,
                           final Action action,
                           final InterfaceExportState exportState) {
        long extracted = 0;
        for (int i = 0; i < exportState.getSlots(); ++i) {
            final ResourceTemplate<?> exportedResource = exportState.getExportedResource(i);
            if (exportedResource == null || !resource.equals(exportedResource.resource())) {
                continue;
            }
            final long stillNeeded = amount - extracted;
            final long toExtract = Math.min(
                exportState.getExportedAmount(i),
                stillNeeded
            );
            if (action == Action.EXECUTE) {
                exportState.shrinkExportedAmount(i, stillNeeded);
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
        final InterfaceExportState exportState = networkNode.getExportState();
        if (exportState == null) {
            return 0;
        }
        return exportState.insert(storageChannelType, resource, amount, action);
    }

    private boolean isAnotherInterfaceActingAsExternalStorage(final Actor actor) {
        return actor instanceof NetworkNodeActor networkNodeActor
            && networkNodeActor.networkNode() instanceof InterfaceNetworkNode actingInterface
            && actingInterface.isActingAsExternalStorage();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Iterator<ResourceAmount<T>> iterator() {
        final InterfaceExportState exportState = networkNode.getExportState();
        if (exportState == null) {
            return Collections.emptyIterator();
        }
        final List<ResourceAmount<T>> slots = new ArrayList<>();
        for (int i = 0; i < exportState.getSlots(); ++i) {
            final ResourceTemplate<?> resource = exportState.getExportedResource(i);
            if (resource == null || resource.storageChannelType() != storageChannelType) {
                continue;
            }
            slots.add(getResourceAmount((ResourceTemplate<T>) resource, exportState.getExportedAmount(i)));
        }
        return slots.iterator();
    }

    private ResourceAmount<T> getResourceAmount(final ResourceTemplate<T> resource,
                                                final long amount) {
        return new ResourceAmount<>(resource.resource(), amount);
    }

    @Override
    public InterfaceNetworkNode getInterface() {
        return networkNode;
    }
}
