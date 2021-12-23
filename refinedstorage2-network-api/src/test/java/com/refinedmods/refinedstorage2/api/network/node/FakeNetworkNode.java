package com.refinedmods.refinedstorage2.api.network.node;

public class FakeNetworkNode extends NetworkNodeImpl {
    private final long energyUsage;
    private int activenessChanges;

    public FakeNetworkNode(long energyUsage) {
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
