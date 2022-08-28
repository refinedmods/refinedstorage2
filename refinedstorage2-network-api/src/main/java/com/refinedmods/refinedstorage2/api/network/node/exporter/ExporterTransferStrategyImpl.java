package com.refinedmods.refinedstorage2.api.network.node.exporter;

import com.refinedmods.refinedstorage2.api.network.Network;
import com.refinedmods.refinedstorage2.api.network.component.StorageNetworkComponent;
import com.refinedmods.refinedstorage2.api.storage.Actor;
import com.refinedmods.refinedstorage2.api.storage.InsertableStorage;
import com.refinedmods.refinedstorage2.api.storage.TransferHelper;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannel;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;

public class ExporterTransferStrategyImpl<T> implements ExporterTransferStrategy {
    private final T resource;
    private final InsertableStorage<T> destination;
    private final StorageChannelType<T> storageChannelType;
    private final long transferQuota;

    public ExporterTransferStrategyImpl(final T resource,
                                        final InsertableStorage<T> destination,
                                        final StorageChannelType<T> storageChannelType,
                                        final long transferQuota) {
        this.resource = resource;
        this.destination = destination;
        this.storageChannelType = storageChannelType;
        this.transferQuota = transferQuota;
    }

    @Override
    public boolean transfer(final Actor actor, final Network network) {
        final StorageChannel<T> storageChannel = network.getComponent(StorageNetworkComponent.class)
            .getStorageChannel(storageChannelType);

        return TransferHelper.transfer(resource, transferQuota, actor, storageChannel, destination, storageChannel) > 0;
    }
}
