package com.refinedmods.refinedstorage2.api.network.component;

import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;

import java.util.Optional;

import org.apiguardian.api.API;

/**
 * Implement this on {@link com.refinedmods.refinedstorage2.api.network.node.NetworkNode}s that can provide a storage
 * for a given {@link StorageChannelType}.
 * Never modify a {@link com.refinedmods.refinedstorage2.api.storage.channel.StorageChannel}
 * from a {@link com.refinedmods.refinedstorage2.api.network.node.NetworkNode} directly.
 * Use this interface to help you manage the lifecycle of your storage, to ensure that your storage is added or removed
 * in the right cases.
 * Use a {@link com.refinedmods.refinedstorage2.api.storage.composite.CompositeStorage} to conditionally enable/disable
 * a provided {@link Storage}.
 */
@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.2")
public interface StorageProvider {
    /**
     * Returns an optional storage for the given storage channel type.
     * This method is called when a {@link com.refinedmods.refinedstorage2.api.network.node.NetworkNode} is added or
     * removed from a network.
     *
     * @param channelType the storage channel type
     * @return the storage for the given channel, if present
     */
    Optional<Storage> getStorageForChannel(StorageChannelType channelType);
}
