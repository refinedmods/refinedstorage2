package com.refinedmods.refinedstorage2.api.network.node.storage;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.core.filter.Filter;
import com.refinedmods.refinedstorage2.api.core.filter.FilterMode;
import com.refinedmods.refinedstorage2.api.network.component.StorageNetworkComponent;
import com.refinedmods.refinedstorage2.api.network.component.StorageProvider;
import com.refinedmods.refinedstorage2.api.network.node.NetworkNodeImpl;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.resource.list.ResourceListImpl;
import com.refinedmods.refinedstorage2.api.storage.AccessMode;
import com.refinedmods.refinedstorage2.api.storage.ProxyStorage;
import com.refinedmods.refinedstorage2.api.storage.Source;
import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.StorageRepository;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;
import com.refinedmods.refinedstorage2.api.storage.composite.CompositeStorage;
import com.refinedmods.refinedstorage2.api.storage.composite.CompositeStorageImpl;
import com.refinedmods.refinedstorage2.api.storage.composite.CompositeStorageListener;
import com.refinedmods.refinedstorage2.api.storage.composite.Priority;
import com.refinedmods.refinedstorage2.api.storage.limited.LimitedStorage;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedResource;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedStorage;

import java.util.Collection;
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
    // In order to be able to "hide" the underlying storage when the activeness changes, we have to
    // expose a composite storage because such a storage can propagate updates to the parent composite.
    // We can't let this network node itself control the storage channel,
    // since that wouldn't work with network node removals or network merges/updates.
    private final ExposedNetworkNodeStorage exposedStorage = new ExposedNetworkNodeStorage();

    public StorageNetworkNode(long energyUsage, StorageChannelType<?> type) {
        this.energyUsage = energyUsage;
        this.type = type;
    }

    public void initializeExistingStorage(StorageRepository storageRepository, UUID storageId) {
        storageRepository.get(storageId).ifPresentOrElse(
                existingStorage -> {
                    LOGGER.info("Loaded existing storage {}", storageId);
                    this.storage = new NetworkNodeStorage((Storage<T>) existingStorage);
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
        if (network == null || storage == null) {
            return;
        }
        LOGGER.info("Storage activeness got changed to '{}', updating underlying storage", active);
        if (active) {
            exposedStorage.addSource(storage);
        } else {
            exposedStorage.removeSource(storage);
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

    @Override
    public <S> Optional<Storage<S>> getStorageForChannel(StorageChannelType<S> channelType) {
        if (channelType == this.type) {
            return Optional.of((Storage<S>) exposedStorage);
        }
        return Optional.empty();
    }

    private class ExposedNetworkNodeStorage implements CompositeStorage<T>, TrackedStorage<T>, Priority {
        private final CompositeStorage<T> compositeStorage = new CompositeStorageImpl<>(new ResourceListImpl<>());

        @Override
        public long extract(T resource, long amount, Action action, Source source) {
            return compositeStorage.extract(resource, amount, action, source);
        }

        @Override
        public long insert(T resource, long amount, Action action, Source source) {
            return compositeStorage.insert(resource, amount, action, source);
        }

        @Override
        public Collection<ResourceAmount<T>> getAll() {
            return compositeStorage.getAll();
        }

        @Override
        public long getStored() {
            return compositeStorage.getStored();
        }

        @Override
        public void sortSources() {
            compositeStorage.sortSources();
        }

        @Override
        public void addSource(Storage<T> source) {
            compositeStorage.addSource(source);
        }

        @Override
        public void removeSource(Storage<T> source) {
            compositeStorage.removeSource(source);
        }

        @Override
        public void clearSources() {
            compositeStorage.clearSources();
        }

        @Override
        public void addListener(CompositeStorageListener<T> listener) {
            compositeStorage.addListener(listener);
        }

        @Override
        public void removeListener(CompositeStorageListener<T> listener) {
            compositeStorage.removeListener(listener);
        }

        @Override
        public int getPriority() {
            return priority;
        }

        @Override
        public Optional<TrackedResource> findTrackedResourceBySourceType(T resource, Class<? extends Source> sourceType) {
            return storage.findTrackedResourceBySourceType(resource, sourceType);
        }
    }

    private class NetworkNodeStorage extends ProxyStorage<T> implements TrackedStorage<T> {
        public NetworkNodeStorage(Storage<T> delegate) {
            super(delegate);
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
    }
}
