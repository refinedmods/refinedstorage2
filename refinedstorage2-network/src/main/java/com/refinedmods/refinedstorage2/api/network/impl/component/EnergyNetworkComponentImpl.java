package com.refinedmods.refinedstorage2.api.network.impl.component;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.network.component.EnergyNetworkComponent;
import com.refinedmods.refinedstorage2.api.network.energy.EnergyStorage;
import com.refinedmods.refinedstorage2.api.network.impl.energy.CompositeEnergyStorage;
import com.refinedmods.refinedstorage2.api.network.node.container.NetworkNodeContainer;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.1")
public class EnergyNetworkComponentImpl implements EnergyNetworkComponent {
    private final CompositeEnergyStorage energyStorage = new CompositeEnergyStorage();

    @Override
    public void onContainerAdded(final NetworkNodeContainer container) {
        if (container.getNode() instanceof EnergyStorage source) {
            energyStorage.addSource(source);
        }
    }

    @Override
    public void onContainerRemoved(final NetworkNodeContainer container) {
        if (container.getNode() instanceof EnergyStorage source) {
            energyStorage.removeSource(source);
        }
    }

    public long getStored() {
        return energyStorage.getStored();
    }

    public long getCapacity() {
        return energyStorage.getCapacity();
    }

    public long extract(final long amount) {
        return energyStorage.extract(amount, Action.EXECUTE);
    }
}
