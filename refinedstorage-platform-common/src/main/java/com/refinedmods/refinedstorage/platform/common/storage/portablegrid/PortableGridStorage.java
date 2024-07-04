package com.refinedmods.refinedstorage.platform.common.storage.portablegrid;

import com.refinedmods.refinedstorage.api.storage.StateTrackedStorage;
import com.refinedmods.refinedstorage.api.storage.StorageState;
import com.refinedmods.refinedstorage.api.storage.channel.StorageChannel;
import com.refinedmods.refinedstorage.api.storage.channel.StorageChannelImpl;

class PortableGridStorage {
    private final StorageChannel storageChannel;
    private final StateTrackedStorage diskStorage;

    PortableGridStorage(final StateTrackedStorage diskStorage) {
        this.storageChannel = new StorageChannelImpl();
        this.diskStorage = diskStorage;
        this.storageChannel.addSource(diskStorage);
    }

    StorageState getState() {
        return diskStorage.getState();
    }

    StorageChannel getStorageChannel() {
        return storageChannel;
    }
}
