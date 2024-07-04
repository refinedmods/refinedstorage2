package com.refinedmods.refinedstorage.api.network.impl.node.storage;

import com.refinedmods.refinedstorage.api.network.impl.node.AbstractStorageContainerNetworkNode;
import com.refinedmods.refinedstorage.api.network.impl.storage.NetworkNodeStorageConfiguration;
import com.refinedmods.refinedstorage.api.network.impl.storage.StorageConfiguration;
import com.refinedmods.refinedstorage.api.network.storage.StorageProvider;
import com.refinedmods.refinedstorage.api.storage.StateTrackedStorage;
import com.refinedmods.refinedstorage.api.storage.Storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StorageNetworkNode extends AbstractStorageContainerNetworkNode implements StorageProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(StorageNetworkNode.class);

    private final StorageConfiguration storageConfiguration;
    private final ExposedStorage storage;

    public StorageNetworkNode(final long energyUsage, final long energyUsagePerStorage, final int size) {
        super(energyUsage, energyUsagePerStorage, size);
        this.storageConfiguration = new NetworkNodeStorageConfiguration(this);
        this.storage = new ExposedStorage(storageConfiguration);
    }

    @Override
    protected void onStorageChange(final AbstractStorageContainerNetworkNode.StorageChange change) {
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
        return storage.getStored();
    }

    public long getCapacity() {
        return storage.getCapacity();
    }

    @Override
    public Storage getStorage() {
        return storage;
    }
}
