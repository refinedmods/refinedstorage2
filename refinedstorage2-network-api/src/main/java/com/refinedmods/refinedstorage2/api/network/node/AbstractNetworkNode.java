package com.refinedmods.refinedstorage2.api.network.node;

import com.refinedmods.refinedstorage2.api.network.Network;
import com.refinedmods.refinedstorage2.api.network.component.EnergyNetworkComponent;

import java.util.function.BooleanSupplier;
import javax.annotation.Nullable;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.0")
public abstract class AbstractNetworkNode implements NetworkNode {
    @Nullable
    protected Network network;
    @Nullable
    protected BooleanSupplier activenessProvider;

    private boolean active;

    public void setActivenessProvider(@Nullable final BooleanSupplier activenessProvider) {
        this.activenessProvider = activenessProvider;
    }

    @Override
    @Nullable
    public Network getNetwork() {
        return network;
    }

    @Override
    public void setNetwork(@Nullable final Network network) {
        this.network = network;
    }

    @Override
    public boolean isActive() {
        if (network == null || (activenessProvider != null && !activenessProvider.getAsBoolean())) {
            return false;
        }
        final long stored = network.getComponent(EnergyNetworkComponent.class).getStored();
        return stored >= getEnergyUsage();
    }

    @Override
    public void update() {
        updateActiveness();
        extractEnergy();
    }

    private void updateActiveness() {
        final boolean newActive = isActive();
        if (active != newActive) {
            active = newActive;
            onActiveChanged(active);
        }
    }

    private void extractEnergy() {
        if (!active || network == null) {
            return;
        }
        network.getComponent(EnergyNetworkComponent.class).extract(getEnergyUsage());
    }

    protected void onActiveChanged(final boolean newActive) {
    }

    public abstract long getEnergyUsage();
}
