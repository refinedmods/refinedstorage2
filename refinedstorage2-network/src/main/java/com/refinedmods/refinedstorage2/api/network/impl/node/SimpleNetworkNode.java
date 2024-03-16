package com.refinedmods.refinedstorage2.api.network.impl.node;

import com.refinedmods.refinedstorage2.api.network.impl.storage.AbstractNetworkNode;

public class SimpleNetworkNode extends AbstractNetworkNode {
    private long energyUsage;

    public SimpleNetworkNode(final long energyUsage) {
        this.energyUsage = energyUsage;
    }

    public void setEnergyUsage(final long energyUsage) {
        this.energyUsage = energyUsage;
    }

    @Override
    public long getEnergyUsage() {
        return energyUsage;
    }
}
