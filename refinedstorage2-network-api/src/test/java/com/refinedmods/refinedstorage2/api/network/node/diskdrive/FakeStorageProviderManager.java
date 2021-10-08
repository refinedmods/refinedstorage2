package com.refinedmods.refinedstorage2.api.network.node.diskdrive;

import com.refinedmods.refinedstorage2.api.storage.StorageInfo;
import com.refinedmods.refinedstorage2.api.storage.StorageManager;
import com.refinedmods.refinedstorage2.api.storage.bulk.BulkStorage;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelTypes;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class FakeStorageProviderManager implements StorageDiskProvider, StorageManager {
    private final Map<Integer, UUID> slots = new HashMap<>();
    private final Map<UUID, BulkStorage<?>> disks = new HashMap<>();

    @Override
    public Optional<UUID> getDiskId(int slot) {
        return Optional.ofNullable(slots.get(slot));
    }

    @Override
    public Optional<StorageChannelType<?>> getStorageChannelType(int slot) {
        if (slots.containsKey(slot)) {
            return Optional.of(StorageChannelTypes.ITEM);
        }
        return Optional.empty();
    }

    @Override
    public <T> Optional<BulkStorage<T>> get(UUID id) {
        BulkStorage<?> disk = disks.get(id);
        return Optional.ofNullable(disk == null ? null : (BulkStorage<T>) disk);
    }

    public <T> void setDiskInSlot(int slot, BulkStorage<T> disk) {
        UUID id = UUID.randomUUID();
        disks.put(id, disk);
        setDiskInSlot(slot, id);
    }

    public void setDiskInSlot(int slot, UUID id) {
        slots.put(slot, id);
    }

    public void removeDiskInSlot(int slot) {
        slots.remove(slot);
    }

    @Override
    public <T> void set(UUID id, BulkStorage<T> storage) {
        throw new RuntimeException();
    }

    @Override
    public <T> Optional<BulkStorage<T>> disassemble(UUID id) {
        return Optional.empty();
    }

    @Override
    public StorageInfo getInfo(UUID id) {
        return null;
    }
}
