package com.refinedmods.refinedstorage2.api.network.component;

import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannel;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.1")
public interface StorageNetworkComponent extends NetworkComponent {
    <T> StorageChannel<T> getStorageChannel(StorageChannelType<T> type);
}
