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
    private final Map<StorageChannelType<?>, DynamicStorage<?>> storages = new HashMap<>();

    public ExternalStorageNetworkNode(final long energyUsage,
                                      final OrderedRegistry<?, StorageChannelType<?>> storageChannelTypeRegistry) {
        this.energyUsage = energyUsage;
        initialize(storageChannelTypeRegistry);
    }

    private void initialize(final OrderedRegistry<?, StorageChannelType<?>> storageChannelTypeRegistry) {
        storageChannelTypeRegistry.getAll().forEach(type -> storages.put(type, new DynamicStorage<>()));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public void initialize(final ExternalStorageProviderFactory factory) {
        storages.forEach((type, storage) -> {
            storage.exposedStorage.tryClearDelegate();
            initialize(factory, (StorageChannelType) type, storage);
        });
    }

    private <T> void initialize(final ExternalStorageProviderFactory factory,
                                final StorageChannelType<T> type,
                                final DynamicStorage<T> dynamicStorage) {
        factory.create(type).ifPresent(provider -> {
            dynamicStorage.internalStorage = new ExternalStorage<>(provider);
            if (isActive()) {
                dynamicStorage.setVisible(true);
            }
        });
    }

    @Override
    protected void onActiveChanged(final boolean newActive) {
        super.onActiveChanged(newActive);
        storages.values().forEach(storage -> storage.setVisible(newActive));
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
        final DynamicStorage<?> storage = storages.get(channelType);
        if (storage == null) {
            return Optional.empty();
        }
        return Optional.of((Storage<T>) storage.exposedStorage);
    }

    private class DynamicStorage<T> {
        private final ExposedExternalStorage<T> exposedStorage;
        @Nullable
        private ExternalStorage<T> internalStorage;

        private DynamicStorage() {
            this.exposedStorage = new ExposedExternalStorage<>(ExternalStorageNetworkNode.this);
        }

        public void setVisible(final boolean visible) {
            if (visible) {
                tryMakeInternalStorageVisible();
            } else {
                exposedStorage.tryClearDelegate();
            }
        }

        private void tryMakeInternalStorageVisible() {
            if (internalStorage == null) {
                return;
            }
            exposedStorage.setDelegate(internalStorage);
        }
    }
}
