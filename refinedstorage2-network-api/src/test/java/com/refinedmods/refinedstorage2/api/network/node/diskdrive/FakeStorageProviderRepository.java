package com.refinedmods.refinedstorage2.api.network.node.diskdrive;

import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.StorageInfo;
import com.refinedmods.refinedstorage2.api.storage.StorageRepository;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;
import com.refinedmods.refinedstorage2.network.test.NetworkTestFixtures;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class FakeStorageProviderRepository implements StorageDiskProvider, StorageRepository {
    private final Map<Integer, UUID> slots = new HashMap<>();
    private final Map<UUID, Storage<?>> storages = new HashMap<>();

    @Override
    public Optional<UUID> getDiskId(final int slot) {
        return Optional.ofNullable(slots.get(slot));
    }

    @Override
    public Optional<StorageChannelType<?>> getStorageChannelType(final int slot) {
        if (slots.containsKey(slot)) {
            return Optional.of(NetworkTestFixtures.STORAGE_CHANNEL_TYPE);
        }
        return Optional.empty();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<Storage<T>> get(final UUID id) {
        final Storage<?> disk = storages.get(id);
        return Optional.ofNullable(disk == null ? null : (Storage<T>) disk);
    }

    public <T> void setInSlot(final int slot, final Storage<T> storage) {
        final UUID id = UUID.randomUUID();
        storages.put(id, storage);
        setInSlot(slot, id);
    }

    public void setInSlot(final int slot, final UUID id) {
        slots.put(slot, id);
    }

    public void removeInSlot(final int slot) {
        slots.remove(slot);
    }

    @Override
    public <T> void set(final UUID id, final Storage<T> storage) {
        storages.put(id, storage);
    }

    @Override
    public <T> Optional<Storage<T>> disassemble(final UUID id) {
        return Optional.empty();
    }

    @Override
    public StorageInfo getInfo(final UUID id) {
        return StorageInfo.UNKNOWN;
    }
}
