package com.refinedmods.refinedstorage2.api.storage;

import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;

import java.util.Optional;

import org.apiguardian.api.API;

/**
 * Implement this on classes that can provide a storage for a given storage channel.
 */
@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.2")
public interface StorageSource {
    /**
     * @param channelType the storage channel type
     * @param <T>         the type of resource
     * @return the storage for the given channel, if present
     */
    <T> Optional<Storage<T>> getStorageForChannel(StorageChannelType<T> channelType);
}
