package com.refinedmods.refinedstorage2.api.grid.watcher;

import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannel;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;

import java.util.Set;

import org.apiguardian.api.API;

/**
 * Provides the {@link GridWatcherManagerImpl} with {@link StorageChannel}s.
 */
@API(status = API.Status.STABLE, since = "2.0.0-milestone.3.3")
public interface GridStorageChannelProvider {
    Set<StorageChannelType<?>> getStorageChannelTypes();

    <T> StorageChannel<T> getStorageChannel(StorageChannelType<T> type);
}
