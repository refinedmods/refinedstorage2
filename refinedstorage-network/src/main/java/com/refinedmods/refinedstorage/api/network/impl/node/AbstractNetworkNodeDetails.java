package com.refinedmods.refinedstorage.api.network.impl.node;

import com.refinedmods.refinedstorage.api.network.node.NetworkNodeDetails;

public abstract class AbstractNetworkNodeDetails implements NetworkNodeDetails {
    private final long energyUsage;
    private final boolean active;

    protected AbstractNetworkNodeDetails(final long energyUsage, final boolean active) {
        this.energyUsage = energyUsage;
        this.active = active;
    }

    public long getEnergyUsage() {
        return energyUsage;
    }

    public boolean isActive() {
        return active;
    }
}
