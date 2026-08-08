package com.refinedmods.refinedstorage.api.network.impl.node;

import com.refinedmods.refinedstorage.api.network.node.NetworkNodeDetails;

public class SimpleNetworkNodeDetails implements NetworkNodeDetails {
    private final long energyUsage;
    private final boolean active;

    public SimpleNetworkNodeDetails(final long energyUsage, final boolean active) {
        this.energyUsage = energyUsage;
        this.active = active;
    }

    public long getEnergyUsage() {
        return energyUsage;
    }

    public boolean isActive() {
        return active;
    }

    public static NetworkNodeDetails of(final AbstractNetworkNode node) {
        return new SimpleNetworkNodeDetails(node.getEnergyUsage(), node.isActive());
    }
}
