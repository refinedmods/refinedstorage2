package com.refinedmods.refinedstorage2.platform.common.exporter;

import com.refinedmods.refinedstorage2.api.network.impl.node.exporter.AbstractExporterTransferStrategy;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.storage.InsertableStorage;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannel;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;
import com.refinedmods.refinedstorage2.platform.api.storage.channel.FuzzyStorageChannel;

import java.util.Collection;
import java.util.stream.Collectors;

public abstract class AbstractFuzzyExporterTransferStrategy<T> extends AbstractExporterTransferStrategy<T> {
    protected AbstractFuzzyExporterTransferStrategy(
        final InsertableStorage<T> destination,
        final StorageChannelType<T> storageChannelType,
        final long transferQuota
    ) {
        super(destination, storageChannelType, transferQuota);
    }

    @Override
    protected Collection<T> expand(final T resource, final StorageChannel<T> storageChannel) {
        if (storageChannel instanceof FuzzyStorageChannel<T> fuzzyStorageChannel) {
            return fuzzyStorageChannel
                .getFuzzy(resource)
                .stream()
                .map(ResourceAmount::getResource)
                .collect(Collectors.toSet());
        }
        return super.expand(resource, storageChannel);
    }
}
