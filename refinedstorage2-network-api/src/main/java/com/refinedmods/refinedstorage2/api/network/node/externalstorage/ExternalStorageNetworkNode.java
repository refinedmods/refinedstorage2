package com.refinedmods.refinedstorage2.api.network.node.externalstorage;

import com.refinedmods.refinedstorage2.api.core.registry.OrderedRegistry;
import com.refinedmods.refinedstorage2.api.network.component.StorageProvider;
import com.refinedmods.refinedstorage2.api.network.node.AbstractStorageNetworkNode;
import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;
import com.refinedmods.refinedstorage2.api.storage.external.ExternalStorage;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;

public class ExternalStorageNetworkNode extends AbstractStorageNetworkNode implements StorageProvider {
    private final long energyUsage;
    private final Map<StorageChannelType<?>, ConfiguredStorage<?>> storages = new HashMap<>();

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
    protected Set<StorageChannelType<?>> getRelevantStorageChannelTypes() {
        return storages.keySet();
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
