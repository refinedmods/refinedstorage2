package com.refinedmods.refinedstorage2.api.storage;

import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;

public record TypedStorage<T>(Storage<T> storage, StorageChannelType<T> storageChannelType) {
}
