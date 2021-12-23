package com.refinedmods.refinedstorage2.api.network.node;

public class CableNetworkNode extends NetworkNodeImpl {
    private final long energyUsage;

    public CableNetworkNode(long energyUsage) {
        this.energyUsage = energyUsage;
    }

    @Override
    public long getEnergyUsage() {
        return energyUsage;
    }
}
