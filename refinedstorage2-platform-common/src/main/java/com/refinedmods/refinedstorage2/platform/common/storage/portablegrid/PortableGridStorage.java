package com.refinedmods.refinedstorage2.platform.common.storage.portablegrid;

import com.refinedmods.refinedstorage2.api.storage.StateTrackedStorage;
import com.refinedmods.refinedstorage2.api.storage.StorageState;
import com.refinedmods.refinedstorage2.api.storage.TypedStorage;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannel;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelImpl;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;

class PortableGridStorage<T> {
    private final StorageChannel<T> storageChannel;
    private final TypedStorage<T, StateTrackedStorage<T>> diskStorage;

    PortableGridStorage(final TypedStorage<T, StateTrackedStorage<T>> diskStorage) {
        this.storageChannel = new StorageChannelImpl<>();
        this.diskStorage = diskStorage;
        this.storageChannel.addSource(diskStorage.storage());
    }

    StorageChannelType<T> getStorageChannelType() {
        return diskStorage.storageChannelType();
    }

    StorageState getState() {
        return diskStorage.storage().getState();
    }

    StorageChannel<T> getStorageChannel() {
        return storageChannel;
    }
}
