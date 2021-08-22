package com.refinedmods.refinedstorage2.api.network.node;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.network.Network;
import com.refinedmods.refinedstorage2.api.network.component.EnergyNetworkComponent;

public abstract class NetworkNodeImpl implements NetworkNode {
    protected Network network;
    private boolean active = true;

    @Override
    public Network getNetwork() {
        return network;
    }

    @Override
    public void setNetwork(Network network) {
        this.network = network;
    }

    @Override
    public final boolean isActive() {
        return active;
    }

    @Override
    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public void update() {
        network.getComponent(EnergyNetworkComponent.class).getEnergyStorage().extract(getEnergyUsage(), Action.EXECUTE);
    }

    public abstract long getEnergyUsage();
}
