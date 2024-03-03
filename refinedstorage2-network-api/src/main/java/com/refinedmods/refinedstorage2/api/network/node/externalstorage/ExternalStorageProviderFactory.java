package com.refinedmods.refinedstorage2.api.network.node.externalstorage;

import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;
import com.refinedmods.refinedstorage2.api.storage.external.ExternalStorageProvider;

import java.util.Optional;

import org.apiguardian.api.API;

/**
 * Provides the external storage with an {@link ExternalStorageProvider} for a given {@link StorageChannelType}.
 */
@API(status = API.Status.STABLE, since = "2.0.0-milestone.2.4")
@FunctionalInterface
public interface ExternalStorageProviderFactory {
    /**
     * @param channelType the channel type
     * @return the external storage provider
     */
    Optional<ExternalStorageProvider> create(StorageChannelType channelType);
}
