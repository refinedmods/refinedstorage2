package com.refinedmods.refinedstorage2.api.network.impl.node.grid;

import com.refinedmods.refinedstorage2.api.grid.GridWatcher;
import com.refinedmods.refinedstorage2.api.network.Network;
import com.refinedmods.refinedstorage2.api.network.node.AbstractNetworkNode;
import com.refinedmods.refinedstorage2.api.storage.Actor;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;

import java.util.Collection;
import javax.annotation.Nullable;

import static java.util.Objects.requireNonNull;

public class GridNetworkNode extends AbstractNetworkNode {
    private final long energyUsage;
    private final GridWatchers watchers;

    public GridNetworkNode(final long energyUsage,
                           final Collection<? extends StorageChannelType<?>> storageChannelTypes) {
        this.energyUsage = energyUsage;
        this.watchers = new GridWatchers(storageChannelTypes);
    }

    @Override
    public long getEnergyUsage() {
        return energyUsage;
    }

    public void addWatcher(final GridWatcher watcher, final Class<? extends Actor> actorType) {
        watchers.addWatcher(watcher, actorType, requireNonNull(network));
    }

    public void removeWatcher(final GridWatcher watcher) {
        watchers.removeWatcher(watcher, requireNonNull(network));
    }

    @Override
    protected void onActiveChanged(final boolean newActive) {
        super.onActiveChanged(newActive);
        watchers.activeChanged(newActive);
    }

    @Override
    public void setNetwork(@Nullable final Network network) {
        if (this.network != null) {
            watchers.detachAll(this.network);
        }
        super.setNetwork(network);
        if (this.network != null) {
            watchers.attachAll(this.network);
        }
    }
}
