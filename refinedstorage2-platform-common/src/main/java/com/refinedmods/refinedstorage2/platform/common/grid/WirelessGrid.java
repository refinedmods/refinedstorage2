package com.refinedmods.refinedstorage2.platform.common.grid;

import com.refinedmods.refinedstorage2.api.grid.operations.GridOperations;
import com.refinedmods.refinedstorage2.api.grid.operations.NoopGridOperations;
import com.refinedmods.refinedstorage2.api.grid.watcher.GridWatcher;
import com.refinedmods.refinedstorage2.api.grid.watcher.GridWatcherManager;
import com.refinedmods.refinedstorage2.api.grid.watcher.GridWatcherManagerImpl;
import com.refinedmods.refinedstorage2.api.network.component.EnergyNetworkComponent;
import com.refinedmods.refinedstorage2.api.network.component.StorageNetworkComponent;
import com.refinedmods.refinedstorage2.api.storage.Actor;
import com.refinedmods.refinedstorage2.api.storage.NoopStorage;
import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.TrackedResourceAmount;
import com.refinedmods.refinedstorage2.platform.api.grid.Grid;
import com.refinedmods.refinedstorage2.platform.api.support.network.bounditem.NetworkBoundItemSession;
import com.refinedmods.refinedstorage2.platform.api.support.resource.ResourceType;
import com.refinedmods.refinedstorage2.platform.common.Platform;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

class WirelessGrid implements Grid {
    private final NetworkBoundItemSession session;
    private final GridWatcherManager watchers = new GridWatcherManagerImpl();

    WirelessGrid(final NetworkBoundItemSession session) {
        this.session = session;
    }

    private Optional<StorageNetworkComponent> getStorage() {
        return session.resolveNetwork().map(network -> network.getComponent(StorageNetworkComponent.class));
    }

    @Override
    public void addWatcher(final GridWatcher watcher, final Class<? extends Actor> actorType) {
        session.drainEnergy(Platform.INSTANCE.getConfig().getWirelessGrid().getOpenEnergyUsage());
        session.resolveNetwork().ifPresent(network -> watchers.addWatcher(
            watcher,
            actorType,
            network.getComponent(StorageNetworkComponent.class)
        ));
    }

    @Override
    public void removeWatcher(final GridWatcher watcher) {
        session.resolveNetwork().ifPresent(network -> watchers.removeWatcher(
            watcher,
            network.getComponent(StorageNetworkComponent.class)
        ));
    }

    @Override
    public Storage getItemStorage() {
        return getStorage().map(Storage.class::cast).orElseGet(NoopStorage::new);
    }

    @Override
    public boolean isGridActive() {
        final boolean networkActive = session.resolveNetwork().map(
            network -> network.getComponent(EnergyNetworkComponent.class).getStored() > 0
        ).orElse(false);
        return networkActive && session.isActive();
    }

    @Override
    public List<TrackedResourceAmount> getResources(final Class<? extends Actor> actorType) {
        return getStorage().map(storage -> storage.getResources(actorType)).orElse(Collections.emptyList());
    }

    @Override
    public GridOperations createOperations(final ResourceType resourceType,
                                           final Actor actor) {
        return getStorage()
            .map(storageChannel -> resourceType.createGridOperations(storageChannel, actor))
            .map(gridOperations -> (GridOperations) new WirelessGridOperations(gridOperations, session, watchers))
            .orElseGet(NoopGridOperations::new);
    }
}
