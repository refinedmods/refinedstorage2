package com.refinedmods.refinedstorage2.api.network.node;

import com.refinedmods.refinedstorage2.api.core.Position;

public class CableNetworkNode extends NetworkNodeImpl {
    private final long energyUsage;

    public CableNetworkNode(Position position, long energyUsage) {
        super(position);
        this.energyUsage = energyUsage;
    }

    @Override
    protected long getEnergyUsage() {
        return energyUsage;
    }
}
