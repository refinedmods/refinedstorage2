package com.refinedmods.refinedstorage2.api.network.impl.node.externalstorage;

import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedStorageRepository;

@FunctionalInterface
public interface TrackedStorageRepositoryProvider {
    TrackedStorageRepository getRepository(StorageChannelType type);
}
