package com.refinedmods.refinedstorage2.platform.apiimpl.storage.channel;

import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannel;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;

import com.google.common.base.Preconditions;

public class NamedStorageChannelType<T> implements StorageChannelType<T> {
    private final String name;
    private final StorageChannelType<T> delegate;

    public NamedStorageChannelType(final String name, final StorageChannelType<T> delegate) {
        Preconditions.checkNotNull(delegate);
        this.name = name;
        this.delegate = delegate;
    }

    @Override
    public StorageChannel<T> create() {
        return delegate.create();
    }

    @Override
    public String toString() {
        return "NamedStorageChannelType{" +
                "name='" + name + '\'' +
                '}';
    }
}
