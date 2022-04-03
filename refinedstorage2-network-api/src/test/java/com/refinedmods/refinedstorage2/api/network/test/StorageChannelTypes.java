package com.refinedmods.refinedstorage2.api.network.test;

import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelImpl;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;

public final class StorageChannelTypes {
    public static final StorageChannelType<String> FAKE = StorageChannelImpl::new;

    private StorageChannelTypes() {
    }
}
