package com.refinedmods.refinedstorage2.api.network.component;

import com.refinedmods.refinedstorage2.api.network.node.container.NetworkNodeContainer;
import com.refinedmods.refinedstorage2.api.storage.StorageProvider;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannel;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelTypeRegistry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class StorageNetworkComponent implements NetworkComponent {
    private static final Logger LOGGER = LogManager.getLogger();

    private final Map<StorageChannelType<?>, StorageChannel<?>> channels = new HashMap<>();

    public StorageNetworkComponent(StorageChannelTypeRegistry storageChannelTypeRegistry) {
        for (StorageChannelType<?> type : storageChannelTypeRegistry.getTypes()) {
            channels.put(type, type.create());
        }
    }

    @Override
    public void onContainerAdded(NetworkNodeContainer container) {
        if (container.getNode() instanceof StorageProvider provider) {
            for (Map.Entry<StorageChannelType<?>, StorageChannel<?>> entry : channels.entrySet()) {
                tryAddStorageFromProviderToChannel(provider, entry.getKey(), entry.getValue());
            }
        }
    }

    private void tryAddStorageFromProviderToChannel(StorageProvider provider, StorageChannelType<?> type, StorageChannel<?> channel) {
        provider.getStorageForChannel(type).ifPresent(storage -> {
            LOGGER.info("Adding source to channel {}", type);
            channel.addSource(storage);
        });
    }

    @Override
    public void onContainerRemoved(NetworkNodeContainer container) {
        if (container.getNode() instanceof StorageProvider provider) {
            for (Map.Entry<StorageChannelType<?>, StorageChannel<?>> entry : channels.entrySet()) {
                tryRemoveStorageFromProviderFromChannel(provider, entry.getKey(), entry.getValue());
            }
        }
    }

    private void tryRemoveStorageFromProviderFromChannel(StorageProvider provider, StorageChannelType<?> type, StorageChannel<?> channel) {
        provider.getStorageForChannel(type).ifPresent(storage -> {
            LOGGER.info("Removing source from channel {}", type);
            channel.removeSource(storage);
        });
    }

    public <T> StorageChannel<T> getStorageChannel(StorageChannelType<T> type) {
        return (StorageChannel<T>) channels.get(type);
    }
}
