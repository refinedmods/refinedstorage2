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
import javax.annotation.Nullable;

public class ExternalStorageNetworkNode extends AbstractNetworkNode implements StorageProvider, StorageConfiguration {
    private final long energyUsage;
    private final Map<StorageChannelType<?>, ConfiguredStorage<?>> storages = new HashMap<>();
    private final Filter filter = new Filter();

    private AccessMode accessMode = AccessMode.INSERT_EXTRACT;
    private int priority;

    public ExternalStorageNetworkNode(final long energyUsage,
                                      final OrderedRegistry<?, StorageChannelType<?>> storageChannelTypeRegistry) {
        this.energyUsage = energyUsage;
        initialize(storageChannelTypeRegistry);
    }

    private void initialize(final OrderedRegistry<?, StorageChannelType<?>> storageChannelTypeRegistry) {
        storageChannelTypeRegistry.getAll().forEach(type -> storages.put(type, new ConfiguredStorage<>()));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public void initialize(final ExternalStorageProviderFactory factory) {
        stopExposingInternalStorages();
        storages.forEach((type, storage) -> initialize(factory, (StorageChannelType) type, storage));
    }

    private <T> void initialize(final ExternalStorageProviderFactory factory,
                                final StorageChannelType<T> type,
                                final ConfiguredStorage<T> configuredStorage) {
        factory.create(type).ifPresent(provider -> {
            configuredStorage.internalStorage = new ExternalStorage<>(provider);
            if (isActive()) {
                makeInternalStorageVisible(configuredStorage);
            }
        });
    }

    @Override
    protected void onActiveChanged(final boolean newActive) {
        super.onActiveChanged(newActive);
        if (!newActive) {
            stopExposingInternalStorages();
        } else {
            storages.values().forEach(this::makeInternalStorageVisible);
        }
    }

    private void stopExposingInternalStorages() {
        storages.values().forEach(s -> s.exposedStorage.tryRemoveStorage());
    }

    private <T> void makeInternalStorageVisible(final ConfiguredStorage<T> storage) {
        if (storage.internalStorage == null) {
            return;
        }
        storage.exposedStorage.setStorage(storage.internalStorage);
    }

    public boolean detectChanges() {
        return storages.values().stream().anyMatch(storage -> storage.exposedStorage.detectChanges());
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
    public boolean isAllowed(final Object resource) {
        return filter.isAllowed(resource);
    }

    @Override
    public void setPriority(final int priority) {
        this.priority = priority;
        if (network != null) {
            storages.keySet().forEach(type -> network
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

    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<Storage<T>> getStorageForChannel(final StorageChannelType<T> channelType) {
        final ConfiguredStorage<?> storage = storages.get(channelType);
        if (storage == null) {
            return Optional.empty();
        }
        return Optional.of((Storage<T>) storage.exposedStorage);
    }

    private class ConfiguredStorage<T> {
        @Nullable
        private ExternalStorage<T> internalStorage;
        private final NetworkNodeStorage<T> exposedStorage;

        private ConfiguredStorage() {
            this.exposedStorage = new NetworkNodeStorage<>(ExternalStorageNetworkNode.this);
        }
    }
}
