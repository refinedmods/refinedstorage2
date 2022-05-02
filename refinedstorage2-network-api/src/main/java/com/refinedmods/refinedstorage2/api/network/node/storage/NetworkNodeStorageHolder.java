package com.refinedmods.refinedstorage2.api.network.node.storage;

import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.limited.LimitedStorage;

class NetworkNodeStorageHolder<T> {
    private final StorageNetworkNode<T> node;
    private NetworkNodeStorage<T> storage;
    private long capacity;

    public NetworkNodeStorageHolder(StorageNetworkNode<T> node) {
        this.node = node;
    }

    public void setStorage(Storage<T> storage) {
        this.storage = new NetworkNodeStorage<>(node, storage);
        this.capacity = storage instanceof LimitedStorage<T> limitedStorage ? limitedStorage.getCapacity() : 0L;
    }

    public Storage<T> getStorage() {
        return storage;
    }

    public long getStored() {
        return storage != null ? storage.getStored() : 0L;
    }

    public long getCapacity() {
        return capacity;
    }
}
