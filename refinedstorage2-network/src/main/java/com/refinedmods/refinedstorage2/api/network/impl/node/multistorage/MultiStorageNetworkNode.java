package com.refinedmods.refinedstorage2.api.network.impl.node.multistorage;

import com.refinedmods.refinedstorage2.api.core.registry.OrderedRegistry;
import com.refinedmods.refinedstorage2.api.network.component.StorageProvider;
import com.refinedmods.refinedstorage2.api.network.node.AbstractStorageNetworkNode;
import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;

import java.util.Arrays;
import java.util.HashSet;
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
    private MultiStorageListener listener;

    private final long energyUsage;
    private final long energyUsagePerStorage;

    private final MultiStorageInternalStorage<?>[] cache;
    private final Map<StorageChannelType<?>, MultiStorageExposedStorage<?>> exposedStorages;
    private int activeStorages;

    public MultiStorageNetworkNode(final long energyUsage,
                                   final long energyUsagePerStorage,
                                   final OrderedRegistry<?, ? extends StorageChannelType<?>> storageChannelTypeRegistry,
                                   final int size) {
        this.energyUsage = energyUsage;
        this.energyUsagePerStorage = energyUsagePerStorage;
        this.exposedStorages = createExposedStorages(storageChannelTypeRegistry);
        this.cache = new MultiStorageInternalStorage[size];
    }

    private Map<StorageChannelType<?>, MultiStorageExposedStorage<?>> createExposedStorages(
        final OrderedRegistry<?, ? extends StorageChannelType<?>> storageChannelTypeRegistry
    ) {
        return storageChannelTypeRegistry
            .getAll()
            .stream()
            .collect(Collectors.toUnmodifiableMap(Function.identity(), this::createExposedStorage));
    }

    private MultiStorageExposedStorage<?> createExposedStorage(final StorageChannelType<?> type) {
        return new MultiStorageExposedStorage<>(this);
    }

    public void setProvider(final MultiStorageProvider provider) {
        this.provider = provider;
        // Avoid initializing multiple times, this causes problems with already initialized storages going out of sync
        // with the composite internalStorage (object reference changes).
        if (activeStorages > 0) {
            return; // TODO: Test?
        }
        for (int i = 0; i < cache.length; ++i) {
            initializeStorage(i);
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
            final StorageChannelType<?> removedType = cache[index].getStorageChannelType();
            final MultiStorageExposedStorage<?> relevantComposite = exposedStorages.get(removedType);
            results.add(new StorageChange(true, relevantComposite, cache[index]));
        }

        if (provider != null) {
            provider.resolve(index).ifPresentOrElse(resolved -> {
                cache[index] = new MultiStorageInternalStorage(
                    resolved.storage(),
                    resolved.storageChannelType(),
                    listener
                );
                final MultiStorageExposedStorage<?> relevantComposite = exposedStorages.get(
                    resolved.storageChannelType()
                );
                results.add(new StorageChange(false, relevantComposite, cache[index]));
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
        LOGGER.info("Activeness got changed to {}, updating underlying internal storages", newActive);
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
                                             final MultiStorageExposedStorage<?> exposedStorage) {
        for (final MultiStorageInternalStorage<?> internalStorage : cache) {
            if (internalStorage != null && internalStorage.getStorageChannelType() == type) {
                exposedStorage.addSource((MultiStorageInternalStorage) internalStorage);
            }
        }
    }

    private void disableAllStorages() {
        exposedStorages.values().forEach(MultiStorageExposedStorage::clearSources);
    }

    public void setListener(final MultiStorageListener listener) {
        this.listener = listener;
    }

    @Override
    public long getEnergyUsage() {
        return energyUsage + (energyUsagePerStorage * activeStorages);
    }

    public MultiStorageState createState() {
        return MultiStorageState.of(cache.length, idx -> getState(cache[idx]));
    }

    private MultiStorageStorageState getState(@Nullable final MultiStorageInternalStorage<?> internalStorage) {
        if (internalStorage == null) {
            return MultiStorageStorageState.NONE;
        } else if (!isActive()) {
            return MultiStorageStorageState.INACTIVE;
        }
        return internalStorage.getState();
    }

    @Override
    protected Set<StorageChannelType<?>> getRelevantStorageChannelTypes() {
        return exposedStorages.keySet();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<Storage<T>> getStorageForChannel(final StorageChannelType<T> channelType) {
        final MultiStorageExposedStorage<?> storage = exposedStorages.get(channelType);
        if (storage != null) {
            return Optional.of((Storage<T>) storage);
        }
        return Optional.empty();
    }

    public int getSize() {
        return cache.length;
    }

    private record StorageChange(boolean removed,
                                 MultiStorageExposedStorage<?> exposedStorage,
                                 MultiStorageInternalStorage<?> internalStorage) {
    }
}
