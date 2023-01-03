package com.refinedmods.refinedstorage2.api.network.impl.node.diskdrive;

import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.TypedStorage;
import com.refinedmods.refinedstorage2.network.test.NetworkTestFixtures;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

// TODO: make pkg private
public class StorageDiskProviderImpl implements StorageDiskProvider {
    private final Map<Integer, Storage<String>> storages = new HashMap<>();

    @Override
    public Optional<TypedStorage<?>> resolve(final int slot) {
        return Optional.ofNullable(storages.get(slot)).map(storage -> new TypedStorage<>(
            storage,
            NetworkTestFixtures.STORAGE_CHANNEL_TYPE
        ));
    }

    public void setInSlot(final int slot, final Storage<String> storage) {
        storages.put(slot, storage);
    }

    public void removeInSlot(final int slot) {
        storages.remove(slot);
    }
}
