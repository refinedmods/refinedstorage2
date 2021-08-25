package com.refinedmods.refinedstorage2.api.network.node;

public class FakeNetworkNode extends NetworkNodeImpl {
    private final long energyUsage;
    private int updateCount;

    public FakeNetworkNode(long energyUsage) {
        this.energyUsage = energyUsage;
    }

    @Override
    public void update() {
        super.update();
        updateCount++;
    }

    public int getUpdateCount() {
        return updateCount;
    }

    @Override
    public long getEnergyUsage() {
        return energyUsage;
    }
}
