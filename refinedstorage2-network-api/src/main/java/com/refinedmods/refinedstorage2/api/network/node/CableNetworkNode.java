package com.refinedmods.refinedstorage2.api.network.node;

public class CableNetworkNode extends AbstractNetworkNode {
    private final long energyUsage;

    public CableNetworkNode(final long energyUsage) {
        this.energyUsage = energyUsage;
    }

    @Override
    public long getEnergyUsage() {
        return energyUsage;
    }
}
