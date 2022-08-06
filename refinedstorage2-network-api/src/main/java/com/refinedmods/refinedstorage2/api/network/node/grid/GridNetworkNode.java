package com.refinedmods.refinedstorage2.api.network.node.grid;

import com.refinedmods.refinedstorage2.api.core.CoreValidations;
import com.refinedmods.refinedstorage2.api.grid.GridWatcher;
import com.refinedmods.refinedstorage2.api.grid.service.GridService;
import com.refinedmods.refinedstorage2.api.grid.service.GridServiceImpl;
import com.refinedmods.refinedstorage2.api.network.component.StorageNetworkComponent;
import com.refinedmods.refinedstorage2.api.network.node.AbstractNetworkNode;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.resource.list.listenable.ResourceListListener;
import com.refinedmods.refinedstorage2.api.storage.Actor;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannel;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedResource;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GridNetworkNode<T> extends AbstractNetworkNode {
    private static final Logger LOGGER = LogManager.getLogger();

    private final Set<GridWatcher<T>> watchers = new HashSet<>();
    private final Map<GridWatcher<T>, ResourceListListener<T>> associatedResourceListeners = new HashMap<>();
    private final long energyUsage;
    private final StorageChannelType<T> type;

    public GridNetworkNode(final long energyUsage, final StorageChannelType<T> type) {
        this.energyUsage = energyUsage;
        this.type = type;
    }

    private StorageChannel<T> getStorageChannel() {
        if (network == null) {
            throw new IllegalStateException("Network must be present to retrieve storage channel");
        }
        return network.getComponent(StorageNetworkComponent.class).getStorageChannel(type);
    }

    public int getResourceAmount() {
        return getStorageChannel().getAll().size();
    }

    public void forEachResource(final BiConsumer<ResourceAmount<T>, Optional<TrackedResource>> consumer,
                                final Class<? extends Actor> sourceType) {
        final StorageChannel<T> storageChannel = getStorageChannel();
        storageChannel.getAll().forEach(resourceAmount -> consumer.accept(
            resourceAmount,
            storageChannel.findTrackedResourceByActorType(resourceAmount.getResource(), sourceType)
        ));
    }

    @Override
    public long getEnergyUsage() {
        return energyUsage;
    }

    public void addWatcher(final GridWatcher<T> watcher,
                           final Class<? extends Actor> actorType) {
        CoreValidations.validateNotContains(watchers, watcher, "Watcher is already registered");
        watchers.add(watcher);
        final StorageChannel<T> storageChannel = getStorageChannel();
        final ResourceListListener<T> listener = change -> watcher.onChanged(
            change,
            storageChannel.findTrackedResourceByActorType(
                change.resourceAmount().getResource(),
                actorType
            ).orElse(null)
        );
        storageChannel.addListener(listener);
        associatedResourceListeners.put(watcher, listener);
        LOGGER.info("Watcher was added, new count is {}", watchers.size());
    }

    public void removeWatcher(final GridWatcher<T> watcher) {
        CoreValidations.validateContains(watchers, watcher, "Watcher is not registered");
        watchers.remove(watcher);
        final ResourceListListener<T> listener = Objects.requireNonNull(associatedResourceListeners.get(watcher));
        getStorageChannel().removeListener(listener);
        associatedResourceListeners.remove(watcher);
        LOGGER.info("Watcher was removed, new count is {}", watchers.size());
    }

    public GridService<T> createService(final Actor actor,
                                        final Function<T, Long> maxAmountProvider,
                                        final long singleAmount) {
        return new GridServiceImpl<>(getStorageChannel(), actor, maxAmountProvider, singleAmount);
    }

    @Override
    protected void onActiveChanged(final boolean newActive) {
        super.onActiveChanged(newActive);
        watchers.forEach(watcher -> watcher.onActiveChanged(newActive));
    }
}
