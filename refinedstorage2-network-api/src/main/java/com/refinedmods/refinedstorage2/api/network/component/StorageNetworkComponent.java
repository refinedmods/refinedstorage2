package com.refinedmods.refinedstorage2.api.network.component;

import com.refinedmods.refinedstorage2.api.core.registry.OrderedRegistry;
import com.refinedmods.refinedstorage2.api.network.node.container.NetworkNodeContainer;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannel;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class StorageNetworkComponent implements NetworkComponent {
    private static final Logger LOGGER = LogManager.getLogger();

    private final Map<StorageChannelType<?>, StorageChannel<?>> channels = new HashMap<>();

    public StorageNetworkComponent(OrderedRegistry<?, StorageChannelType<?>> storageChannelTypeRegistry) {
        for (StorageChannelType<?> type : storageChannelTypeRegistry.getAll()) {
            channels.put(type, type.create());
        }
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void onContainerAdded(NetworkNodeContainer container) {
        if (container.getNode() instanceof StorageProvider provider) {
            for (Map.Entry<StorageChannelType<?>, StorageChannel<?>> entry : channels.entrySet()) {
                tryAddStorageFromProviderToChannel(provider, (StorageChannelType) entry.getKey(), entry.getValue());
            }
        }
    }

    private <T> void tryAddStorageFromProviderToChannel(StorageProvider provider, StorageChannelType<T> type, StorageChannel<T> channel) {
        provider.getStorageForChannel(type).ifPresent(storage -> {
            LOGGER.info("Adding source {} to channel {} from provider {}", storage, type, provider);
            channel.addSource(storage);
        });
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void onContainerRemoved(NetworkNodeContainer container) {
        if (container.getNode() instanceof StorageProvider provider) {
            for (Map.Entry<StorageChannelType<?>, StorageChannel<?>> entry : channels.entrySet()) {
                tryRemoveStorageFromProviderFromChannel(provider, (StorageChannelType) entry.getKey(), entry.getValue());
            }
        }
    }

    private <T> void tryRemoveStorageFromProviderFromChannel(StorageProvider provider, StorageChannelType<T> type, StorageChannel<T> channel) {
        provider.getStorageForChannel(type).ifPresent(storage -> {
            LOGGER.info("Removing source {} from channel {} of provider {}", storage, type, provider);
            channel.removeSource(storage);
        });
    }

    @SuppressWarnings("unchecked")
    public <T> StorageChannel<T> getStorageChannel(StorageChannelType<T> type) {
        return (StorageChannel<T>) channels.get(type);
    }
}
