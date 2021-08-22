package com.refinedmods.refinedstorage2.api.storage.channel;

import java.util.Set;

public interface StorageChannelTypeRegistry {
    void addType(StorageChannelType<?> type);

    Set<StorageChannelType<?>> getTypes();
}
