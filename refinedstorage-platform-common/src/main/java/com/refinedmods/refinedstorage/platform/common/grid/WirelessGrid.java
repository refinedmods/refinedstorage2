package com.refinedmods.refinedstorage.platform.common.grid;

import com.refinedmods.refinedstorage.api.grid.operations.GridOperations;
import com.refinedmods.refinedstorage.api.grid.operations.NoopGridOperations;
import com.refinedmods.refinedstorage.api.grid.watcher.GridWatcher;
import com.refinedmods.refinedstorage.api.grid.watcher.GridWatcherManager;
import com.refinedmods.refinedstorage.api.grid.watcher.GridWatcherManagerImpl;
import com.refinedmods.refinedstorage.api.network.energy.EnergyNetworkComponent;
import com.refinedmods.refinedstorage.api.network.storage.StorageNetworkComponent;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.api.storage.NoopStorage;
import com.refinedmods.refinedstorage.api.storage.Storage;
import com.refinedmods.refinedstorage.api.storage.TrackedResourceAmount;
import com.refinedmods.refinedstorage.platform.api.grid.Grid;
import com.refinedmods.refinedstorage.platform.api.security.PlatformSecurityNetworkComponent;
import com.refinedmods.refinedstorage.platform.api.storage.PlayerActor;
import com.refinedmods.refinedstorage.platform.api.support.network.bounditem.NetworkBoundItemSession;
import com.refinedmods.refinedstorage.platform.api.support.resource.ResourceType;
import com.refinedmods.refinedstorage.platform.common.Platform;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import net.minecraft.server.level.ServerPlayer;

class WirelessGrid implements Grid {
    private final NetworkBoundItemSession session;
    private final GridWatcherManager watchers = new GridWatcherManagerImpl();

    WirelessGrid(final NetworkBoundItemSession session) {
        this.session = session;
    }

    private Optional<StorageNetworkComponent> getStorage() {
        return session.resolveNetwork().map(network -> network.getComponent(StorageNetworkComponent.class));
    }

    private Optional<PlatformSecurityNetworkComponent> getSecurity() {
        return session.resolveNetwork().map(network -> network.getComponent(PlatformSecurityNetworkComponent.class));
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
    public GridOperations createOperations(final ResourceType resourceType, final ServerPlayer player) {
        return getStorage()
            .flatMap(storageChannel ->
                getSecurity().map(security -> createGridOperations(resourceType, player, storageChannel, security)))
            .map(gridOperations -> (GridOperations) new WirelessGridOperations(gridOperations, session, watchers))
            .orElseGet(NoopGridOperations::new);
    }

    private GridOperations createGridOperations(final ResourceType resourceType,
                                                final ServerPlayer player,
                                                final StorageNetworkComponent storageChannel,
                                                final PlatformSecurityNetworkComponent securityNetworkComponent) {
        final PlayerActor playerActor = new PlayerActor(player);
        final GridOperations operations = resourceType.createGridOperations(storageChannel, playerActor);
        return new SecuredGridOperations(player, securityNetworkComponent, operations);
    }
}
