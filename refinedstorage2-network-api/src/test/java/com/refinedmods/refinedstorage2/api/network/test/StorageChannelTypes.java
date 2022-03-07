package com.refinedmods.refinedstorage2.api.network.test;

import com.refinedmods.refinedstorage2.api.resource.list.ResourceListImpl;
import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannel;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelImpl;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageTracker;
import com.refinedmods.refinedstorage2.api.storage.composite.CompositeStorage;
import com.refinedmods.refinedstorage2.api.storage.composite.CompositeStorageImpl;

import java.util.Collections;
import java.util.List;

public final class StorageChannelTypes {
    public static final StorageChannelType<String> FAKE = new StorageChannelType<>() {
        @Override
        public StorageChannel<String> create() {
            return new StorageChannelImpl<>(
                    createCompositeStorage(Collections.emptyList()),
                    ResourceListImpl::new,
                    new StorageTracker<>(System::currentTimeMillis)
            );
        }

        @Override
        public CompositeStorage<String> createCompositeStorage(List<Storage<String>> sources) {
            return new CompositeStorageImpl<>(sources, new ResourceListImpl<>());
        }
    };

    private StorageChannelTypes() {
    }
}
