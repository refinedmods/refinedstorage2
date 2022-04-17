package com.refinedmods.refinedstorage2.api.network.node.storage;

import com.refinedmods.refinedstorage2.api.core.filter.Filter;
import com.refinedmods.refinedstorage2.api.network.node.NetworkNodeImpl;
import com.refinedmods.refinedstorage2.api.storage.AccessMode;
import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.StorageProvider;
import com.refinedmods.refinedstorage2.api.storage.StorageRepository;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;

import java.util.Optional;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class StorageNetworkNode<T> extends NetworkNodeImpl implements StorageProvider {
    public static final Logger LOGGER = LogManager.getLogger();

    private final long energyUsage;
    private final Filter filter = new Filter();
    private final StorageChannelType<?> type;
    private NetworkNodeStorage<T> storage;

    private int priority;
    private AccessMode accessMode = AccessMode.INSERT_EXTRACT;

    public StorageNetworkNode(long energyUsage, StorageChannelType<?> type) {
        this.energyUsage = energyUsage;
        this.type = type;
    }

    public void initializeExistingStorage(StorageRepository storageRepository, UUID storageId) {
        storageRepository.get(storageId).ifPresentOrElse(
                storage -> {
                    LOGGER.info("Loaded existing storage {}", storageId);
                    this.storage = new NetworkNodeStorage<>(this, (Storage<T>) storage);
                },
                () -> LOGGER.warn("Storage {} was not found, ignoring", storageId)
        );
    }

    public void initializeNewStorage(StorageRepository storageRepository, Storage<T> storage, UUID storageId) {
        LOGGER.info("Loaded new storage {}", storageId);
        storageRepository.set(storageId, storage);
        this.storage = new NetworkNodeStorage<>(this, storage);
    }

    @Override
    public long getEnergyUsage() {
        return energyUsage;
    }

    public int getPriority() {
        return priority;
    }

    public boolean isAllowed(T resource) {
        return filter.isAllowed(resource);
    }

    public AccessMode getAccessMode() {
        return accessMode;
    }

    @Override
    public <T> Optional<Storage<T>> getStorageForChannel(StorageChannelType<T> channelType) {
        return channelType == this.type ? Optional.ofNullable((Storage<T>) storage) : Optional.empty();
    }
}
