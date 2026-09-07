package com.refinedmods.refinedstorage.api.network.impl.node;

import com.refinedmods.refinedstorage.api.network.node.NetworkNodeDetails;

public abstract class AbstractNetworkNodeDetails implements NetworkNodeDetails {
    private long energyUsage;
    private boolean active;

    protected AbstractNetworkNodeDetails(final long energyUsage, final boolean active) {
        this.energyUsage = energyUsage;
        this.active = active;
    }

    public void update(final long newEnergyUsage, final boolean newActive) {
        this.energyUsage = newEnergyUsage;
        this.active = newActive;
    }

    public long getEnergyUsage() {
        return energyUsage;
    }

    public boolean isActive() {
        return active;
    }
}
