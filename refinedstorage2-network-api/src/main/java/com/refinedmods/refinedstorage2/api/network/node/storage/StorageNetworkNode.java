package com.refinedmods.refinedstorage2.api.network.node.storage;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.core.filter.Filter;
import com.refinedmods.refinedstorage2.api.core.filter.FilterMode;
import com.refinedmods.refinedstorage2.api.network.component.StorageNetworkComponent;
import com.refinedmods.refinedstorage2.api.network.node.NetworkNodeImpl;
import com.refinedmods.refinedstorage2.api.storage.AccessMode;
import com.refinedmods.refinedstorage2.api.storage.ProxyStorage;
import com.refinedmods.refinedstorage2.api.storage.Source;
import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.StorageProvider;
import com.refinedmods.refinedstorage2.api.storage.StorageRepository;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannel;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;
import com.refinedmods.refinedstorage2.api.storage.composite.Priority;
import com.refinedmods.refinedstorage2.api.storage.limited.LimitedStorage;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedResource;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedStorage;

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

    private int priority;
    private AccessMode accessMode = AccessMode.INSERT_EXTRACT;
    private NetworkNodeStorage storage;

    public StorageNetworkNode(long energyUsage, StorageChannelType<?> type) {
        this.energyUsage = energyUsage;
        this.type = type;
    }

    public void initializeExistingStorage(StorageRepository storageRepository, UUID storageId) {
        storageRepository.get(storageId).ifPresentOrElse(
                storage -> {
                    LOGGER.info("Loaded existing storage {}", storageId);
                    this.storage = new NetworkNodeStorage((Storage<T>) storage);
                },
                () -> LOGGER.warn("Storage {} was not found, ignoring", storageId)
        );
    }

    public void initializeNewStorage(StorageRepository storageRepository, Storage<T> storage, UUID storageId) {
        LOGGER.info("Loaded new storage {}", storageId);
        storageRepository.set(storageId, storage);
        this.storage = new NetworkNodeStorage(storage);
    }

    @Override
    public void onActiveChanged(boolean active) {
        super.onActiveChanged(active);
        if (network == null) {
            return;
        }
        LOGGER.info("Updating storage due to activeness change to {}", active);
        StorageChannel<?> storageChannel = network.getComponent(StorageNetworkComponent.class).getStorageChannel(type);
        if (active) {
            storageChannel.addSource(storage);
        } else {
            storageChannel.removeSource(storage);
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
        return storage != null ? storage.getStored() : 0L;
    }

    public long getCapacity() {
        return storage != null ? storage.getCapacity() : 0L;
    }

    private class NetworkNodeStorage extends ProxyStorage<T> implements TrackedStorage<T>, Priority {
        private final Storage<T> delegate;

        public NetworkNodeStorage(Storage<T> delegate) {
            super(delegate);
            this.delegate = delegate;
        }

        private long getCapacity() {
            return delegate instanceof LimitedStorage<?> limitedStorage ? limitedStorage.getCapacity() : 0L;
        }

        @Override
        public long extract(T resource, long amount, Action action, Source source) {
            if (accessMode == AccessMode.INSERT || !isActive()) {
                return 0;
            }
            return super.extract(resource, amount, action, source);
        }

        @Override
        public long insert(T resource, long amount, Action action, Source source) {
            if (accessMode == AccessMode.EXTRACT || !isActive() || !filter.isAllowed(resource)) {
                return 0;
            }
            return super.insert(resource, amount, action, source);
        }

        @Override
        public Optional<TrackedResource> findTrackedResourceBySourceType(T resource, Class<? extends Source> sourceType) {
            return delegate instanceof TrackedStorage<T> trackedStorage
                    ? trackedStorage.findTrackedResourceBySourceType(resource, sourceType)
                    : Optional.empty();
        }

        @Override
        public int getPriority() {
            return priority;
        }
    }
}
