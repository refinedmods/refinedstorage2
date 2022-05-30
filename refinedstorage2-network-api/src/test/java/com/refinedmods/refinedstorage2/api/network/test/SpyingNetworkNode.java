package com.refinedmods.refinedstorage2.api.network.test;

import com.refinedmods.refinedstorage2.api.network.node.NetworkNodeImpl;

public final class SpyingNetworkNode extends NetworkNodeImpl {
    private final long energyUsage;
    private int activenessChanges;

    public SpyingNetworkNode(long energyUsage) {
        this.energyUsage = energyUsage;
    }

    @Override
    protected void onActiveChanged(boolean active) {
        super.onActiveChanged(active);
        activenessChanges++;
    }

    public int getActivenessChanges() {
        return activenessChanges;
    }

    @Override
    public long getEnergyUsage() {
        return energyUsage;
    }
}
