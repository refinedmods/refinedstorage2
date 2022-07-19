package com.refinedmods.refinedstorage2.api.network.node.importer;

import com.refinedmods.refinedstorage2.api.core.filter.Filter;
import com.refinedmods.refinedstorage2.api.network.Network;
import com.refinedmods.refinedstorage2.api.network.component.StorageNetworkComponent;
import com.refinedmods.refinedstorage2.api.storage.Actor;
import com.refinedmods.refinedstorage2.api.storage.TransferHelper;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannel;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;

import java.util.Iterator;
import java.util.Objects;

public class ImporterTransferStrategyImpl<T> implements ImporterTransferStrategy {
    private final ImporterSource<T> source;
    private final StorageChannelType<T> storageChannelType;
    private final long transferQuota;

    public ImporterTransferStrategyImpl(final ImporterSource<T> source,
                                        final StorageChannelType<T> storageChannelType,
                                        final long transferQuota) {
        this.source = source;
        this.storageChannelType = storageChannelType;
        this.transferQuota = transferQuota;
    }

    @Override
    public boolean transfer(final Filter filter, final Actor actor, final Network network) {
        final StorageChannel<T> storageChannel = network
            .getComponent(StorageNetworkComponent.class)
            .getStorageChannel(storageChannelType);
        return transfer(actor, storageChannel);
    }

    private boolean transfer(final Actor actor, final StorageChannel<T> storageChannel) {
        long totalTransferred = 0;
        T workingResource = null;
        final Iterator<T> iterator = source.getResources();
        while (iterator.hasNext() && totalTransferred < transferQuota) {
            final T resource = iterator.next();
            if (workingResource != null) {
                totalTransferred += performTransfer(storageChannel, actor, totalTransferred, workingResource, resource);
            } else {
                final long transferred = performTransfer(storageChannel, actor, totalTransferred, resource);
                if (transferred > 0) {
                    workingResource = resource;
                }
                totalTransferred += transferred;
            }
        }
        return totalTransferred > 0;
    }

    private long performTransfer(final StorageChannel<T> storageChannel,
                                 final Actor actor,
                                 final long totalTransferred,
                                 final T workingResource,
                                 final T resourceInSlot) {
        if (Objects.equals(workingResource, resourceInSlot)) {
            return performTransfer(storageChannel, actor, totalTransferred, resourceInSlot);
        }
        return 0L;
    }

    private long performTransfer(final StorageChannel<T> storageChannel,
                                 final Actor actor,
                                 final long totalTransferred,
                                 final T resourceInSlot) {
        return TransferHelper.transfer(
            resourceInSlot,
            transferQuota - totalTransferred,
            actor,
            source,
            storageChannel
        );
    }
}
