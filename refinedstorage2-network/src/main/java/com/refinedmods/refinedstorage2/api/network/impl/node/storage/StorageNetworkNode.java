package com.refinedmods.refinedstorage2.api.network.impl.node.storage;

import com.refinedmods.refinedstorage2.api.network.component.StorageProvider;
import com.refinedmods.refinedstorage2.api.network.node.AbstractStorageNetworkNode;
import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;

import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StorageNetworkNode extends AbstractStorageNetworkNode implements StorageProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(StorageNetworkNode.class);

    private final long energyUsage;
    private final StorageChannelType type;
    private final ExposedStorage exposedStorage = new ExposedStorage(this);

    @Nullable
    private Storage internalStorage;

    public StorageNetworkNode(final long energyUsage, final StorageChannelType type) {
        this.energyUsage = energyUsage;
        this.type = type;
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
            exposedStorage.setDelegate(internalStorage);
        } else {
            exposedStorage.clearDelegate();
        }
    }

    @Override
    public long getEnergyUsage() {
        return energyUsage;
    }

    public long getStored() {
        return exposedStorage.getStored();
    }

    public long getCapacity() {
        return exposedStorage.getCapacity();
    }

    @Override
    protected Set<StorageChannelType> getRelevantStorageChannelTypes() {
        return Set.of(type);
    }

    @Override
    public Optional<Storage> getStorageForChannel(final StorageChannelType channelType) {
        if (channelType == this.type) {
            return Optional.of(exposedStorage);
        }
        return Optional.empty();
    }
}
