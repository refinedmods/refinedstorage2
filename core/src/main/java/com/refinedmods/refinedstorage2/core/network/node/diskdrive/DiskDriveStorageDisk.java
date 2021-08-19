package com.refinedmods.refinedstorage2.core.network.node.diskdrive;

import com.refinedmods.refinedstorage2.core.stack.Rs2Stack;
import com.refinedmods.refinedstorage2.core.storage.channel.StorageChannelType;
import com.refinedmods.refinedstorage2.core.storage.disk.DiskState;
import com.refinedmods.refinedstorage2.core.storage.disk.StorageDisk;
import com.refinedmods.refinedstorage2.core.util.Action;

import java.util.Collection;
import java.util.Optional;

public class DiskDriveStorageDisk<T extends Rs2Stack> implements StorageDisk<T> {
    private static final double DISK_NEAR_CAPACITY_THRESHOLD = .75;

    private final StorageDisk<T> parent;
    private final StorageChannelType<T> storageChannelType;
    private final DiskDriveListener listener;
    private DiskState state;

    public DiskDriveStorageDisk(StorageDisk<T> parent, StorageChannelType<T> storageChannelType, DiskDriveListener listener) {
        this.parent = parent;
        this.storageChannelType = storageChannelType;
        this.listener = listener;
        this.state = getState();
    }

    public StorageChannelType<T> getStorageChannelType() {
        return storageChannelType;
    }

    public DiskState getState() {
        double fullness = (double) parent.getStored() / (double) parent.getCapacity();

        if (fullness >= 1D) {
            return DiskState.FULL;
        } else if (fullness >= DISK_NEAR_CAPACITY_THRESHOLD) {
            return DiskState.NEAR_CAPACITY;
        } else {
            return DiskState.NORMAL;
        }
    }

    private void checkStateChanged() {
        DiskState currentDiskState = getState();
        if (state != currentDiskState) {
            this.state = currentDiskState;
            this.listener.onDiskChanged();
        }
    }

    @Override
    public Optional<T> extract(T template, long amount, Action action) {
        Optional<T> extracted = parent.extract(template, amount, action);
        if (action == Action.EXECUTE && extracted.isPresent()) {
            checkStateChanged();
        }
        return extracted;
    }

    @Override
    public Optional<T> insert(T template, long amount, Action action) {
        Optional<T> remainder = parent.insert(template, amount, action);
        if (action == Action.EXECUTE && (remainder.isEmpty() || remainder.get().getAmount() != amount)) {
            checkStateChanged();
        }
        return remainder;
    }

    @Override
    public Collection<T> getStacks() {
        return parent.getStacks();
    }

    @Override
    public long getStored() {
        return parent.getStored();
    }

    @Override
    public long getCapacity() {
        return parent.getCapacity();
    }
}
