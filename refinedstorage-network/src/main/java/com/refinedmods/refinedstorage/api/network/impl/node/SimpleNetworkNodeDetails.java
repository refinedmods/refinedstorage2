package com.refinedmods.refinedstorage.api.network.impl.node;

import com.refinedmods.refinedstorage.api.network.node.NetworkNodeDetails;

public class SimpleNetworkNodeDetails extends AbstractNetworkNodeDetails {
    public SimpleNetworkNodeDetails(final long energyUsage, final boolean active) {
        super(energyUsage, active);
    }

    public static NetworkNodeDetails of(final AbstractNetworkNode node) {
        return new SimpleNetworkNodeDetails(node.getEnergyUsage(), node.isActive());
    }
}
