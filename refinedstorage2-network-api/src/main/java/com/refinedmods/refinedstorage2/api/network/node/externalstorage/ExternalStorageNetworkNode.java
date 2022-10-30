package com.refinedmods.refinedstorage2.api.network.node.externalstorage;

import com.refinedmods.refinedstorage2.api.core.filter.Filter;
import com.refinedmods.refinedstorage2.api.core.filter.FilterMode;
import com.refinedmods.refinedstorage2.api.core.registry.OrderedRegistry;
import com.refinedmods.refinedstorage2.api.network.component.StorageNetworkComponent;
import com.refinedmods.refinedstorage2.api.network.component.StorageProvider;
import com.refinedmods.refinedstorage2.api.network.node.AbstractNetworkNode;
import com.refinedmods.refinedstorage2.api.network.node.StorageConfiguration;
import com.refinedmods.refinedstorage2.api.storage.AccessMode;
import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;
import com.refinedmods.refinedstorage2.api.storage.external.ExternalStorage;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.UnaryOperator;

public class ExternalStorageNetworkNode extends AbstractNetworkNode implements StorageProvider, StorageConfiguration {
    private final long energyUsage;
    private final Map<StorageChannelType<?>, ExternalStorage<?>> internalStorages = new HashMap<>();
    private final Map<StorageChannelType<?>, NetworkNodeStorage<?>> exposedStorages = new HashMap<>();
    private final OrderedRegistry<?, StorageChannelType<?>> storageChannelTypeRegistry;
    private final Filter filter = new Filter();

    private AccessMode accessMode = AccessMode.INSERT_EXTRACT;
    private int priority;

    public ExternalStorageNetworkNode(final long energyUsage,
                                      final OrderedRegistry<?, StorageChannelType<?>> storageChannelTypeRegistry) {
        this.energyUsage = energyUsage;
        this.storageChannelTypeRegistry = storageChannelTypeRegistry;
        initializeExposedStorages();
    }

    private void initializeExposedStorages() {
        storageChannelTypeRegistry.getAll().forEach(type -> exposedStorages.put(type, new NetworkNodeStorage<>(this)));
    }

    public void initialize(final ExternalStorageProviderFactory factory) {
        storageChannelTypeRegistry.getAll().forEach(type -> initialize(factory, type));
    }

    @SuppressWarnings({"rawtypes"})
    private void initialize(final ExternalStorageProviderFactory factory, final StorageChannelType<?> type) {
        stopExposingInternalStorages();
        factory.create(type).ifPresent(provider -> {
            final ExternalStorage externalStorage = new ExternalStorage<>(provider);
            internalStorages.put(type, externalStorage);
            if (isActive()) {
                trySetInternalStorageOnExposedStorage(type, exposedStorages.get(type));
            }
        });
    }

    @Override
    protected void onActiveChanged(final boolean newActive) {
        super.onActiveChanged(newActive);
        if (!newActive) {
            stopExposingInternalStorages();
        } else {
            exposedStorages.forEach(this::trySetInternalStorageOnExposedStorage);
        }
    }

    private void stopExposingInternalStorages() {
        exposedStorages.values().forEach(NetworkNodeStorage::tryRemoveStorage);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void trySetInternalStorageOnExposedStorage(
        final StorageChannelType<?> type,
        final NetworkNodeStorage<?> exposedStorage
    ) {
        final ExternalStorage internalStorage = internalStorages.get(type);
        if (internalStorage != null) {
            exposedStorage.setStorage(internalStorage);
        }
    }

    public boolean detectChanges() {
        return exposedStorages.values().stream().anyMatch(NetworkNodeStorage::detectChanges);
    }

    @Override
    public long getEnergyUsage() {
        return energyUsage;
    }

    @Override
    public AccessMode getAccessMode() {
        return accessMode;
    }

    @Override
    public void setAccessMode(final AccessMode accessMode) {
        this.accessMode = accessMode;
    }

    @Override
    public FilterMode getFilterMode() {
        return filter.getMode();
    }

    @Override
    public void setFilterMode(final FilterMode filterMode) {
        filter.setMode(filterMode);
    }

    @Override
    public void setPriority(final int priority) {
        this.priority = priority;
        if (network != null) {
            exposedStorages.keySet().forEach(type -> network
                .getComponent(StorageNetworkComponent.class)
                .getStorageChannel(type)
                .sortSources()
            );
        }
    }

    @Override
    public int getPriority() {
        return priority;
    }

    public void setFilterTemplates(final Set<Object> templates) {
        filter.setTemplates(templates);
    }

    public void setNormalizer(final UnaryOperator<Object> normalizer) {
        filter.setNormalizer(normalizer);
    }

    public <T> boolean isAllowed(final T resource) {
        return filter.isAllowed(resource);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<Storage<T>> getStorageForChannel(final StorageChannelType<T> channelType) {
        final Storage<?> storage = exposedStorages.get(channelType);
        if (storage == null) {
            return Optional.empty();
        }
        return Optional.of((Storage<T>) storage);
    }
}
