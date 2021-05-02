package com.refinedmods.refinedstorage2.core.network.node;

import com.refinedmods.refinedstorage2.core.Rs2World;
import com.refinedmods.refinedstorage2.core.util.Position;

public class CableNetworkNode extends NetworkNodeImpl {
    private final long energyUsage;

    public CableNetworkNode(Rs2World world, Position pos, NetworkNodeReference ref, long energyUsage) {
        super(world, pos, ref);
        this.energyUsage = energyUsage;
    }

    @Override
    public long getEnergyUsage() {
        return energyUsage;
    }
}
