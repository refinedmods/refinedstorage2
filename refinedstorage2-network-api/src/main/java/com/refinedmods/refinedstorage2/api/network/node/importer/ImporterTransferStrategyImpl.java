package com.refinedmods.refinedstorage2.api.network.node.importer;

import com.refinedmods.refinedstorage2.api.network.Network;
import com.refinedmods.refinedstorage2.api.network.component.StorageNetworkComponent;
import com.refinedmods.refinedstorage2.api.resource.ResourceKey;
import com.refinedmods.refinedstorage2.api.resource.filter.Filter;
import com.refinedmods.refinedstorage2.api.storage.Actor;
import com.refinedmods.refinedstorage2.api.storage.TransferHelper;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannel;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;

import java.util.Iterator;
import java.util.Objects;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.2.1")
public class ImporterTransferStrategyImpl implements ImporterTransferStrategy {
    private final ImporterSource source;
    private final StorageChannelType storageChannelType;
    private final long transferQuota;

    public ImporterTransferStrategyImpl(final ImporterSource source,
                                        final StorageChannelType storageChannelType,
                                        final long transferQuota) {
        this.source = source;
        this.storageChannelType = storageChannelType;
        this.transferQuota = transferQuota;
    }

    @Override
    public boolean transfer(final Filter filter, final Actor actor, final Network network) {
        final StorageChannel storageChannel = network
            .getComponent(StorageNetworkComponent.class)
            .getStorageChannel(storageChannelType);
        return transfer(filter, actor, storageChannel);
    }

    private boolean transfer(final Filter filter, final Actor actor, final StorageChannel storageChannel) {
        long totalTransferred = 0;
        ResourceKey workingResource = null;
        final Iterator<ResourceKey> iterator = source.getResources();
        while (iterator.hasNext() && totalTransferred < transferQuota) {
            final ResourceKey resource = iterator.next();
            if (workingResource != null) {
                totalTransferred += performTransfer(storageChannel, actor, totalTransferred, workingResource, resource);
            } else if (filter.isAllowed(resource)) {
                final long transferred = performTransfer(storageChannel, actor, totalTransferred, resource);
                if (transferred > 0) {
                    workingResource = resource;
                }
                totalTransferred += transferred;
            }
        }
        return totalTransferred > 0;
    }

    private long performTransfer(final StorageChannel storageChannel,
                                 final Actor actor,
                                 final long totalTransferred,
                                 final ResourceKey workingResource,
                                 final ResourceKey resource) {
        if (Objects.equals(workingResource, resource)) {
            return performTransfer(storageChannel, actor, totalTransferred, resource);
        }
        return 0L;
    }

    private long performTransfer(final StorageChannel storageChannel,
                                 final Actor actor,
                                 final long totalTransferred,
                                 final ResourceKey resource) {
        return TransferHelper.transfer(
            resource,
            transferQuota - totalTransferred,
            actor,
            source,
            storageChannel,
            source
        );
    }
}
