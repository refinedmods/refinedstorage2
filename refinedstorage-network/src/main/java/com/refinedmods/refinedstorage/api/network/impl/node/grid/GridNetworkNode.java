package com.refinedmods.refinedstorage.api.network.impl.node.grid;

import com.refinedmods.refinedstorage.api.autocrafting.status.TaskStatusProvider;
import com.refinedmods.refinedstorage.api.network.Network;
import com.refinedmods.refinedstorage.api.network.autocrafting.AutocraftingNetworkComponent;
import com.refinedmods.refinedstorage.api.network.impl.node.AbstractNetworkNode;
import com.refinedmods.refinedstorage.api.network.node.grid.GridWatcher;
import com.refinedmods.refinedstorage.api.network.storage.StorageNetworkComponent;
import com.refinedmods.refinedstorage.api.storage.Actor;

import org.jspecify.annotations.Nullable;

public class GridNetworkNode extends AbstractNetworkNode {
    private final long energyUsage;
    private final GridWatcherManager watchers = new GridWatcherManagerImpl();

    public GridNetworkNode(final long energyUsage) {
        this.energyUsage = energyUsage;
    }

    @Override
    public long getEnergyUsage() {
        return energyUsage;
    }

    public void addWatcher(final GridWatcher watcher, final Class<? extends Actor> actorType) {
        watchers.addWatcher(watcher, actorType, getStorage(), getAutocrafting());
    }

    public void removeWatcher(final GridWatcher watcher) {
        watchers.removeWatcher(watcher, getStorage(), getAutocrafting());
    }

    @Override
    protected void onActiveChanged(final boolean newActive) {
        super.onActiveChanged(newActive);
        watchers.activeChanged(newActive);
    }

    @Override
    public void setNetwork(@Nullable final Network network) {
        if (this.network != null) {
            watchers.detachAll(this.network.getComponent(StorageNetworkComponent.class), getAutocrafting());
        }
        super.setNetwork(network);
        if (this.network != null) {
            watchers.attachAll(this.network.getComponent(StorageNetworkComponent.class), getAutocrafting());
        }
    }

    @Nullable
    private StorageNetworkComponent getStorage() {
        return network == null ? null : network.getComponent(StorageNetworkComponent.class);
    }

    @Nullable
    private TaskStatusProvider getAutocrafting() {
        return network == null ? null : network.getComponent(AutocraftingNetworkComponent.class);
    }
}
