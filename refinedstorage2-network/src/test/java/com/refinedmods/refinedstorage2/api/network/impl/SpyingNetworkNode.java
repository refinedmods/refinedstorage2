package com.refinedmods.refinedstorage2.api.network.impl;

import com.refinedmods.refinedstorage2.api.network.impl.storage.AbstractNetworkNode;

public final class SpyingNetworkNode extends AbstractNetworkNode {
    private final long energyUsage;
    private int activenessChanges;

    public SpyingNetworkNode(final long energyUsage) {
        this.energyUsage = energyUsage;
    }

    @Override
    protected void onActiveChanged(final boolean newActive) {
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
