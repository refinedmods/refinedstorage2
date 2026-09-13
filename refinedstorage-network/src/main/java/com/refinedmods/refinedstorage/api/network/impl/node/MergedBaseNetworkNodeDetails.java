package com.refinedmods.refinedstorage.api.network.impl.node;

import com.refinedmods.refinedstorage.api.network.node.MergedNetworkNodeDetailsElement;

public class MergedBaseNetworkNodeDetails {
    public static final MergedNetworkNodeDetailsElement<MergedBaseNetworkNodeDetails> ELEMENT =
        MergedBaseNetworkNodeDetails::new;

    private long energyUsage;
    private boolean active;

    public void merge(final long nodeEnergyUsage, final boolean nodeActive) {
        this.energyUsage += nodeEnergyUsage;
        this.active |= nodeActive;
    }

    public long getEnergyUsage() {
        return energyUsage;
    }

    public boolean isActive() {
        return active;
    }
}
