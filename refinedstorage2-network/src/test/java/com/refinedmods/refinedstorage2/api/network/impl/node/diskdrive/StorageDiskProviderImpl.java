package com.refinedmods.refinedstorage2.api.network.impl.node.diskdrive;

import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;
import com.refinedmods.refinedstorage2.network.test.NetworkTestFixtures;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

// TODO: make pkg private.
public class StorageDiskProviderImpl implements StorageDiskProvider {
    private final Map<Integer, Storage<?>> storages = new HashMap<>();

    @Override
    public Optional<Storage<?>> resolve(final int slot) {
        return Optional.ofNullable(storages.get(slot));
    }

    @Override
    public Optional<StorageChannelType<?>> getStorageChannelType(final int slot) {
        if (storages.containsKey(slot)) {
            return Optional.of(NetworkTestFixtures.STORAGE_CHANNEL_TYPE);
        }
        return Optional.empty();
    }

    public <T> void setInSlot(final int slot, final Storage<T> storage) {
        storages.put(slot, storage);
    }

    public void removeInSlot(final int slot) {
        storages.remove(slot);
    }
}
