package com.refinedmods.refinedstorage2.platform.common.storage.portablegrid;

import com.refinedmods.refinedstorage2.api.storage.StateTrackedStorage;
import com.refinedmods.refinedstorage2.api.storage.StorageState;
import com.refinedmods.refinedstorage2.api.storage.TypedStorage;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannel;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelImpl;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;

class PortableGridStorage {
    private final StorageChannel storageChannel;
    private final TypedStorage<StateTrackedStorage> diskStorage;

    PortableGridStorage(final TypedStorage<StateTrackedStorage> diskStorage) {
        this.storageChannel = new StorageChannelImpl();
        this.diskStorage = diskStorage;
        this.storageChannel.addSource(diskStorage.storage());
    }

    StorageChannelType getStorageChannelType() {
        return diskStorage.storageChannelType();
    }

    StorageState getState() {
        return diskStorage.storage().getState();
    }

    StorageChannel getStorageChannel() {
        return storageChannel;
    }
}
