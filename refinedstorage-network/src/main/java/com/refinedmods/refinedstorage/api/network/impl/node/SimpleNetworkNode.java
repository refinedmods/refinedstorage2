package com.refinedmods.refinedstorage.api.network.impl.node;

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
