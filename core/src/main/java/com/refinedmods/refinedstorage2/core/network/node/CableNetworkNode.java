package com.refinedmods.refinedstorage2.core.network.node;

import com.refinedmods.refinedstorage2.core.Rs2World;
import com.refinedmods.refinedstorage2.core.util.Position;

public class CableNetworkNode extends NetworkNodeImpl {
    private final long energyUsage;

    public CableNetworkNode(Rs2World world, Position position, long energyUsage) {
        super(world, position);
        this.energyUsage = energyUsage;
    }

    @Override
    protected long getEnergyUsage() {
        return energyUsage;
    }
}
