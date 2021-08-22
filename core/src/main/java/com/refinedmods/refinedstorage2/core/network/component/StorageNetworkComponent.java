package com.refinedmods.refinedstorage2.core.network.component;

import com.refinedmods.refinedstorage2.api.stack.Rs2Stack;
import com.refinedmods.refinedstorage2.api.storage.StorageSource;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannel;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelTypeRegistry;
import com.refinedmods.refinedstorage2.core.network.node.container.NetworkNodeContainer;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class StorageNetworkComponent implements NetworkComponent {
    private static final Logger LOGGER = LogManager.getLogger();

    private final Map<StorageChannelType<?>, StorageChannel<?>> channels = new HashMap<>();

    public StorageNetworkComponent(StorageChannelTypeRegistry storageChannelTypeRegistry) {
        for (StorageChannelType<?> type : storageChannelTypeRegistry.getTypes()) {
            channels.put(type, type.create());
        }
    }

    @Override
    public void onContainerAdded(NetworkNodeContainer<?> container) {
        if (container.getNode() instanceof StorageSource source) {
            for (Map.Entry<StorageChannelType<?>, StorageChannel<?>> entry : channels.entrySet()) {
                tryAddSourceToChannel(source, entry.getKey(), entry.getValue());
            }
        }
    }

    private void tryAddSourceToChannel(StorageSource source, StorageChannelType<?> type, StorageChannel<?> channel) {
        source.getStorageForChannel(type).ifPresent(storage -> {
            LOGGER.info("Adding source to channel {}", type);
            channel.addSource(storage);
        });
    }

    @Override
    public void onContainerRemoved(NetworkNodeContainer<?> container) {
        if (container.getNode() instanceof StorageSource source) {
            for (Map.Entry<StorageChannelType<?>, StorageChannel<?>> entry : channels.entrySet()) {
                tryRemoveSourceFromChannel(source, entry.getKey(), entry.getValue());
            }
        }
    }

    private void tryRemoveSourceFromChannel(StorageSource source, StorageChannelType<?> type, StorageChannel<?> channel) {
        source.getStorageForChannel(type).ifPresent(storage -> {
            LOGGER.info("Removing source from channel {}", type);
            channel.removeSource(storage);
        });
    }

    public <T extends Rs2Stack> StorageChannel<T> getStorageChannel(StorageChannelType<T> type) {
        return (StorageChannel<T>) channels.get(type);
    }
}
