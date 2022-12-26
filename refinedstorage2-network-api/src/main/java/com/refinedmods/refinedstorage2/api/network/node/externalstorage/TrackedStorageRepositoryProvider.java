package com.refinedmods.refinedstorage2.api.network.node.externalstorage;

import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedStorageRepository;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.2.5")
@FunctionalInterface
public interface TrackedStorageRepositoryProvider {
    <T> TrackedStorageRepository<T> getRepository(StorageChannelType<T> type);
}
