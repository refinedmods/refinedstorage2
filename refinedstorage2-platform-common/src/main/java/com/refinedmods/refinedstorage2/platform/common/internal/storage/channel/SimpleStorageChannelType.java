package com.refinedmods.refinedstorage2.platform.common.internal.storage.channel;

import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannel;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelImpl;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;

public class SimpleStorageChannelType<T> implements StorageChannelType<T> {
    private final String name;

    public SimpleStorageChannelType(String name) {
        this.name = name;
    }

    @Override
    public StorageChannel<T> create() {
        return new StorageChannelImpl<>();
    }

    @Override
    public String toString() {
        return "SimpleStorageChannelType{" +
                "name='" + name + '\'' +
                '}';
    }
}
