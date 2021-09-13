package com.refinedmods.refinedstorage2.api.network.node.diskdrive;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.stack.Rs2Stack;
import com.refinedmods.refinedstorage2.api.storage.AccessMode;
import com.refinedmods.refinedstorage2.api.storage.ProxyStorage;
import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;
import com.refinedmods.refinedstorage2.api.storage.composite.Priority;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class DiskDriveStorage<T extends Rs2Stack> extends ProxyStorage<T> implements Priority {
    protected final DiskDriveNetworkNode diskDrive;
    private final StorageChannelType<T> storageChannelType;

    protected DiskDriveStorage(DiskDriveNetworkNode diskDrive, StorageChannelType<T> type) {
        super(type.createEmptyCompositeStorage());
        this.diskDrive = diskDrive;
        this.storageChannelType = type;
    }

    @Override
    public Optional<T> extract(T resource, long amount, Action action) {
        if (diskDrive.getAccessMode() == AccessMode.INSERT || !diskDrive.isActive()) {
            return Optional.empty();
        }
        return super.extract(resource, amount, action);
    }

    @Override
    public Optional<T> insert(T resource, long amount, Action action) {
        if (diskDrive.getAccessMode() == AccessMode.EXTRACT || !diskDrive.isActive()) {
            return notAllowed(resource, amount);
        }
        return super.insert(resource, amount, action);
    }

    protected Optional<T> notAllowed(T template, long remainderAmount) {
        T remainder = (T) template.copy();
        remainder.setAmount(remainderAmount);
        return Optional.of(remainder);
    }

    @Override
    public Collection<T> getAll() {
        if (!diskDrive.isActive()) {
            return Collections.emptyList();
        }
        return super.getAll();
    }

    @Override
    public int getPriority() {
        return diskDrive.getPriority();
    }

    public void setSources(List<Storage<T>> sources) {
        this.parent = storageChannelType.createCompositeStorage(sources);
    }
}
