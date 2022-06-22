package com.refinedmods.refinedstorage2.api.network.node.storage;

import com.refinedmods.refinedstorage2.api.core.filter.Filter;
import com.refinedmods.refinedstorage2.api.core.filter.FilterMode;
import com.refinedmods.refinedstorage2.api.network.component.StorageNetworkComponent;
import com.refinedmods.refinedstorage2.api.network.component.StorageProvider;
import com.refinedmods.refinedstorage2.api.network.node.NetworkNodeImpl;
import com.refinedmods.refinedstorage2.api.storage.AccessMode;
import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.StorageRepository;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.UnaryOperator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class StorageNetworkNode<T> extends NetworkNodeImpl implements StorageProvider {
    public static final Logger LOGGER = LogManager.getLogger();

    private final long energyUsage;
    private final Filter filter = new Filter();
    private final StorageChannelType<?> type;
    private final NetworkNodeStorage<T> exposedStorage = new NetworkNodeStorage<>(this);

    private int priority;
    private AccessMode accessMode = AccessMode.INSERT_EXTRACT;
    private Storage<T> internalStorage;

    public StorageNetworkNode(long energyUsage, StorageChannelType<?> type) {
        this.energyUsage = energyUsage;
        this.type = type;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public void initializeExistingStorage(StorageRepository storageRepository, UUID storageId) {
        storageRepository.get(storageId).ifPresentOrElse(
                existingStorage -> {
                    LOGGER.info("Loaded existing storage {}", storageId);
                    this.internalStorage = (Storage) existingStorage;
                },
                () -> LOGGER.warn("Storage {} was not found, ignoring", storageId)
        );
    }

    public void initializeNewStorage(StorageRepository storageRepository, Storage<T> newStorage, UUID storageId) {
        LOGGER.info("Loaded new storage {}", storageId);
        storageRepository.set(storageId, newStorage);
        this.internalStorage = newStorage;
    }

    @Override
    public void onActiveChanged(boolean active) {
        super.onActiveChanged(active);
        if (network == null || internalStorage == null) {
            return;
        }
        LOGGER.info("Storage activeness got changed to '{}', updating underlying storage", active);
        if (active) {
            exposedStorage.setSource(internalStorage);
        } else {
            exposedStorage.removeSource();
        }
    }

    @Override
    public long getEnergyUsage() {
        return energyUsage;
    }

    public int getPriority() {
        return priority;
    }

    public AccessMode getAccessMode() {
        return accessMode;
    }

    public void setAccessMode(AccessMode accessMode) {
        this.accessMode = accessMode;
    }

    public FilterMode getFilterMode() {
        return filter.getMode();
    }

    public boolean isAllowed(T resource) {
        return filter.isAllowed(resource);
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
        return exposedStorage.getStored();
    }

    public long getCapacity() {
        return exposedStorage.getCapacity();
    }

    @Override
    public <S> Optional<Storage<S>> getStorageForChannel(StorageChannelType<S> channelType) {
        if (channelType == this.type) {
            return Optional.of((Storage<S>) exposedStorage);
        }
        return Optional.empty();
    }
}
