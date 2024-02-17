package com.refinedmods.refinedstorage2.api.network.impl.node.multistorage;

import com.refinedmods.refinedstorage2.api.network.component.StorageProvider;
import com.refinedmods.refinedstorage2.api.network.node.AbstractStorageNetworkNode;
import com.refinedmods.refinedstorage2.api.storage.StateTrackedStorage;
import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.StorageState;
import com.refinedmods.refinedstorage2.api.storage.TypedStorage;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MultiStorageNetworkNode extends AbstractStorageNetworkNode implements StorageProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(MultiStorageNetworkNode.class);

    @Nullable
    private MultiStorageProvider provider;
    @Nullable
    private StateTrackedStorage.Listener listener;

    private final long energyUsage;
    private final long energyUsagePerStorage;

    private final TypedStorage<?, StateTrackedStorage<?>>[] cache;
    private final Map<StorageChannelType<?>, ExposedStorage<?>> exposedStorages;
    private int activeStorages;

    public MultiStorageNetworkNode(final long energyUsage,
                                   final long energyUsagePerStorage,
                                   final Collection<? extends StorageChannelType<?>> storageChannelTypes,
                                   final int size) {
        this.energyUsage = energyUsage;
        this.energyUsagePerStorage = energyUsagePerStorage;
        this.exposedStorages = createExposedStorages(storageChannelTypes);
        this.cache = new TypedStorage[size];
    }

    private Map<StorageChannelType<?>, ExposedStorage<?>> createExposedStorages(
        final Collection<? extends StorageChannelType<?>> storageChannelTypes
    ) {
        return storageChannelTypes.stream().collect(Collectors.toUnmodifiableMap(
            Function.identity(),
            this::createExposedStorage
        ));
    }

    private ExposedStorage<?> createExposedStorage(final StorageChannelType<?> type) {
        return new ExposedStorage<>(this);
    }

    public void setProvider(final MultiStorageProvider provider) {
        this.provider = provider;
        final List<StorageChange> changes = new ArrayList<>();
        for (int i = 0; i < cache.length; ++i) {
            changes.addAll(initializeStorage(i));
        }
        // If we are already initialized, update all the storages to keep the exposed storages in sync.
        // If we are not initialized, update nothing as we have to wait for an activeness update.
        if (activeStorages > 0) {
            changes.forEach(this::processStorageChange);
        }
        updateActiveStorageCount();
    }

    public void onStorageChanged(final int index) {
        if (index < 0 || index >= cache.length) {
            LOGGER.warn("Invalid index {}", index);
            return;
        }
        initializeStorage(index).forEach(this::processStorageChange);
        updateActiveStorageCount();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private Set<StorageChange> initializeStorage(final int index) {
        final Set<StorageChange> results = new HashSet<>();

        if (cache[index] != null) {
            final StorageChannelType<?> removedType = cache[index].storageChannelType();
            final ExposedStorage<?> relevantComposite = exposedStorages.get(removedType);
            results.add(new StorageChange(true, relevantComposite, cache[index].storage()));
        }

        if (provider != null) {
            provider.resolve(index).ifPresentOrElse(resolved -> {
                cache[index] = (TypedStorage) StateTrackedStorage.of(resolved, listener);
                final ExposedStorage<?> relevantComposite = exposedStorages.get(resolved.storageChannelType());
                results.add(new StorageChange(false, relevantComposite, cache[index].storage()));
            }, () -> cache[index] = null);
        }

        return results;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void processStorageChange(final StorageChange change) {
        if (!isActive()) {
            return;
        }
        if (change.removed) {
            change.exposedStorage.removeSource((Storage) change.internalStorage);
        } else {
            change.exposedStorage.addSource((Storage) change.internalStorage);
        }
    }

    private void updateActiveStorageCount() {
        this.activeStorages = (int) Arrays.stream(cache).filter(Objects::nonNull).count();
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
        exposedStorages.forEach(this::enableAllStoragesForChannel);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void enableAllStoragesForChannel(final StorageChannelType<?> type,
                                             final ExposedStorage<?> exposedStorage) {
        for (final TypedStorage<?, StateTrackedStorage<?>> internalStorage : cache) {
            if (internalStorage != null && internalStorage.storageChannelType() == type) {
                exposedStorage.addSource((StateTrackedStorage) internalStorage.storage());
            }
        }
    }

    private void disableAllStorages() {
        exposedStorages.values().forEach(ExposedStorage::clearSources);
    }

    public void setListener(final StateTrackedStorage.Listener listener) {
        this.listener = listener;
    }

    @Override
    public long getEnergyUsage() {
        return energyUsage + (energyUsagePerStorage * activeStorages);
    }

    public int getSize() {
        return cache.length;
    }

    public StorageState getState(final int index) {
        final var storage = cache[index];
        if (storage == null) {
            return StorageState.NONE;
        }
        if (!isActive()) {
            return StorageState.INACTIVE;
        }
        return storage.storage().getState();
    }

    @Override
    protected Set<? extends StorageChannelType<?>> getRelevantStorageChannelTypes() {
        return exposedStorages.keySet();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<Storage<T>> getStorageForChannel(final StorageChannelType<T> channelType) {
        final ExposedStorage<?> storage = exposedStorages.get(channelType);
        if (storage != null) {
            return Optional.of((Storage<T>) storage);
        }
        return Optional.empty();
    }

    private record StorageChange(boolean removed,
                                 ExposedStorage<?> exposedStorage,
                                 StateTrackedStorage<?> internalStorage) {
    }
}
