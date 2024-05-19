package com.refinedmods.refinedstorage2.api.network.impl.node.storage;

import com.refinedmods.refinedstorage2.api.storage.Storage;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

class StorageNetworkNodeProviderImpl implements StorageNetworkNode.Provider {
    private final Map<Integer, Storage> storages = new HashMap<>();

    @Override
    public Optional<Storage> resolve(final int index) {
        return Optional.ofNullable(storages.get(index));
    }

    public void set(final int index, final Storage storage) {
        storages.put(index, storage);
    }

    public void remove(final int index) {
        storages.remove(index);
    }
}
