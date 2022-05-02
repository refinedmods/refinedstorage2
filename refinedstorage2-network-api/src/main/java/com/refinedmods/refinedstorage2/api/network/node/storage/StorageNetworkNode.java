package com.refinedmods.refinedstorage2.api.network.node.storage;

import com.refinedmods.refinedstorage2.api.core.filter.Filter;
import com.refinedmods.refinedstorage2.api.core.filter.FilterMode;
import com.refinedmods.refinedstorage2.api.network.component.StorageNetworkComponent;
import com.refinedmods.refinedstorage2.api.network.node.NetworkNodeImpl;
import com.refinedmods.refinedstorage2.api.storage.AccessMode;
import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.StorageProvider;
import com.refinedmods.refinedstorage2.api.storage.StorageRepository;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;
import com.refinedmods.refinedstorage2.api.storage.limited.LimitedStorage;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.UnaryOperator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// TODO: Add test
public class StorageNetworkNode<T> extends NetworkNodeImpl implements StorageProvider {
    public static final Logger LOGGER = LogManager.getLogger();

    private final long energyUsage;
    private final Filter filter = new Filter();
    private final StorageChannelType<?> type;

    private long capacity;
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
                    setStorage((Storage<T>) storage);
                },
                () -> LOGGER.warn("Storage {} was not found, ignoring", storageId)
        );
    }

    public void initializeNewStorage(StorageRepository storageRepository, Storage<T> storage, UUID storageId) {
        LOGGER.info("Loaded new storage {}", storageId);
        storageRepository.set(storageId, storage);
        setStorage(storage);
    }

    private void setStorage(Storage<T> storage) {
        this.storage = new NetworkNodeStorage<>(this, storage);
        this.capacity = storage instanceof LimitedStorage<T> limitedStorage ? limitedStorage.getCapacity() : 0;
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

    public void setAccessMode(AccessMode accessMode) {
        this.accessMode = accessMode;
    }

    @Override
    public <T> Optional<Storage<T>> getStorageForChannel(StorageChannelType<T> channelType) {
        return channelType == this.type ? Optional.ofNullable((Storage<T>) storage) : Optional.empty();
    }

    public FilterMode getFilterMode() {
        return filter.getMode();
    }

    public void setFilterMode(FilterMode mode) {
        filter.setMode(mode);
    }

    public void setPriority(int priority) {
        this.priority = priority;
        if (network != null) {
            network.getComponent(StorageNetworkComponent.class).getStorageChannel(type).sortSources();
        }
    }

    public void setFilterTemplates(Set<Object> templates) {
        filter.setTemplates(templates);
    }

    public void setNormalizer(UnaryOperator<Object> normalizer) {
        filter.setNormalizer(normalizer);
    }

    public long getStored() {
        return storage != null ? storage.getStored() : 0;
    }

    public long getCapacity() {
        return capacity;
    }
}
