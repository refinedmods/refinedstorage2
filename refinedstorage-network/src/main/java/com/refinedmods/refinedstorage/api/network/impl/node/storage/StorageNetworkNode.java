package com.refinedmods.refinedstorage.api.network.impl.node.storage;

import com.refinedmods.refinedstorage.api.network.impl.node.AbstractStorageContainerNetworkNode;
import com.refinedmods.refinedstorage.api.network.impl.storage.NetworkNodeStorageConfiguration;
import com.refinedmods.refinedstorage.api.network.impl.storage.StorageConfiguration;
import com.refinedmods.refinedstorage.api.network.node.NetworkNodeType;
import com.refinedmods.refinedstorage.api.network.node.StorageNetworkNodeDetailsProvider;
import com.refinedmods.refinedstorage.api.network.storage.StorageProvider;
import com.refinedmods.refinedstorage.api.storage.StateTrackedStorage;
import com.refinedmods.refinedstorage.api.storage.Storage;
import com.refinedmods.refinedstorage.api.storage.composite.PriorityProvider;
import com.refinedmods.refinedstorage.api.storage.limited.LimitedStorage;

import java.util.function.Predicate;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StorageNetworkNode extends AbstractStorageContainerNetworkNode
    implements StorageProvider, StorageNetworkNodeDetailsProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(StorageNetworkNode.class);

    private final StorageConfiguration storageConfiguration;
    private final ExposedStorage storage;

    public StorageNetworkNode(final NetworkNodeType type,
                              final long baseEnergyUsage, final long energyUsagePerStorage, final int size) {
        super(type, baseEnergyUsage, energyUsagePerStorage, size);
        this.storageConfiguration = new NetworkNodeStorageConfiguration(this);
        this.storage = new ExposedStorage(storageConfiguration);
    }

    @Override
    protected void onStorageChange(final AbstractStorageContainerNetworkNode.StorageChange change) {
        super.onStorageChange(change);
        if (!isActive()) {
            return;
        }
        if (change.removed()) {
            storage.removeSource(change.storage());
        } else {
            storage.addSource(change.storage());
        }
    }

    @Override
    protected void onActiveChanged(final boolean newActive) {
        super.onActiveChanged(newActive);
        if (network == null) {
            return;
        }
        LOGGER.debug("Activeness got changed to {}, updating underlying internal storages", newActive);
        if (newActive) {
            enableAllStorages();
        } else {
            disableAllStorages();
        }
    }

    private void enableAllStorages() {
        for (final StateTrackedStorage internalStorage : storages) {
            if (internalStorage != null) {
                storage.addSource(internalStorage);
            }
        }
    }

    private void disableAllStorages() {
        storage.clearSources();
    }

    public StorageConfiguration getStorageConfiguration() {
        return storageConfiguration;
    }

    public long getStored() {
        long stored = 0;
        for (final StateTrackedStorage internalStorage : storages) {
            if (internalStorage != null) {
                stored += internalStorage.getStored();
            }
        }
        return stored;
    }

    @Override
    public long getStored(final Predicate<Storage> storageFilter) {
        long stored = 0;
        for (final StateTrackedStorage internalStorage : storages) {
            if (internalStorage == null) {
                continue;
            }
            final Storage delegate = internalStorage.getDelegate();
            if (delegate instanceof LimitedStorage limitedStorage && storageFilter.test(delegate)) {
                stored += limitedStorage.getStored();
            }
        }
        return stored;
    }

    public long getCapacity() {
        long capacity = 0;
        for (final StateTrackedStorage internalStorage : storages) {
            if (internalStorage != null) {
                capacity += internalStorage.getCapacity();
            }
        }
        return capacity;
    }


    @Override
    public long getCapacity(final Predicate<Storage> storageFilter) {
        long capacity = 0;
        for (final StateTrackedStorage internalStorage : storages) {
            if (internalStorage == null) {
                continue;
            }
            final Storage delegate = internalStorage.getDelegate();
            if (delegate instanceof LimitedStorage limitedStorage && storageFilter.test(delegate)) {
                capacity += limitedStorage.getCapacity();
            }
        }
        return capacity;
    }

    @Override
    public Storage getStorage() {
        return storage;
    }

    @Override
    @Nullable
    public PriorityProvider getPriority() {
        return storage;
    }
}
