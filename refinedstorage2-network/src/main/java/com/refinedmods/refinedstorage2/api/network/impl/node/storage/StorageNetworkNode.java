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

public class StorageNetworkNode<T> extends AbstractStorageNetworkNode implements StorageProvider {
    public static final Logger LOGGER = LoggerFactory.getLogger(StorageNetworkNode.class);

    private final long energyUsage;
    private final StorageChannelType<?> type;
    private final ExposedStorage<T> exposedStorage = new ExposedStorage<>(this);

    @Nullable
    private Storage<T> internalStorage;

    public StorageNetworkNode(final long energyUsage, final StorageChannelType<T> type) {
        this.energyUsage = energyUsage;
        this.type = type;
    }

    public void setStorage(final Storage<T> storage) {
        LOGGER.info("Loading storage {}", storage);
        this.internalStorage = storage;
    }

    @Override
    protected void onActiveChanged(final boolean newActive) {
        super.onActiveChanged(newActive);
        if (network == null || internalStorage == null) {
            return;
        }
        LOGGER.info("Storage activeness got changed to '{}', updating underlying storage", newActive);
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
    protected Set<StorageChannelType<?>> getRelevantStorageChannelTypes() {
        return Set.of(type);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <S> Optional<Storage<S>> getStorageForChannel(final StorageChannelType<S> channelType) {
        if (channelType == this.type) {
            return Optional.of((Storage<S>) exposedStorage);
        }
        return Optional.empty();
    }
}
