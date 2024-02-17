package com.refinedmods.refinedstorage2.api.storage;

import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;

public record TypedStorage<T, S extends Storage<T>>(S storage, StorageChannelType<T> storageChannelType) {
}
