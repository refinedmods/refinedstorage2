package com.refinedmods.refinedstorage2.core.network.node.diskdrive;

import com.refinedmods.refinedstorage2.core.stack.item.Rs2ItemStack;
import com.refinedmods.refinedstorage2.core.storage.disk.DiskState;
import com.refinedmods.refinedstorage2.core.storage.disk.StorageDisk;
import com.refinedmods.refinedstorage2.core.util.Action;

import java.util.Collection;
import java.util.Optional;

public class DiskDriveItemStorageDisk implements StorageDisk<Rs2ItemStack> {
    private static final double DISK_NEAR_CAPACITY_THRESHOLD = .75;

    private final StorageDisk<Rs2ItemStack> parent;
    private final DiskDriveListener listener;
    private DiskState state;

    public DiskDriveItemStorageDisk(StorageDisk<Rs2ItemStack> parent, DiskDriveListener listener) {
        this.parent = parent;
        this.listener = listener;
        this.state = getState();
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
    public Optional<Rs2ItemStack> extract(Rs2ItemStack template, long amount, Action action) {
        Optional<Rs2ItemStack> extracted = parent.extract(template, amount, action);
        if (action == Action.EXECUTE && extracted.isPresent()) {
            checkStateChanged();
        }
        return extracted;
    }

    @Override
    public Optional<Rs2ItemStack> insert(Rs2ItemStack template, long amount, Action action) {
        Optional<Rs2ItemStack> remainder = parent.insert(template, amount, action);
        if (action == Action.EXECUTE && (remainder.isEmpty() || remainder.get().getAmount() != amount)) {
            checkStateChanged();
        }
        return remainder;
    }

    @Override
    public Collection<Rs2ItemStack> getStacks() {
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
