package com.refinedmods.refinedstorage2.api.network.impl.storage;

import com.refinedmods.refinedstorage2.api.network.Network;
import com.refinedmods.refinedstorage2.api.network.component.EnergyNetworkComponent;
import com.refinedmods.refinedstorage2.api.network.node.NetworkNode;

import javax.annotation.Nullable;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.0")
public abstract class AbstractNetworkNode implements NetworkNode {
    @Nullable
    protected Network network;
    private boolean active;

    @Override
    @Nullable
    public Network getNetwork() {
        return network;
    }

    @Override
    public void setNetwork(@Nullable final Network network) {
        this.network = network;
    }

    public void setActive(final boolean active) {
        this.active = active;
        onActiveChanged(active);
    }

    public boolean isActive() {
        return active;
    }

    public void doWork() {
        if (network == null || !active) {
            return;
        }
        network.getComponent(EnergyNetworkComponent.class).extract(getEnergyUsage());
    }

    protected void onActiveChanged(final boolean newActive) {
    }

    public abstract long getEnergyUsage();
}
