package com.refinedmods.refinedstorage2.api.network.impl.node.exporter;

import com.refinedmods.refinedstorage2.api.network.Network;
import com.refinedmods.refinedstorage2.api.network.component.StorageNetworkComponent;
import com.refinedmods.refinedstorage2.api.network.node.exporter.ExporterTransferStrategy;
import com.refinedmods.refinedstorage2.api.storage.Actor;
import com.refinedmods.refinedstorage2.api.storage.InsertableStorage;
import com.refinedmods.refinedstorage2.api.storage.TransferHelper;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannel;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;

import java.util.Collection;
import java.util.Collections;
import javax.annotation.Nullable;

public abstract class AbstractExporterTransferStrategy<T> implements ExporterTransferStrategy {
    private final InsertableStorage<T> destination;
    private final StorageChannelType<T> storageChannelType;
    private final long transferQuota;

    protected AbstractExporterTransferStrategy(final InsertableStorage<T> destination,
                                               final StorageChannelType<T> storageChannelType,
                                               final long transferQuota) {
        this.destination = destination;
        this.storageChannelType = storageChannelType;
        this.transferQuota = transferQuota;
    }

    @Nullable
    protected abstract T tryConvert(Object resource);

    /**
     * @param resource       the resource to expand
     * @param storageChannel the storage channel belonging to the resource
     * @return the list of expanded resources, will be tried out in the order of the list. Can be empty.
     */
    protected Collection<T> expand(final T resource, final StorageChannel<T> storageChannel) {
        return Collections.singletonList(resource);
    }

    @Override
    public boolean transfer(final Object resource, final Actor actor, final Network network) {
        final T converted = tryConvert(resource);
        if (converted == null) {
            return false;
        }
        return tryTransferConverted(converted, actor, network);
    }

    private boolean tryTransferConverted(final T converted, final Actor actor, final Network network) {
        final StorageChannel<T> storageChannel = network.getComponent(StorageNetworkComponent.class)
            .getStorageChannel(storageChannelType);
        final Collection<T> expanded = expand(converted, storageChannel);
        return tryTransferExpanded(actor, storageChannel, expanded);
    }

    private boolean tryTransferExpanded(final Actor actor,
                                        final StorageChannel<T> storageChannel,
                                        final Collection<T> expanded) {
        for (final T resource : expanded) {
            if (tryTransfer(actor, storageChannel, resource)) {
                return true;
            }
        }
        return false;
    }

    private boolean tryTransfer(final Actor actor, final StorageChannel<T> storageChannel, final T resource) {
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
