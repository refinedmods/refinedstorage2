package com.refinedmods.refinedstorage2.api.network.component;

import com.refinedmods.refinedstorage2.api.core.registry.OrderedRegistry;
import com.refinedmods.refinedstorage2.api.network.node.container.NetworkNodeContainer;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannel;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;

import java.util.HashMap;
import java.util.Map;

import org.apiguardian.api.API;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.1")
public class StorageNetworkComponent implements NetworkComponent {
    private static final Logger LOGGER = LoggerFactory.getLogger(StorageNetworkComponent.class);

    private final Map<StorageChannelType<?>, StorageChannel<?>> channels = new HashMap<>();

    public StorageNetworkComponent(
        final OrderedRegistry<?, ? extends StorageChannelType<?>> storageChannelTypeRegistry
    ) {
        for (final StorageChannelType<?> type : storageChannelTypeRegistry.getAll()) {
            channels.put(type, type.create());
        }
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void onContainerAdded(final NetworkNodeContainer container) {
        if (container.getNode() instanceof StorageProvider provider) {
            for (final Map.Entry<StorageChannelType<?>, StorageChannel<?>> entry : channels.entrySet()) {
                tryAddStorageFromProviderToChannel(provider, (StorageChannelType) entry.getKey(), entry.getValue());
            }
        }
    }

    private <T> void tryAddStorageFromProviderToChannel(final StorageProvider provider,
                                                        final StorageChannelType<T> type,
                                                        final StorageChannel<T> channel) {
        provider.getStorageForChannel(type).ifPresent(storage -> {
            LOGGER.info("Adding source {} to channel {} from provider {}", storage, type, provider);
            channel.addSource(storage);
        });
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void onContainerRemoved(final NetworkNodeContainer container) {
        if (container.getNode() instanceof StorageProvider provider) {
            for (final Map.Entry<StorageChannelType<?>, StorageChannel<?>> entry : channels.entrySet()) {
                final StorageChannelType storageChannelType = entry.getKey();
                final StorageChannel<?> storageChannel = entry.getValue();
                tryRemoveStorageFromProviderFromChannel(provider, storageChannelType, storageChannel);
            }
        }
    }

    private <T> void tryRemoveStorageFromProviderFromChannel(final StorageProvider provider,
                                                             final StorageChannelType<T> type,
                                                             final StorageChannel<T> channel) {
        provider.getStorageForChannel(type).ifPresent(storage -> {
            LOGGER.info("Removing source {} from channel {} of provider {}", storage, type, provider);
            channel.removeSource(storage);
        });
    }

    @SuppressWarnings("unchecked")
    public <T> StorageChannel<T> getStorageChannel(final StorageChannelType<T> type) {
        return (StorageChannel<T>) channels.get(type);
    }
}
