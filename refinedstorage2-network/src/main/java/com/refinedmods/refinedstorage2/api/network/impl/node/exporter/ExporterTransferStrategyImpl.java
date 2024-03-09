package com.refinedmods.refinedstorage2.api.network.impl.node.exporter;

import com.refinedmods.refinedstorage2.api.network.Network;
import com.refinedmods.refinedstorage2.api.network.component.StorageNetworkComponent;
import com.refinedmods.refinedstorage2.api.network.node.exporter.ExporterTransferStrategy;
import com.refinedmods.refinedstorage2.api.resource.ResourceKey;
import com.refinedmods.refinedstorage2.api.storage.Actor;
import com.refinedmods.refinedstorage2.api.storage.InsertableStorage;
import com.refinedmods.refinedstorage2.api.storage.TransferHelper;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannel;

import java.util.Collection;
import java.util.Collections;

public class ExporterTransferStrategyImpl implements ExporterTransferStrategy {
    private final InsertableStorage destination;
    private final long transferQuota;

    public ExporterTransferStrategyImpl(final InsertableStorage destination, final long transferQuota) {
        this.destination = destination;
        this.transferQuota = transferQuota;
    }

    /**
     * @param resource       the resource to expand
     * @param storageChannel the storage channel belonging to the resource
     * @return the list of expanded resources, will be tried out in the order of the list. Can be empty.
     */
    protected Collection<ResourceKey> expand(final ResourceKey resource, final StorageChannel storageChannel) {
        return Collections.singletonList(resource);
    }

    @Override
    public boolean transfer(final ResourceKey resource, final Actor actor, final Network network) {
        final StorageChannel storageChannel = network.getComponent(StorageNetworkComponent.class);
        final Collection<ResourceKey> expanded = expand(resource, storageChannel);
        return tryTransferExpanded(actor, storageChannel, expanded);
    }

    private boolean tryTransferExpanded(final Actor actor,
                                        final StorageChannel storageChannel,
                                        final Collection<ResourceKey> expanded) {
        for (final ResourceKey resource : expanded) {
            if (tryTransfer(actor, storageChannel, resource)) {
                return true;
            }
        }
        return false;
    }

    private boolean tryTransfer(final Actor actor, final StorageChannel storageChannel, final ResourceKey resource) {
        final long transferred = TransferHelper.transfer(
            resource,
            transferQuota,
            actor,
            storageChannel,
            destination,
            storageChannel
        );
        return transferred > 0;
    }
}
