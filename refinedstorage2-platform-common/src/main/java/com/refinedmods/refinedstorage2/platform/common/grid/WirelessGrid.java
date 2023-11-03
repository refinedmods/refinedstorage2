package com.refinedmods.refinedstorage2.platform.common.grid;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.grid.GridWatcher;
import com.refinedmods.refinedstorage2.api.grid.operations.GridExtractMode;
import com.refinedmods.refinedstorage2.api.grid.operations.GridInsertMode;
import com.refinedmods.refinedstorage2.api.grid.operations.GridOperations;
import com.refinedmods.refinedstorage2.api.network.component.EnergyNetworkComponent;
import com.refinedmods.refinedstorage2.api.network.component.StorageNetworkComponent;
import com.refinedmods.refinedstorage2.api.network.impl.node.grid.GridWatchers;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.storage.Actor;
import com.refinedmods.refinedstorage2.api.storage.ExtractableStorage;
import com.refinedmods.refinedstorage2.api.storage.InsertableStorage;
import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.TrackedResourceAmount;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;
import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.grid.Grid;
import com.refinedmods.refinedstorage2.platform.api.storage.channel.PlatformStorageChannelType;
import com.refinedmods.refinedstorage2.platform.api.support.network.bounditem.NetworkBoundItemSession;
import com.refinedmods.refinedstorage2.platform.api.support.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.common.storage.channel.StorageChannelTypes;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

class WirelessGrid implements Grid {
    private final NetworkBoundItemSession session;
    private final GridWatchers watchers;

    WirelessGrid(final NetworkBoundItemSession session) {
        this.session = session;
        this.watchers = new GridWatchers(PlatformApi.INSTANCE.getStorageChannelTypeRegistry().getAll());
    }

    private Optional<StorageNetworkComponent> getStorage() {
        return session.resolveNetwork().map(network -> network.getComponent(StorageNetworkComponent.class));
    }

    @Override
    public void addWatcher(final GridWatcher watcher, final Class<? extends Actor> actorType) {
        session.resolveNetwork().ifPresent(network -> watchers.addWatcher(watcher, actorType, network));
    }

    @Override
    public void removeWatcher(final GridWatcher watcher) {
        session.resolveNetwork().ifPresent(network -> watchers.removeWatcher(watcher, network));
    }

    @Override
    public Storage<ItemResource> getItemStorage() {
        return getStorage().map(storage -> (Storage<ItemResource>) storage.getStorageChannel(StorageChannelTypes.ITEM))
            .orElseGet(NoOpStorage::new);
    }

    @Override
    public boolean isGridActive() {
        final boolean networkActive = session.resolveNetwork().map(
            network -> network.getComponent(EnergyNetworkComponent.class).getStored() > 0
        ).orElse(false);
        return networkActive && session.isActive();
    }

    @Override
    public <T> List<TrackedResourceAmount<T>> getResources(final StorageChannelType<T> type,
                                                           final Class<? extends Actor> actorType) {
        return getStorage().map(storage -> storage.getResources(type, actorType)).orElse(Collections.emptyList());
    }

    @Override
    public <T> GridOperations<T> createOperations(final PlatformStorageChannelType<T> storageChannelType,
                                                  final Actor actor) {
        return getStorage()
            .map(storage -> storage.getStorageChannel(storageChannelType))
            .map(storageChannel -> storageChannelType.createGridOperations(storageChannel, actor))
            .map(gridOperations -> (GridOperations<T>) new WirelessGridOperations<>(gridOperations, session, watchers))
            .orElseGet(this::createNoOpGridOperations);
    }

    private <T> GridOperations<T> createNoOpGridOperations() {
        return new GridOperations<>() {
            @Override
            public boolean extract(final T resource,
                                   final GridExtractMode extractMode,
                                   final InsertableStorage<T> destination) {
                return false;
            }

            @Override
            public boolean insert(final T resource,
                                  final GridInsertMode insertMode,
                                  final ExtractableStorage<T> source) {
                return false;
            }
        };
    }

    private static class NoOpStorage<T> implements Storage<T> {
        @Override
        public long extract(final T resource, final long amount, final Action action, final Actor actor) {
            return 0;
        }

        @Override
        public long insert(final T resource, final long amount, final Action action, final Actor actor) {
            return 0;
        }

        @Override
        public Collection<ResourceAmount<T>> getAll() {
            return Collections.emptyList();
        }

        @Override
        public long getStored() {
            return 0;
        }
    }
}
