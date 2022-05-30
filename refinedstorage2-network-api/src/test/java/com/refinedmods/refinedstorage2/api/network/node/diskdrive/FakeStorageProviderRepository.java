package com.refinedmods.refinedstorage2.api.network.node.diskdrive;

import com.refinedmods.refinedstorage2.api.network.test.NetworkTestFixtures;
import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.StorageInfo;
import com.refinedmods.refinedstorage2.api.storage.StorageRepository;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class FakeStorageProviderRepository implements StorageDiskProvider, StorageRepository {
    private final Map<Integer, UUID> slots = new HashMap<>();
    private final Map<UUID, Storage<?>> storages = new HashMap<>();

    @Override
    public Optional<UUID> getDiskId(int slot) {
        return Optional.ofNullable(slots.get(slot));
    }

    @Override
    public Optional<StorageChannelType<?>> getStorageChannelType(int slot) {
        if (slots.containsKey(slot)) {
            return Optional.of(NetworkTestFixtures.STORAGE_CHANNEL_TYPE);
        }
        return Optional.empty();
    }

    @Override
    public <T> Optional<Storage<T>> get(UUID id) {
        Storage<?> disk = storages.get(id);
        return Optional.ofNullable(disk == null ? null : (Storage<T>) disk);
    }

    public <T> void setInSlot(int slot, Storage<T> storage) {
        UUID id = UUID.randomUUID();
        storages.put(id, storage);
        setInSlot(slot, id);
    }

    public void setInSlot(int slot, UUID id) {
        slots.put(slot, id);
    }

    public void removeInSlot(int slot) {
        slots.remove(slot);
    }

    @Override
    public <T> void set(UUID id, Storage<T> storage) {
        storages.put(id, storage);
    }

    @Override
    public <T> Optional<Storage<T>> disassemble(UUID id) {
        return Optional.empty();
    }

    @Override
    public StorageInfo getInfo(UUID id) {
        return null;
    }
}
