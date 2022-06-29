package com.refinedmods.refinedstorage2.api.network.test;

import com.refinedmods.refinedstorage2.api.network.node.AbstractNetworkNode;

public final class SpyingNetworkNode extends AbstractNetworkNode {
    private final long energyUsage;
    private int activenessChanges;

    public SpyingNetworkNode(long energyUsage) {
        this.energyUsage = energyUsage;
    }

    @Override
    protected void onActiveChanged(boolean newActive) {
        super.onActiveChanged(newActive);
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
