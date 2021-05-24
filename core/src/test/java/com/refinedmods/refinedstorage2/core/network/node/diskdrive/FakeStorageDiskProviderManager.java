package com.refinedmods.refinedstorage2.core.network.node.diskdrive;

import com.refinedmods.refinedstorage2.core.storage.disk.StorageDisk;
import com.refinedmods.refinedstorage2.core.storage.disk.StorageDiskInfo;
import com.refinedmods.refinedstorage2.core.storage.disk.StorageDiskManager;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class FakeStorageDiskProviderManager implements StorageDiskProvider, StorageDiskManager {
    private final Map<Integer, UUID> slots = new HashMap<>();
    private final Map<UUID, StorageDisk<?>> disks = new HashMap<>();
    private DiskDriveNetworkNode diskDrive;

    public void setDiskDrive(DiskDriveNetworkNode diskDrive) {
        this.diskDrive = diskDrive;
    }

    @Override
    public Optional<UUID> getDiskId(int slot) {
        return Optional.ofNullable(slots.get(slot));
    }

    @Override
    public <T> Optional<StorageDisk<T>> getDisk(UUID id) {
        StorageDisk<?> disk = disks.get(id);
        return Optional.ofNullable(disk == null ? null : (StorageDisk<T>) disk);
    }

    public <T> void setDisk(int slot, StorageDisk<T> disk) {
        UUID id = UUID.randomUUID();
        disks.put(id, disk);
        slots.put(slot, id);
        diskDrive.onDiskChanged(slot);
    }

    public void setDisk(int slot, UUID id) {
        slots.put(slot, id);
        diskDrive.onDiskChanged(slot);
    }

    public void removeDiskInSlot(int slot) {
        slots.remove(slot);
        diskDrive.onDiskChanged(slot);
    }

    @Override
    public <T> void setDisk(UUID id, StorageDisk<T> disk) {

    }

    @Override
    public <T> Optional<StorageDisk<T>> disassembleDisk(UUID id) {
        return Optional.empty();
    }

    @Override
    public StorageDiskInfo getInfo(UUID id) {
        return null;
    }
}
