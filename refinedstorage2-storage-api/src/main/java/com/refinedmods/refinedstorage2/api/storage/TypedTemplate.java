package com.refinedmods.refinedstorage2.api.storage;

import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;

public record TypedTemplate<T>(T template, StorageChannelType<T> storageChannelType) {
}
