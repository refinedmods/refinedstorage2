package com.refinedmods.refinedstorage2.api.network.impl.node.multistorage;

import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.TypedStorage;
import com.refinedmods.refinedstorage2.network.test.NetworkTestFixtures;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

// TODO: make pkg private
public class MultiStorageProviderImpl implements MultiStorageProvider {
    private final Map<Integer, Storage<String>> storages = new HashMap<>();

    @Override
    public Optional<TypedStorage<?>> resolve(final int index) {
        return Optional.ofNullable(storages.get(index)).map(storage -> new TypedStorage<>(
            storage,
            NetworkTestFixtures.STORAGE_CHANNEL_TYPE
        ));
    }

    public void set(final int index, final Storage<String> storage) {
        storages.put(index, storage);
    }

    public void remove(final int index) {
        storages.remove(index);
    }
}
