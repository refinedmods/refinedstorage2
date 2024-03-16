package com.refinedmods.refinedstorage2.api.network.impl.node.storage;

import com.refinedmods.refinedstorage2.api.network.component.StorageProvider;
import com.refinedmods.refinedstorage2.api.network.impl.storage.AbstractStorageNetworkNode;
import com.refinedmods.refinedstorage2.api.storage.Storage;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StorageNetworkNode extends AbstractStorageNetworkNode implements StorageProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(StorageNetworkNode.class);

    private final long energyUsage;
    private final ExposedStorage storage = new ExposedStorage(this);
    @Nullable
    private Storage internalStorage;

    public StorageNetworkNode(final long energyUsage) {
        this.energyUsage = energyUsage;
    }

    public void setStorage(final Storage storage) {
        LOGGER.debug("Loading storage {}", storage);
        this.internalStorage = storage;
    }

    @Override
    protected void onActiveChanged(final boolean newActive) {
        super.onActiveChanged(newActive);
        if (network == null || internalStorage == null) {
            return;
        }
        LOGGER.debug("Storage activeness got changed to '{}', updating underlying storage", newActive);
        if (newActive) {
            storage.setDelegate(internalStorage);
        } else {
            storage.clearDelegate();
        }
    }

    @Override
    public long getEnergyUsage() {
        return energyUsage;
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
