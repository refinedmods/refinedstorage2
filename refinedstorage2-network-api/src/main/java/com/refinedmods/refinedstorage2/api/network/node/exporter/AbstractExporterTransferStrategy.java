package com.refinedmods.refinedstorage2.api.network.node.exporter;

import com.refinedmods.refinedstorage2.api.network.Network;
import com.refinedmods.refinedstorage2.api.network.component.StorageNetworkComponent;
import com.refinedmods.refinedstorage2.api.storage.Actor;
import com.refinedmods.refinedstorage2.api.storage.InsertableStorage;
import com.refinedmods.refinedstorage2.api.storage.TransferHelper;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannel;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;

import javax.annotation.Nullable;

public abstract class AbstractExporterTransferStrategy<T> implements ExporterTransferStrategy {
    private final InsertableStorage<T> destination;
    private final StorageChannelType<T> storageChannelType;
    private final long transferQuota;

    public AbstractExporterTransferStrategy(final InsertableStorage<T> destination,
                                            final StorageChannelType<T> storageChannelType,
                                            final long transferQuota) {
        this.destination = destination;
        this.storageChannelType = storageChannelType;
        this.transferQuota = transferQuota;
    }

    @Nullable
    protected abstract T tryConvert(Object resource);

    @Override
    public boolean transfer(final Object resource, final Actor actor, final Network network) {
        final T converted = tryConvert(resource);
        if (converted == null) {
            return false;
        }
        return doTransfer(converted, actor, network);
    }

    private boolean doTransfer(final T converted, final Actor actor, final Network network) {
        final StorageChannel<T> storageChannel = network.getComponent(StorageNetworkComponent.class)
            .getStorageChannel(storageChannelType);

        final long transferred = TransferHelper.transfer(
            converted,
            transferQuota,
            actor,
            storageChannel,
            destination,
            storageChannel
        );
        return transferred > 0;
    }
}