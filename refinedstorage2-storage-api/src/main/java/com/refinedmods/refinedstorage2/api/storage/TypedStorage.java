package com.refinedmods.refinedstorage2.api.storage;

import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;

public record TypedStorage<S extends Storage>(S storage, StorageChannelType storageChannelType) {
}
