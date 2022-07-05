package com.refinedmods.refinedstorage2.platform.apiimpl.storage.channel;

import com.refinedmods.refinedstorage2.api.core.CoreValidations;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannel;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;

public class NamedStorageChannelType<T> implements StorageChannelType<T> {
    private final String name;
    private final StorageChannelType<T> delegate;

    public NamedStorageChannelType(final String name, final StorageChannelType<T> delegate) {
        this.name = name;
        this.delegate = CoreValidations.validateNotNull(delegate, "Delegate cannot be null");
    }

    @Override
    public StorageChannel<T> create() {
        return delegate.create();
    }

    @Override
    public String toString() {
        return "NamedStorageChannelType{"
            + "name='" + name + '\''
            + '}';
    }
}
