package com.refinedmods.refinedstorage2.api.network.impl.node.grid;

import com.refinedmods.refinedstorage2.api.grid.GridWatcher;
import com.refinedmods.refinedstorage2.api.grid.service.GridService;
import com.refinedmods.refinedstorage2.api.grid.service.GridServiceFactory;
import com.refinedmods.refinedstorage2.api.grid.service.GridServiceImpl;
import com.refinedmods.refinedstorage2.api.network.Network;
import com.refinedmods.refinedstorage2.api.network.component.StorageNetworkComponent;
import com.refinedmods.refinedstorage2.api.network.node.AbstractNetworkNode;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.storage.Actor;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannel;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedResource;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.ToLongFunction;
import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GridNetworkNode extends AbstractNetworkNode implements GridServiceFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(GridNetworkNode.class);

    private final Collection<? extends StorageChannelType<?>> storageChannelTypes;
    private final Map<GridWatcher, GridWatcherRegistration> watchers = new HashMap<>();
    private final long energyUsage;

    public GridNetworkNode(final long energyUsage,
                           final Collection<? extends StorageChannelType<?>> storageChannelTypes) {
        this.energyUsage = energyUsage;
        this.storageChannelTypes = storageChannelTypes;
    }

    private <T> StorageChannel<T> getStorageChannel(final StorageChannelType<T> type) {
        if (network == null) {
            throw new IllegalStateException("Network must be present to retrieve storage channel");
        }
        return network.getComponent(StorageNetworkComponent.class).getStorageChannel(type);
    }

    public <T> List<GridResource<T>> getResources(final StorageChannelType<T> type,
                                                  final Class<? extends Actor> actorType) {
        final StorageChannel<T> storageChannel = getStorageChannel(type);
        return storageChannel.getAll().stream().map(resourceAmount -> new GridResource<>(
            resourceAmount,
            storageChannel.findTrackedResourceByActorType(resourceAmount.getResource(), actorType).orElse(null)
        )).toList();
    }

    @Override
    public long getEnergyUsage() {
        return energyUsage;
    }

    public void addWatcher(final GridWatcher watcher, final Class<? extends Actor> actorType) {
        if (watchers.containsKey(watcher)) {
            throw new IllegalArgumentException("Watcher is already registered");
        }
        final GridWatcherRegistration registration = new GridWatcherRegistration(watcher, actorType);
        attachAll(registration);
        watchers.put(watcher, registration);
        LOGGER.info("Added watcher {}, new count is {}", watcher, watchers.size());
    }

    private void attachAll(final GridWatcherRegistration registration) {
        storageChannelTypes.forEach(storageChannelType -> attach(registration, storageChannelType));
    }

    private <T> void attach(final GridWatcherRegistration registration,
                            final StorageChannelType<T> storageChannelType) {
        LOGGER.info("Attaching {} to {}", registration, storageChannelType);
        registration.attach(getStorageChannel(storageChannelType), storageChannelType);
    }

    public void removeWatcher(final GridWatcher watcher) {
        final GridWatcherRegistration registration = watchers.get(watcher);
        if (registration == null) {
            throw new IllegalArgumentException("Watcher is not registered");
        }
        detachAll(registration);
        watchers.remove(watcher);
        LOGGER.info("Removed watcher {}, remaining {}", watcher, watchers.size());
    }

    private void detachAll(final GridWatcherRegistration registration) {
        storageChannelTypes.forEach(storageChannelType -> detach(registration, storageChannelType));
    }

    private <T> void detach(final GridWatcherRegistration registration,
                            final StorageChannelType<T> storageChannelType) {
        LOGGER.info("Detaching {} from {}", registration, storageChannelType);
        registration.detach(getStorageChannel(storageChannelType), storageChannelType);
    }

    @Override
    protected void onActiveChanged(final boolean newActive) {
        super.onActiveChanged(newActive);
        watchers.keySet().forEach(watcher -> watcher.onActiveChanged(newActive));
    }

    @Override
    public void setNetwork(@Nullable final Network network) {
        LOGGER.info("Network has changed to {}, detaching {} watchers", network, watchers.size());
        if (this.network != null) {
            watchers.forEach((watcher, registration) -> {
                detachAll(registration);
                watcher.onNetworkChanged();
            });
            watchers.clear();
        }
        super.setNetwork(network);
    }

    @Override
    public <T> GridService<T> create(
        final StorageChannelType<T> storageChannelType,
        final Actor actor,
        final ToLongFunction<T> maxAmountProvider,
        final long singleAmount
    ) {
        final StorageChannel<T> storageChannel = getStorageChannel(storageChannelType);
        return new GridServiceImpl<>(storageChannel, actor, maxAmountProvider, singleAmount);
    }

    public record GridResource<T>(
        ResourceAmount<T> resourceAmount,
        @Nullable TrackedResource trackedResource
    ) {
    }
}
