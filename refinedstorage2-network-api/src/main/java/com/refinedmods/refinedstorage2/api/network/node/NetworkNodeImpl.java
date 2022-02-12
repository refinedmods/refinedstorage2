package com.refinedmods.refinedstorage2.api.network.node;

import com.refinedmods.refinedstorage2.api.network.Network;
import com.refinedmods.refinedstorage2.api.network.component.EnergyNetworkComponent;

import java.util.function.BooleanSupplier;

public abstract class NetworkNodeImpl implements NetworkNode {
    protected Network network;
    private boolean active;
    protected BooleanSupplier activenessProvider;

    public void setActivenessProvider(BooleanSupplier activenessProvider) {
        this.activenessProvider = activenessProvider;
    }

    @Override
    public Network getNetwork() {
        return network;
    }

    @Override
    public void setNetwork(Network network) {
        this.network = network;
    }

    @Override
    public boolean isActive() {
        if (activenessProvider != null && !activenessProvider.getAsBoolean()) {
            return false;
        }
        long stored = network.getComponent(EnergyNetworkComponent.class).getStored();
        return stored >= getEnergyUsage();
    }

    @Override
    public void update() {
        updateActiveness();
        extractEnergy();
    }

    private void updateActiveness() {
        boolean newActive = isActive();
        if (active != newActive) {
            active = newActive;
            onActiveChanged(active);
        }
    }

    private void extractEnergy() {
        if (!active) {
            return;
        }
        network.getComponent(EnergyNetworkComponent.class).extract(getEnergyUsage());
    }

    protected void onActiveChanged(boolean active) {
    }

    public abstract long getEnergyUsage();
}
