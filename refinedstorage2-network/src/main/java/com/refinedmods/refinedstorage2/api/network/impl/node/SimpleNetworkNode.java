package com.refinedmods.refinedstorage2.api.network.impl.node;

import com.refinedmods.refinedstorage2.api.network.node.AbstractNetworkNode;

public final class SimpleNetworkNode extends AbstractNetworkNode {
    private final long energyUsage;

    public SimpleNetworkNode(final long energyUsage) {
        this.energyUsage = energyUsage;
    }

    @Override
    public long getEnergyUsage() {
        return energyUsage;
    }
}
