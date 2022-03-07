package com.refinedmods.refinedstorage2.api.network.test;

import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelImpl;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageTracker;

public final class StorageChannelTypes {
    public static final StorageChannelType<String> FAKE = () -> new StorageChannelImpl<>(new StorageTracker<>(System::currentTimeMillis));

    private StorageChannelTypes() {
    }
}
