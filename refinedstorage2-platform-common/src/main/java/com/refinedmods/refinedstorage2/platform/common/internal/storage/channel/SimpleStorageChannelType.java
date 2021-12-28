package com.refinedmods.refinedstorage2.platform.common.internal.storage.channel;

import com.refinedmods.refinedstorage2.api.resource.list.ResourceListImpl;
import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannel;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelImpl;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageTracker;
import com.refinedmods.refinedstorage2.api.storage.composite.CompositeStorage;

import java.util.Collections;
import java.util.List;

public class SimpleStorageChannelType<T> implements StorageChannelType<T> {
    private final String name;

    public SimpleStorageChannelType(String name) {
        this.name = name;
    }

    @Override
    public StorageChannel<T> create() {
        return new StorageChannelImpl<>(
                createCompositeStorage(Collections.emptyList()),
                ResourceListImpl::new,
                new StorageTracker<>(System::currentTimeMillis)
        );
    }

    @Override
    public CompositeStorage<T> createCompositeStorage(List<Storage<T>> sources) {
        return new CompositeStorage<>(sources, new ResourceListImpl<>());
    }

    @Override
    public String toString() {
        return "SimpleStorageChannelType{" +
                "name='" + name + '\'' +
                '}';
    }
}
